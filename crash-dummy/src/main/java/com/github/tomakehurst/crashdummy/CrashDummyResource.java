package com.github.tomakehurst.crashdummy;


import com.codahale.metrics.annotation.Timed;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Path("/")
public class CrashDummyResource {

    private static final Logger log = LoggerFactory.getLogger(CrashDummyResource.class);

    private final HttpClient longTimeoutHhttpClient;
    private final HttpClient shortTimeoutHttpClient;
    private final String wireMockHost;
    private final WireMock wireMock;

    public CrashDummyResource(HttpClient longTimeoutHhttpClient, HttpClient shortTimeoutHttpClient, WireMock wireMock, String wireMockHost) {
        this.longTimeoutHhttpClient = longTimeoutHhttpClient;
        this.shortTimeoutHttpClient = shortTimeoutHttpClient;
        this.wireMockHost = wireMockHost;

        this.wireMock = wireMock;
    }

    @GET
    public String root() {
        return "Uh-oh...";
    }

    @GET
    @Timed(name = "webresources.some-text.timer", absolute = true)
    @Path("some-text")
    public Response httpClientWithLongConnectTimeout() {
        HttpGet get = getSomething();
        HttpResponse response;
        try {
            response = longTimeoutHhttpClient.execute(get);
            return Response.ok(EntityUtils.toString(response.getEntity())).build();
        } catch (IOException ioe) {
            log.error("Failed to GET " + get.getURI(), ioe);
            return Response.serverError().entity(renderFailureMessage(ioe)).build();
        } finally {
            get.releaseConnection();
        }
    }

    @GET
    @Timed(name = "webresources.bad-http-client-error-handling.timer", absolute = true)
    @Path("bad-http-client-error-handling")
    public Response httpClientWithBadErrorHandling() {
        HttpGet get = getSomething();
        HttpResponse response;
        try {
            response = shortTimeoutHttpClient.execute(get);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Text snippet service returned status code " + response.getStatusLine().getStatusCode());
            }
            String result = EntityUtils.toString(response.getEntity());
            get.releaseConnection();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError().entity(renderFailureMessage(e)).build();
        }
    }

    private String renderFailureMessage(Exception ioe) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        ioe.printStackTrace(pw);
        return "Failure:\n" + stringWriter.toString();
    }

    private HttpGet getSomething() {
        return new HttpGet("http://" + wireMockHost + ":8080/something");
    }

}
