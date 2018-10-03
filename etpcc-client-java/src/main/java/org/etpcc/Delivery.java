// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Transaction
public class Delivery extends FabricXA {
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Warehouse warehouse;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String O_CARRIER_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String[] districtNum;
}
