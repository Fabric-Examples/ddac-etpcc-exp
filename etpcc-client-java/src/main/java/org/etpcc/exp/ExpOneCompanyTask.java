package org.etpcc.exp;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConnection;
import org.etpcc.utils.Counter;
import org.etpcc.workload.CompanyTask;
import org.etpcc.workload.MockEtpcc;
import org.hyperledger.composer.ComposerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ailly on 17-11-13.
 */
public class ExpOneCompanyTask implements Runnable{
    private static  final Logger logger = LoggerFactory.getLogger(CompanyTask.class);

    private final Counter counter;

    Etpcc workload;
    EtpccConnection connCompany;

    int id;

    boolean _2pc, encrypted;

    CountDownLatch latch;

    public ExpOneCompanyTask(Etpcc workload, int id, Counter counter, CountDownLatch latch, boolean twoPC) {
        this.workload = workload;
        this.connCompany = workload.getConfig().debug() == 1 ?
                new MockEtpcc(workload.getConfig()) :
                new EtpccConnection(workload.getConfig());
        this.id = id;
        this.counter = counter;
        this.latch = latch;
        this._2pc = twoPC;
        this.encrypted = workload.getConfig().encrypted() == 1;
    }

    @Override
    public void run() {
        try {
            connCompany.connect("company", 1);
//            SimpleTpcc.companyLatch.countDown();
            this.latch.countDown();
            logger.info("company created and countdown, company C#{}", 1);
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
			e.printStackTrace();
		}

        long start = System.currentTimeMillis();
        int c = 0;

        System.out.println("start company TX");

        long last = System.currentTimeMillis();

//        while(!Thread.interrupted()){
        while(!Exp1.stopFlag){
            //run NewOrder tx for exp1

            workload.randomNewOrderForExpOne(connCompany, _2pc, encrypted);
            c++;
            if(c > 0){

                long end = System.currentTimeMillis();
                System.out.println("Time : " + ((end - start) / (double) c));
                System.out.println("single exec time: " +  (System.currentTimeMillis() - last) );
                last = System.currentTimeMillis();
            }
            counter.newOrderNum().getAndAdd(1);
        }

        logger.info("company C#{} stopped", id);

        System.out.println("company C#" + id + " stopped");

    }
}
