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
import java.util.Set;
import javax.activation.MimeType;

/**
 * Represents a Transformer. 
 * For more information about transformers, see: https://github.com/fusepoolP3/overall-architecture/blob/master/data-transformer-api.md
 * 
 * @author reto
 */
public interface Transformer {
    
    /**
     * @return The Media Types supported for the entity to be transformed
     */
    Set<MimeType> getSupportedInputFormats();

    /**
     * @return The Media types that the transformer can produce as output
     */
    Set<MimeType> getSupportedOutputFormats();     

    /**
     * This is a convenience method that checks the supported input formats 
     * if one of them is same as or more generic than a type specified as argument.
     * 
     * @param type the Media Type to check for acceptance
     * @return true if type can be accepted, false otherwise
     */
    boolean accepts(MimeType type);
    
    /**
     * Transforms an entity. This method supports specifying accepted
     * formats.
     * @param entity the Entity to be transformer
     * @param acceptedFormats the accepted result formats in order of preference
     * @return the transformed Entity
     */
    Entity transform(Entity entity, MimeType... acceptedFormats);
    
}
