// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Asset
public class District extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String D_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String D_NAME;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String D_STREET_1;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String D_STREET_2;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String D_CITY;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String D_STATE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String D_ZIP;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double D_TAX;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double D_YTD;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int D_NEXT_O_ID;
	@org.hyperledger.composer.annotation.Pointer(optional = true)
	public Warehouse[] D_W_IDS;
}
