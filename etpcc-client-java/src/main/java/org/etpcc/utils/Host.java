package org.etpcc.utils;

import java.io.Serializable;
import java.util.Properties;

class Host implements Serializable {
    public String name;
    public String url;
    public Properties properties;

	// for json construct
	public Host() {}

    Host(String name, String url, Properties properties) {
	    this();
        this.name = name;
        this.url = url;
        this.properties = properties;
    }

    @Override
    public String toString() {
        return '{' + name + ':' + url + '}';
    }
}
