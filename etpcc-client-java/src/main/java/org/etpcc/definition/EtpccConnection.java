package org.etpcc.definition;

import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.etpcc.BranchTransaction;
import org.etpcc.utils.ConnectionProfile;
import org.etpcc.utils.Counter;
import org.etpcc.utils.ETPCCWallet;
import org.hyperledger.composer.ComposerException;
import org.hyperledger.composer.client.ComposerConnection;
import org.hyperledger.composer.client.ComposerDriverManager;
import org.hyperledger.composer.client.ComposerUser;
import org.hyperledger.composer.client.EnrollRequest;
import org.hyperledger.composer.system.Event;

public class EtpccConnection {

	final private static ETPCCWallet UserWallet;
	private static boolean IS_ENROLLED = false;

	static {
		UserWallet = new ETPCCWallet();
	}

	private ComposerConnection connection;
	private BlockingQueue<Event[]> events;
	static private final Set<String> receivedEventIds;
	private String role;
	private int id;
	private EtpccConfig config;
	private Pattern transactionConflict = Pattern.compile(
			"Transaction\\([^\\\\)]+\\) failed: Received invalid transaction event\\. Transaction ID [^\\s]+ status 11");

	static {
		receivedEventIds = new HashSet<>();
	}

	public EtpccConnection(EtpccConfig config) {
		this.config = config;
	}

	/**
	 * @param role
	 *            warehouse or company
	 * @param id
	 *            start from 1
	 * @throws ClassNotFoundException
	 * @throws ComposerException
	 * @throws IOException 
	 */
	synchronized public void connect(String role, int id) throws ClassNotFoundException, ComposerException, IOException {
		Class.forName("org.hyperledger.composer.driver.hlfv1.FabricDriver");
		if (!role.equals("warehouse") && !role.equals("company")) {
			throw new ComposerException(ComposerException.APPLICATION_ERROR,
					"role should be either warehouse or company");
		}
		int numOfCompany = this.config.numOfCompanies();
		String peerName = "peer";
		if (role.equals("company")) {
			peerName += id;
		} else {
			peerName += (numOfCompany + id);
		}
		this.role = role;
		this.id = id;
		String connStr = ConnectionProfile.toConnectionString(peerName);

		if (!IS_ENROLLED) {
			ComposerDriverManager.enroll(connStr, UserWallet,
					new EnrollRequest().userId("admin").affiliation("a").secret("adminpw"));
			String privateKey = "-----BEGIN PRIVATE KEY-----\n"
					+ "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgLMv9CrEbwRLVZZRk\n"
					+ "Nm8M3cbl4A1IkWAlrnUrdd2A8JqhRANCAASnvddvDDgbBcc9qqyS7YaCtpMCpOic\n"
					+ "yOCOzg/1V7nrmzJZ/LDFj1weSiVZ0w4kcaOWfSghglbx0hCpTJN6GmjK\n"
					+ "-----END PRIVATE KEY-----";

			String cert = "-----BEGIN CERTIFICATE-----\n"
					+ "MIICBzCCAa6gAwIBAgIRAI5Knx5BE9YMGawotM9+vg0wCgYIKoZIzj0EAwIwZzEL\n"
					+ "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n"
					+ "cmFuY2lzY28xEzARBgNVBAoTCnl6aG9yZy5uZXQxFjAUBgNVBAMTDWNhLnl6aG9y\n"
					+ "Zy5uZXQwHhcNMTcxMDMwMDkzMDUyWhcNMjcxMDI4MDkzMDUyWjBVMQswCQYDVQQG\n"
					+ "EwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZyYW5jaXNj\n"
					+ "bzEZMBcGA1UEAwwQQWRtaW5AeXpob3JnLm5ldDBZMBMGByqGSM49AgEGCCqGSM49\n"
					+ "AwEHA0IABKe9128MOBsFxz2qrJLthoK2kwKk6JzI4I7OD/VXueubMln8sMWPXB5K\n"
					+ "JVnTDiRxo5Z9KCGCVvHSEKlMk3oaaMqjTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNV\n"
					+ "HRMBAf8EAjAAMCsGA1UdIwQkMCKAIMZZP3OASvHSIo97UiMfAMYIYOiNobLvxmlr\n"
					+ "8KGz4fYzMAoGCCqGSM49BAMCA0cAMEQCIHAN7nlVUmjAzBFyM/wrfTXVncMR4dNh\n"
					+ "FMWq2aKn3jhNAiBP4W8zmQimltcF8rvhuJ5yDX0LnkLP0PU9Djm8urhTlA==\n" 
					+ "-----END CERTIFICATE-----";
			PEMParser pemParser = new PEMParser(new StringReader(privateKey));
			Object object = pemParser.readObject();
		    pemParser.close();
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		    PrivateKey privkey = converter.getPrivateKey((PrivateKeyInfo) object);
		    
			
			UserWallet.add(
					new ComposerUser("Admin@yzhorg.net", "YzhMSP", "a", 
							privkey, cert));
			IS_ENROLLED = true;
		}
		this.events = new LinkedBlockingQueue<>();
		this.connection = ComposerDriverManager.connect(connStr, UserWallet, "Admin@yzhorg.net", "a");
		this.connection.on(es -> {
			ArrayList<Event> res = new ArrayList<>();
			synchronized (EtpccConnection.class) {
				for (Event e : es) {
					if (!receivedEventIds.contains(e.eventId)) {
						receivedEventIds.add(e.eventId);
						res.add(e);
					}
				}
			}
			if (res.size() > 0) {
				events.offer(res.toArray(new Event[res.size()]));
			}
		});
	}

	public Event[] getEvent() throws ComposerException {
		try {
			return events.poll(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new ComposerException(ComposerException.APPLICATION_ERROR, "Cannot retrieve event message", e);
		}
	}

	public void submitTransaction(Object tx, Counter counter) throws ComposerException {
		// Data_add_Warehouse tx = new Data_add_Warehouse();
		// tx.recordNum = 2;
		try {
			connection.submitTransaction(tx);
		} catch (ComposerException e) {
			counter.failTxNum().getAndAdd(1);
			if (transactionConflict.matcher(e.getMessage()).find()) {
				System.err.println("mvcc read conflict");
			} else {
				throw e;
			}
		} finally {
			if (!(tx instanceof BranchTransaction)) {
				counter.txNum().getAndAdd(1);
			}
		}
	}

	public int getId() {
		return id;
	}

	public String getRole() {
		return role;
	}
}
