// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Transaction
public class GenerateLoad extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int numOfCompanies;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int numOfWarehouses;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int numOfDistricts;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int numOfItems;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int districtsPerWarehouse;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int customersPerDistrict;
}
