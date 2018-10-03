// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Participant
public class Warehouse extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String W_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String W_NAME;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String W_STREET_1;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String W_STREET_2;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String W_CITY;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String W_STATE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String W_ZIP;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double W_TAX;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double W_YTD;
}
