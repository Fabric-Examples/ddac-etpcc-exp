package org.etpcc.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ConnectionProfileLocal {
	static final private Map<String, Pair<String, String>> peerInfo;

	static {
		peerInfo = new HashMap<>();
		for (int i = 1; i <= 30; i++) {
			peerInfo.put("peer" + i, Pair.of(String.valueOf(31000 + i),
					String.valueOf(32000 + i)));
		}
	}

	static public String toConnectionString(String peer) {
		String connectionString = null;
		Pair<String, String> ports = peerInfo.get(peer);
		if (ports == null) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
		int serverId = ThreadLocalRandom.current().nextInt(2, 10);
		try {
			connectionString = mapper.writeValueAsString(new ConnectionOptions()
					.addOrderer("orderer", "grpc://exp2.sl.cloud9.ibm.com:30002")
					.addPeer(peer, "grpc://exp" + serverId + ".sl.cloud9.ibm.com:" + ports.getLeft())
					.addEventHub(peer, "grpc://exp" + serverId + ".sl.cloud9.ibm.com:" + ports.getRight())
					.ca("http://exp2.sl.cloud9.ibm.com:30001")
					.channel("yzhchannel").chaincodeId("etpcc").mspId("YzhMSP").invokeWaitMillis(6000 * 1000));
			connectionString = ((ObjectNode) new ObjectMapper().readTree(connectionString)).put("protocol", "fabric.v1")
					.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connectionString;
	}
}