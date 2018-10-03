package org.etpcc.workload;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConfig;
import org.etpcc.definition.EtpccConnection;
import org.etpcc.exp.Exp3;
import org.etpcc.utils.Counter;
import org.hyperledger.composer.ComposerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ailly on 17-11-1.
 */
public class CompanyTask implements Runnable {
    private static  final Logger logger = LoggerFactory.getLogger(CompanyTask.class);
    private final Counter counter;
    EtpccConfig config = new EtpccConfig();


    Etpcc workload;
    EtpccConnection connCompany;

    int id;

    boolean _2pc, encrypted;

    CountDownLatch latch;

    public CompanyTask(Etpcc workload, int id, Counter counter, CountDownLatch latch) {
        this.workload = workload;
        this.connCompany = workload.getConfig().debug() == 1 ?
                new MockEtpcc(workload.getConfig()) :
                new EtpccConnection(workload.getConfig());
        this.id = id;
        this.counter = counter;
        this.latch = latch;
        this._2pc = workload.getConfig().twoPC() == 1;
        this.encrypted = workload.getConfig().encrypted() == 1;
    }

    @Override
    public void run() {
        try {
            connCompany.connect("company", id);
//            SimpleTpcc.companyLatch.countDown();
            this.latch.countDown();
            logger.info("company created and countdown, company C#{}", id);
//            SimpleTpcc.companyLatch.await();
            this.latch.await();
            logger.info("company start: C#{}", id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ComposerException e) {
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        long start = System.currentTimeMillis();
        int c = 0;
        long last = System.currentTimeMillis();

        while(!Exp3.stopFlag){
        	try{
        		 // simply go through all the transactions once for each without FabricXA
//                System.out.println("txNum: " + counter.txNum().get());
//                System.out.println("newOrderNum: " + counter.newOrderNum().get());
//                System.out.println("paymentNum: " + counter.newOrderNum().get());
//                System.out.println("orderstatus: " + counter.orderStatusNum().get());
//                System.out.println("newOrder percentage: " + (counter.newOrderNum().get() / counter.txNum().get()));
//                System.out.println("payment percentage: " + (counter.paymentNum().get() / counter.txNum().get()));
//                System.out.println("orderStatus percentage: " + (counter.orderStatusNum().get() / counter.txNum().get()));
//                System.out.println("ss: " + (config.newOrderPercentage() / 100.0) );
                if ((counter.newOrderNum().get() / counter.txNum().get()) <= (config.newOrderPercentage() / 100.0)) {
                    counter.newOrderNum().getAndAdd(1);
                    last = System.currentTimeMillis();
                    workload.randomNewOrder(connCompany, _2pc, encrypted);
                    c++;
                    if(c > 0){
                        System.out.println("start company TX: NewOrder");
                        long end = System.currentTimeMillis();
                        System.out.println("Time : " + ((end - start) / (double) c));
                        System.out.println("Exp3 single NewOrder exec time: " +  (System.currentTimeMillis() - last) );
                    }

                } else if ((counter.paymentNum().get() / counter.txNum().get()) <= (config.paymentPercentage() / 100.0) ) {
                    workload.randomPayment(connCompany, _2pc, encrypted);
                    counter.paymentNum().getAndAdd(1);
                } else if ((counter.orderStatusNum().get() / counter.txNum().get()) <= (config.orderStatusPercentage() / 100.0) ) {
                    workload.randomOrderStatus(connCompany, _2pc, encrypted);
                    counter.orderStatusNum().getAndAdd(1);
                } else  {
                    System.out.println("company: Waiting for next round");
                    Thread.sleep(1000);
                }
        	}catch(Exception e){
        		e.printStackTrace();
        	}
           
        }

        logger.info("company C#{} stopped", id);

    }


}
