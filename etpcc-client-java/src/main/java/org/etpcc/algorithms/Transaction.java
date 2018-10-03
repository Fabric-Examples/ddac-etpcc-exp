package org.etpcc.algorithms;

import java.util.HashSet;

/**
 * Created by liangjiao on 17/10/26.
 */
public class Transaction {
    private String name;
    private HashSet<String> writeSet = new HashSet<>();
    private HashSet<String> ReadWriteSet = new HashSet<>();

    public Transaction(String name) {
        this.name = name;
        if (name.equals("norder")) { //rw
            writeSet.add("District");
            writeSet.add("Stock");
            writeSet.add("New_Order");
            writeSet.add("Order");
            writeSet.add("Order_Line");
            ReadWriteSet.addAll(getWriteSet());
            ReadWriteSet.add("Warehouse");
            ReadWriteSet.add("Customer");
            ReadWriteSet.add("Item");
        } else if (name.equals("pay")) {
            writeSet.add("Warehouse");
            writeSet.add("District");
            writeSet.add("Customer");
            writeSet.add("History");
            writeSet.add("Order");
            ReadWriteSet.addAll(getWriteSet());
        } else if (name.equals("deliver")) {
            writeSet.add("Customer");
            writeSet.add("New_Order");
            writeSet.add("Order_Line");
            ReadWriteSet.addAll(getWriteSet());
        } else if(name.equals("orders")){
            ReadWriteSet.add("Customer");
            ReadWriteSet.add("Order");
            ReadWriteSet.add("Order_Line");
        } else if(name.equals("stockl")){
            ReadWriteSet.add("Warehouse");
            ReadWriteSet.add("District");
            ReadWriteSet.add("Stock");
            ReadWriteSet.add("Order_Line");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<String> getWriteSet() {
        return writeSet;
    }

    public void setWriteSet(HashSet<String> writeSet) {
        this.writeSet = writeSet;
    }

    public HashSet<String> getReadWriteSet() {
        return ReadWriteSet;
    }

    public void setReadWriteSet(HashSet<String> readWriteSet) {
        this.ReadWriteSet = readWriteSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
