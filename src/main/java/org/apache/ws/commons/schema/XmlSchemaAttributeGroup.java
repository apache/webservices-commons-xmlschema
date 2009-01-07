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

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.apache.ws.commons.schema.utils.XmlSchemaNamedImpl;

/**
 * Class for attribute groups. Groups a set of attribute declarations so that 
 * they can be incorporated as a
 * group into complex type definitions. Represents the World Wide Web 
 * consortium (W3C) attributeGroup element when it does <i>not</i> have a 'ref='
 * attribute. 
 */

public class XmlSchemaAttributeGroup extends XmlSchemaAnnotated implements XmlSchemaNamed {
    private XmlSchemaAnyAttribute anyAttribute;
    private XmlSchemaObjectCollection attributes;
    private XmlSchemaNamedImpl namedDelegate;

    /**
     * Creates new XmlSchemaAttributeGroup
     */
    public XmlSchemaAttributeGroup(XmlSchema parent) {
        namedDelegate = new XmlSchemaNamedImpl(parent, true);
        attributes = new XmlSchemaObjectCollection();
    }

    public XmlSchemaAnyAttribute getAnyAttribute() {
        return this.anyAttribute;
    }

    public void setAnyAttribute(XmlSchemaAnyAttribute anyAttribute) {
        this.anyAttribute = anyAttribute;
    }

    public XmlSchemaObjectCollection getAttributes() {
        return this.attributes;
    }

    public void setAttributes(XmlSchemaObjectCollection attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return namedDelegate.getName();
    }

    public XmlSchema getParent() {
        return namedDelegate.getParent();
    }

    public QName getQName() {
        return namedDelegate.getQName();
    }

    public boolean isAnonymous() {
        return namedDelegate.isAnonymous();
    }

    public boolean isTopLevel() {
        return namedDelegate.isTopLevel();
    }

    public void setName(String name) {
        namedDelegate.setName(name);
    }
}
