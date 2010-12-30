/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

/**
 * Holds static test helper methods.
 * 
 * @author mirko
 */
public final class TUtils {

    private TUtils() {
        // Do not instantiate as all methods are static.
    }

    public static void setHttpClientLogging() {
        if (System.getProperty("sardine.log-httpclient") != null) {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "INFO");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
        }
    }
}
