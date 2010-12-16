package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import com.googlecode.sardine.util.SardineException;

/**
 * Simple class for making move a bit easier to deal with.
 */
public class HttpMove extends HttpEntityEnclosingRequestBase {
    public HttpMove(String sourceUrl, String destinationUrl) throws SardineException {
        super();
        this.setHeader("Destination", destinationUrl);
        this.setHeader("Overwrite", "T");
        this.setURI(URI.create(sourceUrl));

        if (sourceUrl.endsWith("/") && !destinationUrl.endsWith("/"))
            throw new SardineException("Destinationurl must end with a /", destinationUrl);
    }

    @Override
    public String getMethod() {
        return "MOVE";
    }
}
