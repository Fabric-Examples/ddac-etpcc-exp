// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Asset
public class Order extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String O_ID__O_D_ID__O_W_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String O_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String O_D_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String O_W_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String O_C_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public java.util.Date O_ENTRY_D;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = true, embedded = true)
	public String O_CARRIER_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double O_OL_CNT;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double O_ALL_LOCAL;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Customer O_C_ID__O_D_ID__O_W_ID;
}
