package org.etpcc.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConnectionProfile {
	static final private Map<String, Pair<String, String>> peerInfo;

	static {
		peerInfo = new HashMap<>();
		for (int i = 1; i <= 30; i++) {
			peerInfo.put("peer" + i, Pair.of(String.valueOf(7051),
					String.valueOf(7053)));
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
		String chaincode = System.getProperty("chainname", "etpcc");
		try {
			connectionString = mapper.writeValueAsString(new ConnectionOptions()
					.addOrderer("orderer", "grpc://orderer:7050")
					.addPeer(peer, "grpc://" + peer + ":" + ports.getLeft())
					.addEventHub(peer, "grpc://" + peer + ":" + ports.getRight())
					.ca("http://ca:7054")
					.channel("yzhchannel").chaincodeId(chaincode).mspId("YzhMSP").invokeWaitMillis(6000 * 1000));
			connectionString = ((ObjectNode) new ObjectMapper().readTree(connectionString)).put("protocol", "fabric.v1")
					.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connectionString;
	}
}