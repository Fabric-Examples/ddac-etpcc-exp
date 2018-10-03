// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Participant
public class Company extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String P_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String P_NAME;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String P_STREET_1;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String P_STREET_2;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String P_CITY;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String P_STATE;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String P_ZIP;
}
