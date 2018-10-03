package org.etpcc.definition;

import java.util.Properties;

public class EtpccConfig {

	Properties properties;

	public EtpccConfig() {
		properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream("etpcc.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int districtsPerWarehouse() {
		return intProperty("etpcc.districtsPerWarehouse");
	}

	public int customersPerDistrict() {
		return intProperty("etpcc.customersPerDistrict") ;
	}

	public int numOfCustomers() {
		return customersPerDistrict() * districtsPerWarehouse();
	}

	public int numOfWarehouses() {
		String prop = System.getProperty("numOfWarehouses");
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.numOfWarehouses");
	}

	public int numOfCompanies() {
		String prop = System.getProperty("numOfCompanies");
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.numOfCompanies");
	}

	public int numOfDistricts() {
		return intProperty("etpcc.numOfDistricts");
	}

	public int numOfItems() {
		return intProperty("etpcc.numOfItems");
	}

	public int companyParallelism() {
		String prop = System.getProperty("companyParallelism");
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.companyParallelism");
	}

	public int warehouseParallelism() {
		String prop = System.getProperty("warehouseParallelism");
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.warehouseParallelism");
	}

	public int newOrderPercentage(){ return intProperty("etpcc.percentage.newOrder"); }

	public int paymentPercentage(){ return intProperty("etpcc.percentage.payment"); }

	public int orderStatusPercentage(){ return intProperty("etpcc.percentage.orderStatus"); }

	public int stockLevelPercentage(){ return intProperty("etpcc.percentage.stockLevel"); }

	public int deliveryPercentage(){ return intProperty("etpcc.percentage.delivery"); }

	public int strategy() {
		String prop = System.getProperty("partNum").split("@")[0];
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.strategy");
	}

	public int companyNum() {
		String prop = System.getProperty("partNum").split("@")[1];
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.companyNum");
	}

	public int warehouseNum() {
		String prop = System.getProperty("partNum").split("@")[2];
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.warehouseNum");
	}

	public int execSeconds() {
		return intProperty("etpcc.execSeconds");
	}

	public int printIntervalSeconds() {
		return intProperty("etpcc.printIntervalSeconds");
	}

	public int debug() {
		return intProperty("etpcc.debug");
	}

	public int twoPC() {
		String prop = System.getProperty("twoPC");
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.twoPC");
	}

	public int encrypted() {
		String prop = System.getProperty("encrypted");
		if(prop != null && prop.length() > 0) {
			return Integer.parseInt(prop);
		}
		return intProperty("etpcc.encrypted");
	}

	private int intProperty(String key) {
		return Integer.parseInt(properties.getProperty(key));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Etpcc Configurations:\n");
		properties.forEach((key, value) -> builder.append('\t').append(key).append("=").append(value).append('\n'));
		return builder.append('\n').toString();
	}
}
