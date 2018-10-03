// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Asset
public class History extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String H_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String H_C_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String H_C_D_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String H_D_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String H_W_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public java.util.Date H_DATE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double H_AMOUNT;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String H_DATA;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Customer H_C_ID__H_C_D_ID__H_C_W_ID;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public District H_D_ID__H_W_ID;
}
