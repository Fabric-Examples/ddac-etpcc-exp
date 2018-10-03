// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Asset
public class Customer extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String C_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_D_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_FIRST;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_MIDDLE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_LAST;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_STREET_1;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_STREET_2;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_CITY;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_STATE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_ZIP;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_PHONE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public java.util.Date C_SINCE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_CREDIT;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double C_CREDIT_LIM;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double C_DISCOUNT;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double C_BALANCE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double C_YTD_PAYMENT;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double C_PAYMENT_CNT;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public double C_DELIVERY_CNT;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String C_DATA;
}
