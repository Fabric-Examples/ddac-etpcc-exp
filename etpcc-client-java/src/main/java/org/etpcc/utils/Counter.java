package org.etpcc.utils;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

import java.time.LocalDateTime;

import com.google.common.util.concurrent.AtomicDouble;

public class Counter {
	private AtomicDouble txNum, stockLevelNum, deliveryNum, orderStatusNum,
			paymentNum, newOrderNum, failTxNum;

	public Counter() {
		this.txNum = new AtomicDouble();
		this.stockLevelNum = new AtomicDouble();
		this.deliveryNum = new AtomicDouble();
		this.orderStatusNum = new AtomicDouble();
		this.paymentNum = new AtomicDouble();
		this.newOrderNum = new AtomicDouble();
		this.failTxNum = new AtomicDouble();
	}

	public Counter txNum(double txNum) {
		this.txNum.getAndSet(txNum);
		return this;
	}

	public Counter stockLevelNum(double stockLevelNum) {
		this.stockLevelNum.getAndSet(stockLevelNum);
		return this;
	}

	public Counter deliveryNum(double deliveryNum) {
		this.deliveryNum.getAndSet(deliveryNum);
		return this;
	}

	public Counter orderStatusNum(double orderStatusNum) {
		this.orderStatusNum.getAndSet(orderStatusNum);
		return this;
	}

	public Counter paymentNum(double paymentNum) {
		this.paymentNum.getAndSet(paymentNum);
		return this;
	}

	public Counter newOrderNum(double newOrderNum) {
		this.newOrderNum.getAndSet(newOrderNum);
		return this;
	}

	public AtomicDouble txNum() {
		return txNum;
	}

	public AtomicDouble stockLevelNum() {
		return stockLevelNum;
	}

	public AtomicDouble deliveryNum() {
		return deliveryNum;
	}

	public AtomicDouble orderStatusNum() {
		return orderStatusNum;
	}

	public AtomicDouble paymentNum() {
		return paymentNum;
	}

	public AtomicDouble newOrderNum() {
		return newOrderNum;
	}

	public AtomicDouble failTxNum() {
		return failTxNum;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(LocalDateTime.now().format(ISO_LOCAL_TIME)).append("-----------------------").append('\n');
		b.append("txNum\tfailTxNum\tstockLevelNum\tdeliveryNum\torderStatusNum\tpaymentNum\tnewOrderNum").append('\n');
		b.append(txNum.get()).append("\t").append(failTxNum.get()).append("\t").append(stockLevelNum.get()).append("\t").append(deliveryNum.get()).append("\t")
				.append(orderStatusNum.get()).append("\t").append(paymentNum.get()).append("\t").append(newOrderNum.get()).append('\n');
		return b.toString();
	}
}
