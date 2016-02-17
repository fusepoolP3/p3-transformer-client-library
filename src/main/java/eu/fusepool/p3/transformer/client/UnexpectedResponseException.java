/*
 * Copyright 2015 developer.
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

/**
 * This Exception is thrown when the client receives a response  with an
 * unexpected status code.
 * 
 * @author reto
 */
public class UnexpectedResponseException extends RuntimeException {
    private final Entity responseEntity;

    /**
     * Constructs an instance of <code>TransfomerClientException</code> with the
     * specified detail message.
     *
     * @param responseCode the HTTP status code of the response
     * @param entity the response entity
     */
    public UnexpectedResponseException(int responseCode, Entity entity) {
        super("Unexpected response code: " + responseCode);
        this.responseEntity = entity;
    }
    
    public Entity getResponseEntity() {
        return responseEntity;
    }
}
