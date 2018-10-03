package org.etpcc.workload;

import java.util.Set;

import org.etpcc.definition.Etpcc;
import org.etpcc.definition.EtpccConfig;
import org.etpcc.definition.EtpccConnection;
import org.etpcc.utils.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleTpcc {
	final static private Logger logger = LoggerFactory.getLogger(SimpleTpcc.class);
	public static void main(String[] args) throws Exception {
		EtpccConfig config = new EtpccConfig();
		
		// Start standard tpcc workload
		EtpccConnection conn = new EtpccConnection(config);
		// No partition is generated for baseline
		Set<String>[] partitions = null;
		
		Etpcc workload = new Etpcc(partitions, config, new Counter());
		logger.info("Initializing datasets..."); 
		
		conn.connect("company", 1);
		//workload.generateLoad(conn);
		
		EtpccConnection connWarehouse = new EtpccConnection(config);
		
		connWarehouse.connect("warehouse", 1);
		
		logger.info("Start workloads...");
		long start = System.currentTimeMillis();
		int iteration = 0;
		while(true){
			// simply go through all the transactions once for each without FabricXA
			workload.randomNewOrder(conn, false, false);
			workload.randomPayment(conn, false, false);
			workload.randomDelivery(connWarehouse, false, false);
			workload.randomOrderStatus(conn, false, false);
			workload.randomStockLevel(connWarehouse, false, false);
			iteration++;
			if(System.currentTimeMillis() - start > 1000 * 120){
				break;
			}
		}
		
		logger.info("Complete {} iterations in {} seconds", iteration, (System.currentTimeMillis() - start)/1000.);
	}
	
}
