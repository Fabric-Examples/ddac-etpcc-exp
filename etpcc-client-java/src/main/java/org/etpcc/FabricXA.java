// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Transaction
public abstract class FabricXA extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public boolean two_PC;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = true, embedded = true)
	public String[] encryptionSet;

	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public boolean encrypted;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String targeCompany;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String targeWarehouse;
}
