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
package org.apache.ws.commons.schema.utils;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;

/**
 * A named item. Named items have two names; their schema-local name (a string)
 * and a QName qualified by their schema's target namespace. Note that the qualified
 * name is <i>not</i> the on-the-wire name for attributes and elements. Those
 * names depend on the form, and are managed by {@link XmlSchemaNamedWithForm}.
 */
public interface XmlSchemaNamed extends XmlSchemaObjectBase {

    /**
     * Retrieve the name.
     * @return
     */
    String getName();

    boolean isAnonymous();

    /**
     * Set the name. Set to null to render the object anonymous.
     * @param name
     */
    void setName(String name);

    /**
     * Retrieve the parent schema.
     * @return
     */
    XmlSchema getParent();

    QName getQName();

    boolean isTopLevel();

}
