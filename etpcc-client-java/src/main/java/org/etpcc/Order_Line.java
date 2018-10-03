// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Asset
public class Order_Line extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String id;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String OL_O_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String OL_D_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String O_W_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int OL_NUMBER;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String OL_I_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String OL_SUPPLY_W_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = true, embedded = true)
	public java.util.Date OL_DELIVERY_D;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double OL_QUANTITY;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double OL_AMOUNT;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String OL_DIST_INFO;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Order order;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Stock stock;
}
