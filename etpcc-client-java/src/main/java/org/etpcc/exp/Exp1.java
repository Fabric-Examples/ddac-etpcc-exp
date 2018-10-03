package org.etpcc.exp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConfig;
import org.etpcc.utils.Counter;
import org.etpcc.workload.ComplicateTpcc;
import org.hyperledger.composer.ComposerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ailly on 17-11-13.
 */
public class Exp1 {
    final static private Logger logger = LoggerFactory.getLogger(ComplicateTpcc.class);

    public static volatile boolean stopFlag = false;

    public static void main(String[] args) throws ComposerException, ClassNotFoundException {
        EtpccConfig config = new EtpccConfig();
        int currentPart = Integer.parseInt(System.getProperty("partNum", "0"));
        
        System.out.println("Exp1 using " + currentPart);

        System.out.println(config);

        Set<String>[] part1 = new Set[]{
                new HashSet<String>(),
                new HashSet<String>(Arrays.asList("New_Order1", "Order1", "Order_Line1", "District", "Stock1")),
        };

        Set<String>[] part2 = new Set[]{
                new HashSet<String>(),
                new HashSet<>(Arrays.asList("New_Order1", "Order1")),
                new HashSet<>(Arrays.asList("Order_Line1", "District", "Stock1")),
        };

        Set<String>[] part3 = new Set[]{
                new HashSet<String>(),
                new HashSet<>(Arrays.asList("New_Order1", "Order1")),
                new HashSet<>(Arrays.asList("Order_Line1", "District")),
                new HashSet<>(Arrays.asList("Stock1")),
        };

        Set<String>[] part4 = new Set[]{
                new HashSet<String>(),
                new HashSet<>(Arrays.asList("New_Order1", "Order1")),
                new HashSet<>(Arrays.asList("Order_Line1")),
                new HashSet<>(Arrays.asList("District")),
                new HashSet<>(Arrays.asList("Stock1")),
        };

        Set<String>[] part5 = new Set[]{
                new HashSet<String>(),
                new HashSet<>(Arrays.asList("New_Order1")),
                new HashSet<>(Arrays.asList("Order1")),
                new HashSet<>(Arrays.asList("Order_Line1")),
                new HashSet<>(Arrays.asList("District")),
                new HashSet<>(Arrays.asList("Stock1")),
        };
        
        
        Set<String>[][] partitions = new Set[][]{
        	part1, part2, part3, part4, part5
        	};
        

        Counter counter = new Counter().txNum(0).stockLevelNum(0)
                .deliveryNum(0).orderStatusNum(0).paymentNum(0).newOrderNum(0);
        Etpcc workload = new Etpcc(partitions[currentPart], config, counter);
        logger.info("Initializing datasets...");


        //TODO chenshi
        CountDownLatch txLatch = new CountDownLatch(config.companyNum() * config.companyParallelism());

        ExecutorService companyThreadPool = Executors.newCachedThreadPool();

        for (int id = 0; id < config.companyNum(); id++) {
            logger.info("Add new company tasks: C#{}", (id + 1));
            //company threads
            for (int i = 0; i < config.companyParallelism(); i++) {
                companyThreadPool.submit(new ExpOneCompanyTask(workload,id + 1, counter, txLatch, currentPart > 0));
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

//            System.out.println(counter.toString());
//            System.out.println( counter.txNum().get() / ((System.currentTimeMillis() - start) / 1000 )   + " TXs / second");
//            System.out.println( ((System.currentTimeMillis() - start) / 1000) / counter.txNum().get()   + " seconds / TX");

        }

        System.out.println("Prepare to kill all TXs");
        Exp1.stopFlag = true;

        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        companyThreadPool.shutdownNow();

        System.exit(1);
    }
}
