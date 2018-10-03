// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Concept
public class new_order_line_param {
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Item OL_I_ID;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Warehouse OL_SUPPLY_W_ID;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public int OL_QUANTITY;
}
