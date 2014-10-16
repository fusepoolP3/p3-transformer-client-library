/*
 * Copyright (C) 2014 Bern University of Applied Sciences.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.fusepool.p3.transformer.client.test;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.client.Transformer;
import eu.fusepool.p3.transformer.client.TransformerClientImpl;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.transformer.commons.util.WritingEntity;
import eu.fusepool.p3.transformer.server.TransformerServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Reto
 */
public class ParametersForwardingTest {

    String baseURI;
    
    @Before
    public void setUp() throws Exception {
        
        
    }

    @Test
    public synchronized void parameterTest() throws Exception {
        final int port = findFreePort();
        baseURI = "http://localhost:"+port+"/";
        TransformerServer server = new TransformerServer(port);
        //enclosures Java Style
        final String[] requestUri = new String[1];
        final String[] queryString = new String[1];
        server.start(new SyncTransformer() {

            @Override
            public Entity transform(HttpRequestEntity entity) throws IOException {
                requestUri[0] = entity.getRequest().getRequestURI();
                queryString[0] = entity.getRequest().getQueryString();
                return entity;
            }

            @Override
            public boolean isLongRunning() {
                return false;
            }

            @Override
            public Set<MimeType> getSupportedInputFormats() {
                try {
                    return Collections.singleton(new MimeType("fancy", "type"));
                } catch (MimeTypeParseException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public Set<MimeType> getSupportedOutputFormats() {
                try {
                    return Collections.singleton(new MimeType("fancy", "type"));
                } catch (MimeTypeParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        Transformer t = new TransformerClientImpl(baseURI+"test?foo=bar");
        t.transform(new WritingEntity() {

            @Override
            public MimeType getType() {
                try {
                    return new MimeType("fancy/type");
                } catch (MimeTypeParseException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void writeData(OutputStream out) throws IOException {
                out.write("Hello".getBytes("utf-8"));
            }
        }, new MimeType("fancy/type"));
        Assert.assertTrue("Got request uri path", requestUri[0].endsWith("test"));
        Assert.assertTrue("Got query string", queryString[0].endsWith("foo=bar"));
    }
    

    public static int findFreePort() {
        int port = 0;
        try (ServerSocket server = new ServerSocket(0);) {
            port = server.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException("unable to find a free port");
        }
        return port;
    }

}
