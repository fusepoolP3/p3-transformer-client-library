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
package eu.fusepool.p3.transformer.client;

import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.transformer.commons.util.InputStreamEntity;
import eu.fusepool.p3.vocab.TRANSFORMER;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.commons.io.IOUtils;

/**
 * An implementation of {@link Transformer} to access a Transformer identified 
 * by a URI
 * @author Gabor, reto
 */
public class TransformerClientImpl implements Transformer {

    final private URI uri;
    final private Set<MimeType> supportedInputFormats;
    final private Set<MimeType> supportedOutputFormats;

    public TransformerClientImpl(URI _uri) {
        uri = _uri;
        supportedInputFormats = new HashSet<>();
        supportedOutputFormats = new HashSet<>();
        setMimeTypes();
    }

    public TransformerClientImpl(String uriString) {
        try {
            uri = new URI(uriString);
            supportedInputFormats = new HashSet<>();
            supportedOutputFormats = new HashSet<>();
            setMimeTypes();
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax error!", e);
        }
    }

    private void setMimeTypes() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/turtle");

            final Parser parser = Parser.getInstance();
            final UriRef transformerRes = new UriRef(uri.toString());
            final Graph graph = parser.parse(connection.getInputStream(), "text/turtle", transformerRes);
            final GraphNode transformerNode = new GraphNode(transformerRes, graph);
            {
                final Iterator<Literal> sifIter = transformerNode.getLiterals(TRANSFORMER.supportedInputFormat);
                while (sifIter.hasNext()) {
                    Literal lit = sifIter.next();
                    try {
                        supportedInputFormats.add(new MimeType(lit.getLexicalForm()));
                    } catch (MimeTypeParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            {
                final Iterator<Literal> sofIter = transformerNode.getLiterals(TRANSFORMER.supportedOutputFormat);
                while (sofIter.hasNext()) {
                    Literal lit = sofIter.next();
                    try {
                        supportedOutputFormats.add(new MimeType(lit.getLexicalForm()));
                    } catch (MimeTypeParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot establish connection to " + uri.toString() + " !", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public Entity transform(Entity entity, MimeType... acceptedFormats) {
        HttpURLConnection connection = null;
        try {
            final URL transfromerUrl = uri.toURL();
            connection = (HttpURLConnection) transfromerUrl.openConnection();
            connection.setRequestMethod("POST");
            String acceptHeaderValue = null;
            if (acceptedFormats.length > 0) {
                final StringWriter acceptString = new StringWriter();
                double q = 1;
                for (MimeType mimeType : acceptedFormats) {
                    acceptString.write(mimeType.toString());
                    acceptString.write("; q=");
                    acceptString.write(Double.toString(q));
                    q = q * 0.9;
                    acceptString.write(", ");
                }
                acceptHeaderValue = acceptString.toString();
                connection.setRequestProperty("Accept", acceptHeaderValue);
            }

            connection.setRequestProperty("Content-Type", entity.getType().toString());
            if (entity.getContentLocation() != null) {
                connection.setRequestProperty("Content-Location", entity.getContentLocation().toString());
            }

            connection.setDoOutput(true);
            connection.setUseCaches(false);
            try (OutputStream out = connection.getOutputStream()) {
                entity.writeData(out);
            }
            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return getResponseEntity(connection);
            }
            if (responseCode == 202) {
                final String location = connection.getHeaderField("Location");
                if (location == null) {
                    throw new RuntimeException("No location header in firts 202 response");
                }
                return getAsyncResponseEntity(new URL(transfromerUrl, location), acceptHeaderValue);
            }
            throw new RuntimeException("Unexpected response code: " + responseCode);

        } catch (IOException e) {
            throw new RuntimeException("Cannot establish connection to " + uri.toString() + " !", e);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException("Error parsing MediaType returned from Server. ", ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public boolean accepts(MimeType type) {
        for (MimeType m : supportedInputFormats) {
            if ((m.match(type)) || m.getPrimaryType().equals("*")
                    || (m.getSubType().equals("*")
                    && m.getPrimaryType().equals(type.getPrimaryType()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<MimeType> getSupportedInputFormats() {
        return supportedInputFormats;
    }

    @Override
    public Set<MimeType> getSupportedOutputFormats() {
        return supportedOutputFormats;
    }

    private Entity getResponseEntity(HttpURLConnection connection) throws IOException, MimeTypeParseException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        IOUtils.copy(connection.getInputStream(), baos);

        final byte[] bytes = baos.toByteArray();

        final String resultContentTypeString = connection.getHeaderField("Content-Type");
        final MimeType resultType = resultContentTypeString != null
                ? new MimeType(resultContentTypeString) : new MimeType("application", "octet-stream");
        return new InputStreamEntity() {

            @Override
            public MimeType getType() {
                return resultType;
            }

            @Override
            public InputStream getData() throws IOException {
                return new ByteArrayInputStream(bytes);
            }
        };
    }

    private Entity getAsyncResponseEntity(URL url, String acceptHeaderValue) {
        //recursive function would be nicer, but this saves memory
        while (true) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                if (acceptHeaderValue != null) {
                    connection.setRequestProperty("Accept", acceptHeaderValue);
                }

                final int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    return getResponseEntity(connection);
                }
                if (responseCode != 202) {
                    throw new RuntimeException("Unexpected response code: " + responseCode);
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot establish connection to " + uri.toString() + " !", e);
            } catch (MimeTypeParseException ex) {
                throw new RuntimeException("Error parsing MediaType returned from Server. ", ex);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

}
