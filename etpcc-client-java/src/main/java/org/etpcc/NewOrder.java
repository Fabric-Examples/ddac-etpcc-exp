// this code is generated and should not be modified
package org.etpcc;

@org.hyperledger.composer.annotation.Transaction
public class NewOrder extends FabricXA {
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Warehouse warehouse;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public District district;
	@org.hyperledger.composer.annotation.Pointer(optional = false)
	public Customer customer;
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public new_order_line_param[] order_lines;
}
