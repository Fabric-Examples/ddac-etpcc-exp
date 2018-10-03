// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Asset
public class Item extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String I_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String I_IM_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String I_NAME;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double I_PRICE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String I_DATA;
}
