package org.etpcc.exp;

import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConfig;
import org.etpcc.definition.EtpccConnection;
import org.etpcc.utils.Counter;
import org.etpcc.workload.CompanyTask;
import org.etpcc.workload.MockEtpcc;
import org.hyperledger.composer.ComposerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ailly on 17-11-24.
 */
public class InsertOrderLineTask implements Runnable {
    private static  final Logger logger = LoggerFactory.getLogger(CompanyTask.class);
    private final Counter counter;
    EtpccConfig config = new EtpccConfig();


    Etpcc workload;
    EtpccConnection connCompany;

    int id;

    boolean _2pc, encrypted;

    CountDownLatch latch;

    public InsertOrderLineTask(Etpcc workload, int id, Counter counter, CountDownLatch latch) {
        this.workload = workload;
        this.connCompany = workload.getConfig().debug() == 1 ?
                new MockEtpcc(workload.getConfig()) :
                new EtpccConnection(workload.getConfig());
        this.id = id;
        this.counter = counter;
        this.latch = latch;
//        this._2pc = workload.getConfig().twoPC() == 1;
//        this.encrypted = workload.getConfig().encrypted() == 1;
        this._2pc = false;
        this.encrypted = false;
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
			e.printStackTrace();
		}

        long start = System.currentTimeMillis();
        int c = 0;
        long last = System.currentTimeMillis();

        while(!InsertOrderLine.stopFlag){
            try{
                // simply go through all the transactions once for each without FabricXA
                workload.randomNewOrder(connCompany, _2pc, encrypted);
                counter.newOrderNum().getAndAdd(1);
                c++;
                if(c > 0){
                    System.out.println("start company TX: NewOrder");
                    long end = System.currentTimeMillis();
                    System.out.println("Time : " + ((end - start) / (double) c));
                    System.out.println("insert orderLine NewOrder exec time: " +  (System.currentTimeMillis() - last) );
                    last = System.currentTimeMillis();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        logger.info("company C#{} stopped", id);

    }

}
