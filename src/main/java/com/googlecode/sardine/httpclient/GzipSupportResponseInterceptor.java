package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * This {@link HttpResponseInterceptor} decompresses the {@link HttpEntity} of the {@link HttpResponse} on the fly when
 * the content encoding indicates it should by replacing the entity of the response.
 */
final class GzipSupportResponseInterceptor implements HttpResponseInterceptor {
    public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            final Header ceheader = entity.getContentEncoding();
            if (ceheader != null) {
                HeaderElement[] codecs = ceheader.getElements();
                for (final HeaderElement codec : codecs) {
                    if (codec.getName().equalsIgnoreCase("gzip")) {
                        response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                        return;
                    }
                }
            }
        }
    }
}
