package org.etpcc.exp;

import org.etpcc.algorithms.*;
import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConfig;
import org.etpcc.utils.Counter;
import org.etpcc.workload.CompanyTask;
import org.etpcc.workload.ComplicateTpcc;
import org.etpcc.workload.WarehouseTask;
import org.hyperledger.composer.ComposerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ailly on 17-11-20.
 */
public class Exp3 {
    final static private Logger logger = LoggerFactory.getLogger(ComplicateTpcc.class);

    public static volatile boolean stopFlag = false;

    public static void main(String[] args) throws ComposerException, ClassNotFoundException {
        //strategy
        //lp:1
        //enp:2
        //gep:3
        EtpccConfig config = new EtpccConfig();
        System.out.println(config);
        System.out.println("config.strategy: " + config.strategy());

        int strategy = config.strategy();

        // switch partition strategy
        Set<String>[] partitions = null;
        ArrayList<HashSet<Model>> modelsPartition = null;
        Initialization initialization = new Initialization(config.numOfCompanies(), config.numOfWarehouses());
        initialization.constructRel(0, 2);

        if (strategy == 1) {
            //lp
            Alg1GeneralPartition alg1GeneralPartition = new Alg1GeneralPartition(initialization.getEntities(), initialization.getModels());
            modelsPartition = alg1GeneralPartition.alg1GeneralPartition();
        } else if (strategy == 2) {
            //enp
            Alg2EncryptionPartition alg2EncryptionPartition = new Alg2EncryptionPartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
            modelsPartition = alg2EncryptionPartition.alg2EncryptionPartition();
        } else if (strategy == 3) {
            //gep
            Alg3GepIncrePartition alg3GepIncrePartition = new Alg3GepIncrePartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
            modelsPartition = alg3GepIncrePartition.alg3GeneralEnceyptionPartition();
        } else {
            System.out.println("wrong strategy!");
        }

        if (modelsPartition.size() != 0) {

                Set<String>[] strategyPartitions = new Set[modelsPartition.size()];
                int countP = 0;
                for (HashSet<Model> models : modelsPartition) {
                    HashSet<String> set = new HashSet<>();
                    for (Model model : models) {
                        set.add(model.getName());
                    }
                    strategyPartitions[countP++] = set;
                }
                partitions = strategyPartitions;

        }

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
            System.out.println("========================== newOrder TX / second: " + 1000 * counter.newOrderNum().get() / (System.currentTimeMillis() - start));
            System.out.println("========================== newOrder TX / minute: " + 60 * 1000 * counter.newOrderNum().get() / (System.currentTimeMillis() - start));

        }

        System.out.println("Prepare to kill all TXs");
        Exp3.stopFlag = true;

        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        warehouseThreadPool.shutdownNow();
        companyThreadPool.shutdownNow();

        //save exp data

        System.exit(1);
    }
}
