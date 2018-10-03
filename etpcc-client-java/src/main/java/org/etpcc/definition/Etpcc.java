package org.etpcc.definition;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.etpcc.BranchTransaction;
import org.etpcc.Customer;
import org.etpcc.Delivery;
import org.etpcc.District;
import org.etpcc.Item;
import org.etpcc.NewOrder;
import org.etpcc.Operation;
import org.etpcc.OrderStatus;
import org.etpcc.Payment;
import org.etpcc.StockLevel;
import org.etpcc.Warehouse;
import org.etpcc.WriteSetEvent;
import org.etpcc.new_order_line_param;
import org.etpcc.utils.Counter;
import org.hyperledger.composer.ComposerException;
import org.hyperledger.composer.ResourceSerializer;
import org.hyperledger.composer.system.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Etpcc {
	final private static Logger logger = LoggerFactory.getLogger(Etpcc.class);
	private Set<String>[] partitions;
	private String[] encPartition;
	private EtpccConfig config;
	private Counter counter;

	public Etpcc(Set<String>[] partitions, EtpccConfig config, Counter counter) {
		this.partitions = partitions;
		if(this.partitions != null && this.partitions.length > 0) {
			this.encPartition = partitions[0].toArray(new String[partitions[0].size()]);
		}
		this.config = config;
		this.counter = counter;
	}

	private int generateRandomId(int total) {
		return ThreadLocalRandom.current().nextInt(1, total);
	}

	private int generateCustomerId(int districtId, int customersPerDistrict) {
		return (districtId - 1) * customersPerDistrict + generateRandomId(customersPerDistrict);
	}

	private int generateDistrictId(int warehouseId, int numOfWarehouses, int numOfDistricts,
			int districtsPerWarehouse) {
		// wid = j * chainSize + ((did - 1) % chainSize) + 1
		// (did - 1) % chainSize = (wid - 1) % chainSize
		int chainNum = numOfWarehouses * districtsPerWarehouse / numOfDistricts;
		int chainSize = numOfWarehouses / chainNum;
		int base = ThreadLocalRandom.current().nextInt(districtsPerWarehouse) * chainSize;
		int did = base + (warehouseId - 1) % chainSize + 1;
		if (did > numOfDistricts) {
			throw new IllegalArgumentException("District num is not consistant with Warehouses");
		}
		return did;
	}

	private String[] generateDistrictList(int warehouseId, int numOfWarehouses, int numOfDistricts,
			int districtsPerWarehouse) {
		int chainNum = numOfWarehouses * districtsPerWarehouse / numOfDistricts;
		int chainSize = numOfWarehouses / chainNum;
		String[] ret = new String[districtsPerWarehouse];
		for (int i = 0; i < districtsPerWarehouse; i++) {
			ret[i] = String.valueOf(i * chainSize + (warehouseId - 1) % chainSize + 1);
		}
		return ret;
	}

	public void randomNewOrderForExpOne(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		Object[] params = new Object[6];
		int warehouseId = 1;
		int districtId = generateDistrictId(warehouseId, config.numOfWarehouses(), config.numOfDistricts(),
				config.districtsPerWarehouse());
		params[0] = String.valueOf(districtId);
		params[1] = String.valueOf(generateCustomerId(districtId, config.customersPerDistrict()));
		params[2] = String.valueOf(warehouseId);
		int numOfOrderLine = 5 + ThreadLocalRandom.current().nextInt(10);

		params[3] = new String[numOfOrderLine];
		params[4] = new int[numOfOrderLine];
		params[5] = new String[numOfOrderLine];

		for (int i = 0; i < numOfOrderLine; i++) {
			((String[]) params[3])[i] = String.valueOf(generateRandomId(config.numOfItems()));
			((int[]) params[4])[i] = ThreadLocalRandom.current().nextInt(10);
			// TODO 1% using other warehouse in the same chain
			((String[]) params[5])[i] = String.valueOf(warehouseId);

		}
		logger.info("call newOrder {}", Arrays.toString(params));
		try {
			newOrder(conn, (String) params[0], (String) params[1], (String) params[2], (String[]) params[3],
					(int[]) params[4], (String[]) params[5], two_PC, encrypted);
			logger.info("finish newOrder");
		} catch (ComposerException e) {
			// TODO Handle random errors
			e.printStackTrace();
			logger.error("NewOrder execution error", e);
		}
	}

	public void randomNewOrder(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		Object[] params = new Object[6];
		int warehouseId = generateRandomId(config.numOfWarehouses());
		int districtId = generateDistrictId(warehouseId, config.numOfWarehouses(), config.numOfDistricts(),
				config.districtsPerWarehouse());
		params[0] = String.valueOf(districtId);
		params[1] = String.valueOf(generateCustomerId(districtId, config.customersPerDistrict()));
		params[2] = String.valueOf(warehouseId);
		int numOfOrderLine = 5 + ThreadLocalRandom.current().nextInt(10);

		params[3] = new String[numOfOrderLine];
		params[4] = new int[numOfOrderLine];
		params[5] = new String[numOfOrderLine];

		for (int i = 0; i < numOfOrderLine; i++) {
			((String[]) params[3])[i] = String.valueOf(generateRandomId(config.numOfItems()));
			((int[]) params[4])[i] = ThreadLocalRandom.current().nextInt(10);
			// TODO 1% using other warehouse in the same chain
			((String[]) params[5])[i] = String.valueOf(warehouseId);

		}
		logger.info("call newOrder {}", Arrays.toString(params));
		try {
			newOrder(conn, (String) params[0], (String) params[1], (String) params[2], (String[]) params[3],
					(int[]) params[4], (String[]) params[5], two_PC, encrypted);
			logger.info("finish newOrder");
		} catch (ComposerException e) {
			// TODO Handle random errors
			logger.error("randomNewOrder", e);
			e.printStackTrace();
		}
	}

	public void newOrder(EtpccConnection conn, String districtId, String customerId, String warehouseId,
			String[] itemIds, int[] itemNums, String[] itemWarehouses, boolean two_PC, boolean encrypted)
			throws ComposerException {
		NewOrder neword = new NewOrder();
		neword.district = ResourceSerializer.fromID(districtId, District.class);
		neword.customer = ResourceSerializer.fromID(customerId, Customer.class);
		neword.warehouse = ResourceSerializer.fromID(warehouseId, Warehouse.class);
		neword.targeCompany = String.valueOf(conn.getId());
		neword.targeWarehouse = warehouseId;
		neword.encryptionSet = this.encPartition;

		new_order_line_param[] params = new new_order_line_param[itemWarehouses.length];
		for (int i = 0; i < itemWarehouses.length; i++) {
			new_order_line_param ol = new new_order_line_param();
			ol.OL_I_ID = ResourceSerializer.fromID(itemIds[i], Item.class);
			ol.OL_QUANTITY = itemNums[i];
			ol.OL_SUPPLY_W_ID = ResourceSerializer.fromID(itemWarehouses[i], Warehouse.class);
			params[i] = ol;
		}

		neword.order_lines = params;
		neword.two_PC = two_PC;
		neword.encrypted = encrypted;
		conn.submitTransaction(neword, counter);
		if (neword.two_PC) {
			processBranchTransactions(conn);
		}
	}

	public void randomPaymentForExpTwo(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		Object[] params = new Object[4];
		int warehouseId = 1;
		int districtId = generateDistrictId(warehouseId, config.numOfWarehouses(), config.numOfDistricts(),
				config.districtsPerWarehouse());
		params[0] = String.valueOf(districtId);
		params[1] = String.valueOf(generateCustomerId(districtId, config.customersPerDistrict()));
		params[2] = String.valueOf(warehouseId);
		params[3] = ((Number) (ThreadLocalRandom.current().nextDouble(4999.0) + 1.0)).intValue();
		logger.info("call payment {}", Arrays.toString(params));
		try {
			payment(conn, (String) params[0], (String) params[1], (String) params[2], (int) params[3], two_PC,
					encrypted);
			logger.info("finish payment");
		} catch (ComposerException e) {
			e.printStackTrace();
		}
	}

	public void randomPayment(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		Object[] params = new Object[4];
		int warehouseId = generateRandomId(config.numOfWarehouses());
		int districtId = generateDistrictId(warehouseId, config.numOfWarehouses(), config.numOfDistricts(),
				config.districtsPerWarehouse());
		params[0] = String.valueOf(districtId);
		params[1] = String.valueOf(generateCustomerId(districtId, config.customersPerDistrict()));
		params[2] = String.valueOf(warehouseId);
		params[3] = ((Number) (ThreadLocalRandom.current().nextDouble(4999.0) + 1.0)).intValue();
		logger.info("call payment {}", Arrays.toString(params));
		try {
			payment(conn, (String) params[0], (String) params[1], (String) params[2], (int) params[3], two_PC,
					encrypted);
			logger.info("finish payment");
		} catch (ComposerException e) {
			e.printStackTrace();
		}
	}

	public void payment(EtpccConnection conn, String districtId, String customerId, String warehouseId, int amount,
			boolean two_PC, boolean encrypted) throws ComposerException {
		Payment payment = new Payment();
		payment.district = ResourceSerializer.fromID(districtId, District.class);
		payment.customer = ResourceSerializer.fromID(customerId, Customer.class);
		payment.warehouse = ResourceSerializer.fromID(warehouseId, Warehouse.class);
		payment.H_AMOUNT = amount;
		payment.two_PC = two_PC;
		payment.encrypted = encrypted;
		payment.targeCompany = String.valueOf(conn.getId());
		payment.targeWarehouse = warehouseId;
		payment.encryptionSet = this.encPartition;
		conn.submitTransaction(payment, counter);
		if (payment.two_PC) {
			processBranchTransactions(conn);
		}
	}

	public void randomDeliveryForExpTwo(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		Object[] params = new Object[3];
		int warehouseId = conn.getId();
		int districtId = generateDistrictId(warehouseId, config.numOfWarehouses(), config.numOfDistricts(),
				config.districtsPerWarehouse());
		params[0] = String.valueOf(districtId);
		params[1] = String.valueOf(ThreadLocalRandom.current().nextInt(1, 11));
		params[2] = String.valueOf(warehouseId);
		logger.info("call delivery {}", Arrays.toString(params));
		try {
			deliveryForExpTwo(conn, (String) params[0], (String) params[1], (String) params[2], two_PC, encrypted);
			logger.info("finish delivery");
		} catch (ComposerException e) {
			e.printStackTrace();
		}
	}

	public void deliveryForExpTwo(EtpccConnection conn, String districtId, String carrierId, String warehouseId, boolean two_PC,
						 boolean encrypted) throws ComposerException {
		Delivery delivery = new Delivery();
		delivery.O_CARRIER_ID = carrierId;

		delivery.districtNum = this.generateDistrictList(Integer.parseInt(warehouseId), config.numOfWarehouses(),
				config.numOfDistricts(), config.districtsPerWarehouse());
		delivery.warehouse = ResourceSerializer.fromID(warehouseId, Warehouse.class);
		delivery.two_PC = two_PC;
		delivery.encrypted = encrypted;
//		delivery.targeCompany = String.valueOf(ThreadLocalRandom.current().nextInt(1, config.numOfCompanies()));
		delivery.targeCompany = "1";
		delivery.targeWarehouse = warehouseId;
		delivery.encryptionSet = this.encPartition;
		conn.submitTransaction(delivery, counter);
		if (delivery.two_PC) {
			processBranchTransactions(conn);
		}
	}

	public void randomDelivery(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		Object[] params = new Object[3];
		int warehouseId = conn.getId();
		int districtId = generateDistrictId(warehouseId, config.numOfWarehouses(), config.numOfDistricts(),
				config.districtsPerWarehouse());
		params[0] = String.valueOf(districtId);
		params[1] = String.valueOf(ThreadLocalRandom.current().nextInt(1, 11));
		params[2] = String.valueOf(warehouseId);
		logger.info("call delivery {}", Arrays.toString(params));
		try {
			delivery(conn, (String) params[0], (String) params[1], (String) params[2], two_PC, encrypted);
			logger.info("finish delivery");
		} catch (ComposerException e) {
			e.printStackTrace();
		}
	}

	public void delivery(EtpccConnection conn, String districtId, String carrierId, String warehouseId, boolean two_PC,
			boolean encrypted) throws ComposerException {
		Delivery delivery = new Delivery();
		delivery.O_CARRIER_ID = carrierId;

		delivery.districtNum = this.generateDistrictList(Integer.parseInt(warehouseId), config.numOfWarehouses(),
				config.numOfDistricts(), config.districtsPerWarehouse());
		delivery.warehouse = ResourceSerializer.fromID(warehouseId, Warehouse.class);
		delivery.two_PC = two_PC;
		delivery.encrypted = encrypted;
		delivery.targeCompany = String.valueOf(ThreadLocalRandom.current().nextInt(1, config.numOfCompanies()));
		delivery.targeWarehouse = warehouseId;
		delivery.encryptionSet = this.encPartition;
		conn.submitTransaction(delivery, counter);
		if (delivery.two_PC) {
			processBranchTransactions(conn);
		}
	}

	public void randomOrderStatusForExpTwo(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		logger.info("call orderStatus ");
		try {
			orderStatusForExpTwo(conn, String.valueOf(this.generateRandomId(config.numOfCustomers())), two_PC, encrypted);
			logger.info("finish orderStatus");
		} catch (ComposerException e) {
			// TODO handle errors
			e.printStackTrace();
		}
	}

	public void orderStatusForExpTwo(EtpccConnection conn, String customerId, boolean two_PC, boolean encrypted)
			throws ComposerException {
		OrderStatus order_status = new OrderStatus();
		order_status.customer = ResourceSerializer.fromID(customerId, Customer.class);
		order_status.two_PC = two_PC;
		order_status.encrypted = encrypted;
		order_status.targeCompany = String.valueOf(1);
		order_status.targeWarehouse = String.valueOf(1);
//		order_status.targeWarehouse = String.valueOf(ThreadLocalRandom.current().nextInt(1, config.numOfWarehouses()));
		order_status.encryptionSet = this.encPartition;
		conn.submitTransaction(order_status, counter);
	}

	public void randomOrderStatus(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		logger.info("call orderStatus ");
		try {
			orderStatus(conn, String.valueOf(this.generateRandomId(config.numOfCustomers())), two_PC, encrypted);
			logger.info("finish orderStatus");
		} catch (ComposerException e) {
			// TODO handle errors
			e.printStackTrace();
		}
	}

	public void orderStatus(EtpccConnection conn, String customerId, boolean two_PC, boolean encrypted)
			throws ComposerException {
		OrderStatus order_status = new OrderStatus();
		order_status.customer = ResourceSerializer.fromID(customerId, Customer.class);
		order_status.two_PC = two_PC;
		order_status.encrypted = encrypted;
		order_status.targeCompany = String.valueOf(conn.getId());
		order_status.targeWarehouse = String.valueOf(ThreadLocalRandom.current().nextInt(1, config.numOfWarehouses()));
		order_status.encryptionSet = this.encPartition;
		conn.submitTransaction(order_status, counter);
	}

	public void randomStockLevel(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		logger.info("call stockLevel ");
		int warehouseId = conn.getId();
		int districtId = generateDistrictId(warehouseId, config.numOfWarehouses(), config.numOfDistricts(),
				config.districtsPerWarehouse());
		try {
			stockLevel(conn, String.valueOf(districtId), two_PC, encrypted);
			logger.info("finish stockLevel");
		} catch (ComposerException e) {
			// TODO handle errors
			e.printStackTrace();
		}
	}

	public void randomStockLevelForExpTwo(EtpccConnection conn, boolean two_PC, boolean encrypted) {
		logger.info("call stockLevel ");
		int warehouseId = 1;
		int districtId = generateDistrictId(warehouseId, config.numOfWarehouses(), config.numOfDistricts(),
				config.districtsPerWarehouse());
		try {
			stockLevelForExpTwo(conn, String.valueOf(districtId), two_PC, encrypted);
			logger.info("finish stockLevel");
		} catch (ComposerException e) {
			// TODO handle errors
			e.printStackTrace();
		}
	}

	public void stockLevelForExpTwo(EtpccConnection conn, String districtId, boolean two_PC, boolean encrypted)
			throws ComposerException {
		StockLevel stock_level = new StockLevel();
		stock_level.district = ResourceSerializer.fromID(districtId, District.class);
		stock_level.two_PC = two_PC;
		stock_level.encrypted = encrypted;
		stock_level.targeCompany = "1";
		stock_level.targeWarehouse = String.valueOf(conn.getId());
		stock_level.encryptionSet = this.encPartition;
		conn.submitTransaction(stock_level, counter);
	}

	public void stockLevel(EtpccConnection conn, String districtId, boolean two_PC, boolean encrypted)
			throws ComposerException {
		StockLevel stock_level = new StockLevel();
		stock_level.district = ResourceSerializer.fromID(districtId, District.class);
		stock_level.two_PC = two_PC;
		stock_level.encrypted = encrypted;
		stock_level.targeCompany = String.valueOf(generateRandomId(config.numOfCompanies()));
		stock_level.targeWarehouse = String.valueOf(conn.getId());
		stock_level.encryptionSet = this.encPartition;
		conn.submitTransaction(stock_level, counter);
	}

	public void processBranchTransactions(EtpccConnection conn) throws ComposerException {
		Event[] events = conn.getEvent();
		if(events == null){
			return;
		}
		List<WriteSetEvent> eventList = Arrays.stream(events).map(e -> (WriteSetEvent) e).collect(Collectors.toList());
		int numOfBranches = 0;
		for (Set<String> partition : partitions) {
			// get the id of the model from the core participant
			Operation[] operations = eventList.stream().map(e -> e.operation)
					.filter(e -> partition.contains(e.modelName)).toArray(Operation[]::new);
			if (operations.length > 0) {
				BranchTransaction bt = new BranchTransaction();
				bt.operations = operations;
				conn.submitTransaction(bt, counter);
				numOfBranches++;
			}
		}

		logger.info("Split to {} branch transactions", numOfBranches);
		System.out.println("Split to " + numOfBranches + " tx");
	}

	public EtpccConfig getConfig() {
		return config;
	}
}
