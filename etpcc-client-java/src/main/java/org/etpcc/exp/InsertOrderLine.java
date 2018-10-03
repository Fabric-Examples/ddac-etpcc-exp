package org.etpcc.exp;

import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConfig;
import org.etpcc.utils.Counter;
import org.etpcc.workload.CompanyTask;
import org.etpcc.workload.ComplicateTpcc;
import org.etpcc.workload.WarehouseTask;
import org.hyperledger.composer.ComposerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ailly on 17-11-24.
 */
public class InsertOrderLine {
    final static private Logger logger = LoggerFactory.getLogger(ComplicateTpcc.class);

    public static volatile boolean stopFlag = false;

    public static void main(String[] args) throws ComposerException, ClassNotFoundException {
        EtpccConfig config = new EtpccConfig();

        int gjx= 100;

        System.out.println(config);

        Set<String>[] partitions = null;

        Counter counter = new Counter().txNum(4).stockLevelNum(1)
                .deliveryNum(1).orderStatusNum(1).paymentNum(1).newOrderNum(0);

        Etpcc workload = new Etpcc(partitions, config, counter);
        logger.info("Initializing datasets...");


        //TODO chenshi

        CountDownLatch txLatch = new CountDownLatch(10);

        ExecutorService companyThreadPool = Executors.newCachedThreadPool();

        for (int id = 0; id < 10; id++) {
            logger.info("Add new company tasks: C#{}", (id + 1));
            //company threads
            for (int i = 0; i < 1; i++) {
                companyThreadPool.submit(new InsertOrderLineTask(workload,id + 1, counter, txLatch));
            }
        }

        try {
            txLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < config.execSeconds() * 1000) {
            if (counter.newOrderNum().get() > gjx) {
                InsertOrderLine.stopFlag = true;
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                companyThreadPool.shutdownNow();
                //save exp data
                System.exit(1);
            }
            try {
                Thread.sleep(config.printIntervalSeconds() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(counter.toString());
        }

        System.out.println("Prepare to kill all TXs=====for insert orderLine");
        InsertOrderLine.stopFlag = true;

        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        companyThreadPool.shutdownNow();

        //save exp data

        System.exit(1);
    }


}
