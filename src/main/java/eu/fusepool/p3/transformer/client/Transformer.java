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
 *
 * @author reto
 */
public interface Transformer {
    
    Set<MimeType> getSupportedInputFormats();

    Set<MimeType> getSupportedOutputFormats();     

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
