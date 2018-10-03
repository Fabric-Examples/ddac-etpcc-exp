// this code is generated and should not be modified
package org.etpcc;

import org.hyperledger.composer.system.Event;

@org.hyperledger.composer.annotation.Event
public class WriteSetEvent extends Event {
	@org.hyperledger.composer.annotation.DataField(primary = false, optional = false, embedded = true)
	public Operation operation;
}
