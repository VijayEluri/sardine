package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import com.googlecode.sardine.util.SardineException;

/**
 * Simple class for making move a bit easier to deal with.
 */
class HttpMove extends HttpEntityEnclosingRequestBase {
    public HttpMove(String sourceUrl, String destinationUrl) throws SardineException {
        super();
        HttpClientUtils.checkConsistentSlashes(sourceUrl, destinationUrl);
        this.setHeader("Destination", destinationUrl);
        this.setHeader("Overwrite", "T");
        this.setURI(URI.create(sourceUrl));
    }

    @Override
    public String getMethod() {
        return "MOVE";
    }
}
