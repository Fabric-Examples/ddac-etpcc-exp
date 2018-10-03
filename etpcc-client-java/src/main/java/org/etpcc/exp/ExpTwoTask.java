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
 * Created by ailly on 17-11-16.
 */
public class ExpTwoTask implements Runnable{
    private static  final Logger logger = LoggerFactory.getLogger(CompanyTask.class);

    private final Counter counter;

    Etpcc workload;
    EtpccConnection connCompany;
    EtpccConnection connWarehouse;

    int id;

    boolean _2pc, encrypted;

    CountDownLatch latch;

    int txFlag;



    public ExpTwoTask(Etpcc workload, int id, Counter counter, int txFlag) {
        this.workload = workload;
        this.connCompany = workload.getConfig().debug() == 1 ?
                new MockEtpcc(workload.getConfig()) :
                new EtpccConnection(workload.getConfig());
        this.connWarehouse = workload.getConfig().debug() == 1 ?
                new MockEtpcc(workload.getConfig()) :
                new EtpccConnection(workload.getConfig());
        this.id = id;
        this.counter = counter;

        this.txFlag = txFlag;

        this._2pc = workload.getConfig().twoPC() == 1;
        this.encrypted = workload.getConfig().encrypted() == 1;
    }

    @Override
    public void run() {
        //StockLevel: flag == 3
        if (txFlag == 3 || txFlag == 5) {
            try {
                connWarehouse.connect("warehouse", 1);
                logger.info("warehouse created and countdown, warehouse W#{}", 1);
                logger.info(" warehouse start: W#{}", 1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (ComposerException e) {
                e.printStackTrace();
            } catch (IOException e) {
				e.printStackTrace();
			}

            if (txFlag == 3) {
                long start = System.currentTimeMillis();
                int c = 0;

                System.out.println("start warehouse TX: StockLevel");

                long last = System.currentTimeMillis();

                while(!Exp1.stopFlag){
                    workload.randomStockLevelForExpTwo(connWarehouse, _2pc, encrypted);
                    c++;
                    if(c > 0){
                        long end = System.currentTimeMillis();
                        System.out.println("Time : " + ((end - start) / (double) c));
                        System.out.println("Exp2 single StockLevel exec time: " +  (System.currentTimeMillis() - last) );
                        last = System.currentTimeMillis();
                    }
                    counter.stockLevelNum().getAndAdd(1);
                }

                logger.info("warehouse W#{} stopped", id);
                System.out.println("warehouse W#" + id + " stopped");
            } else {
                long start = System.currentTimeMillis();
                int c = 0;

                System.out.println("start warehouse TX: Delivery");

                long last = System.currentTimeMillis();

                while(!Exp1.stopFlag){
                    workload.randomDeliveryForExpTwo(connWarehouse, _2pc, encrypted);
                    c++;
                    if(c > 0){
                        long end = System.currentTimeMillis();
                        System.out.println("Time : " + ((end - start) / (double) c));
                        System.out.println("Exp2 single Delivery exec time: " +  (System.currentTimeMillis() - last) );
                        last = System.currentTimeMillis();
                    }
                    counter.deliveryNum().getAndAdd(1);
                }

                logger.info("warehouse W#{} stopped", id);
                System.out.println("warehouse W#" + id + " stopped");
            }


        } else {
            //Company TX: NewOrder, Payment, orderStatus
            try {
                connCompany.connect("company", 1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (ComposerException e) {
                e.printStackTrace();
            } catch (IOException e) {
				e.printStackTrace();
			}
            logger.info("company created and countdown, company C#{}", 1);
            logger.info("company start: C#{}", 1);

            //NewOrder: flag == 1
            if (txFlag == 1) {
                long start = System.currentTimeMillis();
                int c = 0;

                System.out.println("start company TX: NewOrder");

                long last = System.currentTimeMillis();

                while(!Exp1.stopFlag){
                	try{
                    //run NewOrder tx for exp1
                    workload.randomNewOrderForExpOne(connCompany, _2pc, encrypted);
                    c++;
                    if(c > 0){

                        long end = System.currentTimeMillis();
                        System.out.println("Time : " + ((end - start) / (double) c));
                        System.out.println("Exp2 single NewOrder exec time: " +  (System.currentTimeMillis() - last) );
                        last = System.currentTimeMillis();
                    }
                    counter.newOrderNum().getAndAdd(1);
                	} catch(Exception e){
                		e.printStackTrace();
                	}
                }
                logger.info("company C#{} stopped", id);
                System.out.println("company C#" + id + " stopped");

                //Payment: flag == 2
            } else if (txFlag == 2) {
                long start = System.currentTimeMillis();
                int c = 0;

                System.out.println("start company TX: Payment");

                long last = System.currentTimeMillis();

                while(!Exp2.stopFlag){
                    //run Payment tx for exp1
                    workload.randomPaymentForExpTwo(connCompany, _2pc, encrypted);
                    c++;
                    if(c > 0){

                        long end = System.currentTimeMillis();
                        System.out.println("Time : " + ((end - start) / (double) c));
                        System.out.println("Exp2 single Payment exec time: " +  (System.currentTimeMillis() - last) );
                        last = System.currentTimeMillis();
                    }
                    counter.paymentNum().getAndAdd(1);
                }
                logger.info("company C#{} stopped", id);
                System.out.println("company C#" + id + " stopped");

            } else if( txFlag == 4) {
                long start = System.currentTimeMillis();
                int c = 0;

                System.out.println("start company TX: OrderStatus");

                long last = System.currentTimeMillis();

                while(!Exp2.stopFlag){
                    //run Payment tx for exp1
                    workload.randomOrderStatusForExpTwo(connCompany, _2pc, encrypted);

                    c++;
                    if(c > 0){
                        long end = System.currentTimeMillis();
                        System.out.println("Time : " + ((end - start) / (double) c));
                        System.out.println("Exp2 single OrderStatus exec time: " +  (System.currentTimeMillis() - last) );
                        last = System.currentTimeMillis();
                    }
                    counter.paymentNum().getAndAdd(1);
                }
                logger.info("company C#{} stopped", id);
                System.out.println("company C#" + id + " stopped");

            } else {
                System.out.println("wrong strategy input!");
            }
        }

    }
}
