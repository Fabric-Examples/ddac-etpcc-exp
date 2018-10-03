package org.etpcc.workload;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConfig;
import org.etpcc.utils.Counter;
import org.hyperledger.composer.ComposerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ComplicateTpcc {
    final static private Logger logger = LoggerFactory.getLogger(ComplicateTpcc.class);

    public static void main(String[] args) throws ComposerException, ClassNotFoundException {
        EtpccConfig config = new EtpccConfig();

        System.out.println(config);

        Set<String>[] partitions = null;

	    Counter counter = new Counter().txNum(4).stockLevelNum(1)
			    .deliveryNum(1).orderStatusNum(1).paymentNum(1).newOrderNum(0);

        Etpcc workload = new Etpcc(partitions, config, counter);
        logger.info("Initializing datasets...");


        //TODO chenshi

        CountDownLatch txLatch = new CountDownLatch(config.companyNum() * config.companyParallelism() + config.warehouseNum() * config.warehouseParallelism());

        ExecutorService warehouseThreadPool = Executors.newCachedThreadPool();
        ExecutorService companyThreadPool = Executors.newCachedThreadPool();

	    for (int id = 0; id < config.companyNum(); id++) {
            logger.info("Add new company tasks: C#{}", (id + 1));
            //company threads
            for (int i = 0; i < config.companyParallelism(); i++) {
                companyThreadPool.submit(new CompanyTask(workload,id + 1, counter, txLatch));
            }
        }

        for (int id = 0; id < config.warehouseNum(); id++) {
            //warehouse threads
            logger.info("Add new warehouse tasks: W#{}", (id + 1));
            for (int i = 0; i < config.warehouseParallelism(); i++) {
                warehouseThreadPool.submit(new WarehouseTask(workload,id + 1, counter, txLatch));
            }
        }

        try {
            txLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < config.execSeconds() * 1000) {
            try {
                Thread.sleep(config.printIntervalSeconds() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

	        System.out.println(counter.toString());

        }


        warehouseThreadPool.shutdownNow();
        companyThreadPool.shutdownNow();

        System.exit(1);
    }

}
