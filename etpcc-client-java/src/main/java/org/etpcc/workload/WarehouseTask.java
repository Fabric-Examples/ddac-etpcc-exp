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
public class WarehouseTask implements Runnable {
	Etpcc workload;
	EtpccConnection connWarehouse;
	EtpccConfig config = new EtpccConfig();
	int id;
	boolean _2pc, encrypted;

	Counter counter;

	CountDownLatch latch;

	private static final Logger logger = LoggerFactory.getLogger(WarehouseTask.class);

	public WarehouseTask(Etpcc workload, int id, Counter counter, CountDownLatch latch) {
		this.workload = workload;
		this.connWarehouse = workload.getConfig().debug() == 1 ? new MockEtpcc(workload.getConfig())
				: new EtpccConnection(workload.getConfig());
		this.id = id;
		this.counter = counter;
		this.latch = latch;
		this._2pc = workload.getConfig().twoPC() == 1;
		this.encrypted = workload.getConfig().encrypted() == 1;
	}

	@Override
	public void run() {
		try {
			connWarehouse.connect("warehouse", id);
			// SimpleTpcc.warehouseLatch.countDown();
			this.latch.countDown();
			logger.info("warehouse created and countdown, warehouse W#{}", id);

			// SimpleTpcc.warehouseLatch.await();
			this.latch.await();
			logger.info(" warehouse start: W#{}", id);
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
		while (!Exp3.stopFlag) {
			try {
				// simply go through all the transactions once for each without
				// FabricXA
				if ((counter.stockLevelNum().get() / counter.txNum().get()) <= (config.stockLevelPercentage() / 100.0)) {
					workload.randomStockLevel(connWarehouse, _2pc, encrypted);
					counter.stockLevelNum().getAndAdd(1);
				} else if ((counter.deliveryNum().get() / counter.txNum().get()) <= (config.deliveryPercentage() / 100.0)) {
					workload.randomDelivery(connWarehouse, _2pc, encrypted);
					counter.deliveryNum().getAndAdd(1);
				} else  {
					System.out.println("warehouse: Waiting for next round");
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		logger.info("warehouse W#{}stopped", id);

	}
}
