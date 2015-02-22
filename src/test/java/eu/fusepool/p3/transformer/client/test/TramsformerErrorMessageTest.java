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
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.transformer.client.Transformer;
import eu.fusepool.p3.transformer.client.UnexpectedResponseException;
import eu.fusepool.p3.transformer.client.TransformerClientImpl;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.transformer.commons.util.WritingEntity;
import eu.fusepool.p3.transformer.sample.SimpleTransformer;
import eu.fusepool.p3.transformer.server.TransformerServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Reto
 */
public class TramsformerErrorMessageTest {

    String baseURI;
    
    final String responseBody = "Seerver too lazy to process";
    
    @Before
    public void setUp() throws Exception {
        final int port = findFreePort();
        baseURI = "http://localhost:"+port+"/";
        TransformerServer server = new TransformerServer(port, false);
        server.start(new SimpleTransformer() {

            @Override
            protected TripleCollection generateRdf(HttpRequestEntity entity) throws IOException {
                throw new TransformerException(500, responseBody);
            }
            
        });
    }

    
    
    @Test
    public void transform() throws MimeTypeParseException, IOException {
        Transformer t = new TransformerClientImpl(baseURI);
        boolean gotRightException = false;
        final WritingEntity entity = new WritingEntity() {
            
            @Override
            public MimeType getType() {
                try {
                    return new MimeType("text/plain");
                } catch (MimeTypeParseException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void writeData(OutputStream out) throws IOException {
                out.write("Hello".getBytes("utf-8"));
            }
        };
        try {
            Entity result = t.transform(entity, new MimeType("text/turtle"));
        } catch (UnexpectedResponseException ex) {
            gotRightException = true;
            Entity responseEntity = ex.getResponseEntity();
            Assert.assertNotNull(responseEntity);
            ByteArrayOutputStream responseWriter = new ByteArrayOutputStream();
            responseEntity.writeData(responseWriter);
            Assert.assertArrayEquals(responseBody.getBytes("utf-8"), responseWriter.toByteArray());            
        }
        Assert.assertTrue("Didn't get TransformerClientException", gotRightException);
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
