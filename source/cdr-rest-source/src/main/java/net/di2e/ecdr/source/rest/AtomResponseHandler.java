package net.di2e.ecdr.source.rest;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.StatusLine;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public class AtomResponseHandler implements ResponseHandler<InputStream> {

    @Override
    public InputStream handleResponse( HttpResponse response ) throws ClientProtocolException, IOException {
        StatusLine statusLine = response.getStatusLine();
        if ( statusLine.getStatusCode() != 200 ) {
            EntityUtils.consume( response.getEntity() );
            throw new HttpResponseException( statusLine.getStatusCode(), statusLine.getReasonPhrase() );
        }
        return response.getEntity().getContent();
    }

}
