package org.etpcc.exp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.etpcc.algorithms.Alg1GeneralPartition;
import org.etpcc.algorithms.Alg2EncryptionPartition;
import org.etpcc.algorithms.Initialization;
import org.etpcc.algorithms.Model;
import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConfig;
import org.etpcc.utils.Counter;
import org.etpcc.workload.ComplicateTpcc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ailly on 17-11-15.
 */
public class Exp2 {
    final static private Logger logger = LoggerFactory.getLogger(ComplicateTpcc.class);

    public static volatile boolean stopFlag = false;

    public static void main(String[] args) {
        //NewOrder:1
        //Payment:2
        //StockLevel:3
        //OrderStatus:4
        //Delivery: 5
    	String partNum = System.getProperty("partNum");
    	if(partNum == null) {
    		partNum = args[0];
    	}
        int txFlag = Integer.parseInt(partNum);

        //new partitions
        Initialization initialization = new Initialization(50, 20);
        initialization.constructRel(0, 2);
//        Alg1GeneralPartition alg1GeneralPartition = new Alg1GeneralPartition(initialization.getEntities(), initialization.getModels());
//        ArrayList<HashSet<Model>> modelsPartition1 = alg1GeneralPartition.alg1GeneralPartition(); //加密集没有

        Alg2EncryptionPartition alg2EncryptionPartition = new Alg2EncryptionPartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        ArrayList<HashSet<Model>> modelsPartition2 = alg2EncryptionPartition.alg2EncryptionPartition();


        EtpccConfig config = new EtpccConfig();

        System.out.println("-----------Running Exp2---------");

        System.out.println(config);

        Set<String>[] EnpPartitions = new Set[modelsPartition2.size()];

        int i = 0;
        for (HashSet<Model> models : modelsPartition2) {
            HashSet<String> set = new HashSet<>();
            for (Model model : models) {
                set.add(model.getName());
            }
            EnpPartitions[i++] = set;
        }

        Set<String>[] partitions = null;
        if (config.encrypted() == 1) {
            partitions = EnpPartitions;
        }

        Counter counter = new Counter().txNum(0).stockLevelNum(0)
                .deliveryNum(0).orderStatusNum(0).paymentNum(0).newOrderNum(0);

        Etpcc workload = new Etpcc(partitions, config, counter);
        logger.info("Initializing datasets...");

        ExecutorService exp2ThreadPool = Executors.newCachedThreadPool();

        exp2ThreadPool.submit(new ExpTwoTask(workload, 1, counter, txFlag));


        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < config.execSeconds() * 1000) {
            try {
                Thread.sleep(config.printIntervalSeconds() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(counter.toString());
            System.out.println( counter.txNum().get() / ((System.currentTimeMillis() - start) / 1000 )   + " TXs / second");
            System.out.println( ((System.currentTimeMillis() - start) / 1000) / counter.txNum().get()   + " seconds / TX");
        }

        System.out.println("Prepare to kill all TXs");
        Exp2.stopFlag = true;

        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        exp2ThreadPool.shutdownNow();

        System.exit(1);
    }
}
