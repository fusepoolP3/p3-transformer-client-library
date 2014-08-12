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

import eu.fusepool.p3.transformer.client.Transformer;
import eu.fusepool.p3.transformer.client.TransformerClientImpl;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.transformer.commons.util.WritingEntity;
import eu.fusepool.transformer.sample.LongRunningTransformer;
import eu.fusepool.transformer.sample.SimpleTransformer;
import eu.fusepool.transformer.server.TransformerServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Reto
 */
public class ASyncTramsformerTest {

    String baseURI;
    
    @Before
    public void setUp() throws Exception {
        final int port = findFreePort();
        baseURI = "http://localhost:"+port+"/";
        TransformerServer server = new TransformerServer(port);
        server.start(new LongRunningTransformer());
    }

    @Test
    public void textPlainAccepted() throws MimeTypeParseException {
        Transformer t = new TransformerClientImpl(baseURI);
        Assert.assertTrue("text/plain is not accepted", t.accepts(new MimeType("text/plain")));
    }
    
    @Test
    public void turtleAsOutputFormat() throws MimeTypeParseException {
        Transformer t = new TransformerClientImpl(baseURI);
        Set<MimeType> types = t.getSupportedOutputFormats();
        Assert.assertTrue("No supported Output format", types.size() > 0);
        boolean turtleFound = false;
        for (MimeType mimeType : types) {
            if (turtle.match(mimeType)) {
                turtleFound = true;
            }
        }
        Assert.assertTrue("None of the supported output formats is turtle", turtleFound);
    }
    private static MimeType turtle;
    static {
        try {
            turtle = new MimeType("text/turtle");
        } catch (MimeTypeParseException ex) {
            Logger.getLogger(ASyncTramsformerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    @Test
    public void transform() throws MimeTypeParseException {
        Transformer t = new TransformerClientImpl(baseURI);
        Entity result = t.transform(new WritingEntity() {

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
        }, new MimeType("text/turtle"));
        Assert.assertTrue("Result not turtle", turtle.match(result.getType()));
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
