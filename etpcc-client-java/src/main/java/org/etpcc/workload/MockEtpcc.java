package org.etpcc.workload;

import org.etpcc.definition.EtpccConfig;
import org.etpcc.definition.EtpccConnection;
import org.etpcc.utils.Counter;
import org.hyperledger.composer.ComposerException;
import org.hyperledger.composer.system.Event;

/**
 * Created by ailly on 17-11-2.
 */
public class MockEtpcc extends EtpccConnection {

    public MockEtpcc(EtpccConfig config) {
        super(config);
    }

    @Override
    public synchronized void connect(String role, int id) throws ClassNotFoundException, ComposerException {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Event[] getEvent() throws ComposerException {
        return new Event[0];
    }

    @Override
    public void submitTransaction(Object tx, final Counter counter) throws ComposerException {
        try {
            Thread.sleep(2000);
            counter.txNum().getAndAdd(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            counter.failTxNum().getAndAdd(1);
        }
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public String getRole() {
        return "mock Role";
    }
}
