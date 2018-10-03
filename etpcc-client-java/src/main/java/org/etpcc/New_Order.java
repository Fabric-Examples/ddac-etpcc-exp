// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Asset
public class New_Order extends Object {
	@org.hyperledger.composer.annotation.DataField(primary = true, optional = false, embedded = true)
	public String NO_O_ID__NO_D_ID__NO_W_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String NO_O_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String NO_D_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public String NO_W_ID;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Order F_NO_O_ID__NO_D_ID__NO_W_ID;
}
