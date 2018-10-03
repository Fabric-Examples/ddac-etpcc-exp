package org.etpcc.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hyperledger.composer.client.ComposerUser;
import org.hyperledger.composer.client.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ETPCCWallet implements Wallet<ComposerUser> {

	private final Map<String, ComposerUser> map = new HashMap<>();
	private final Logger logger = LoggerFactory.getLogger(ETPCCWallet.class);

	@Override
	public List<ComposerUser> list() {
		List<ComposerUser> result = new LinkedList<>();
		result.addAll(map.values());
		return result;
	}

	@Override
	public boolean contains(String id) {
		return map.containsKey(id);
	}

	@Override
	public ComposerUser get(String id) {
		return map.get(id);
	}

	@Override
	public ComposerUser add(ComposerUser value) {
		ComposerUser put = map.put(value.getId(), value);
		log();
		return put;
	}

	@Override
	public ComposerUser update(ComposerUser value) {
		map.put(value.getId(), value);
		log();
		return value;
	}

	@Override
	public ComposerUser remove(String id) {
		ComposerUser remove = map.remove(id);
		log();
		return remove;
	}

	private void log() {
		logger.info("wallet: {}", map);
	}
}
