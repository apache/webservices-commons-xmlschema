/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ws.commons.schema;

/**
 * An abstract class. Provides information about the included schema.
 */

public abstract class XmlSchemaExternal extends XmlSchemaAnnotated {

    XmlSchema schema;
    String schemaLocation;

    /**
     * Creates new XmlSchemaExternal
     */
    protected XmlSchemaExternal(XmlSchema parent) {
        parent.getExternals().add(this);
        parent.getItems().add(this);
    }

    public XmlSchema getSchema() {
        return schema;
    }
    public void setSchema(XmlSchema sc) {
        schema = sc;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }
}
