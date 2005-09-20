/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.ws.commons.schema;

/**
 * Class for defining maxExclusive facets. Represents the World Wide
 * Web Consortium (W3C) maxExclusive facet.
 *
 * @author mukund
 */

// Vidyanand - 16th Oct - initial implementation

public class XmlSchemaMaxExclusiveFacet extends XmlSchemaFacet {

    /**
     * Creates new XmlSchemaMaxExclusiveFacet
     */
    public XmlSchemaMaxExclusiveFacet() {
    }

    public XmlSchemaMaxExclusiveFacet(Object value, boolean fixed) {
        super(value, fixed);
    }
}
