/*
 * Copyright 2004,2007 The Apache Software Foundation.
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
 * Class for attribute groups. Groups a set of attribute declarations
 * so that they can be incorporated as a group into complex type
 * definitions. Represents the World Wide Web Consortium (W3C)
 * attributeGroup element.
 */

public class XmlSchemaAttributeGroup extends XmlSchemaAnnotated {

    /**
     * Creates new XmlSchemaAttributeGroup
     */
    public XmlSchemaAttributeGroup() {
        attributes = new XmlSchemaObjectCollection();
    }

    XmlSchemaAnyAttribute anyAttribute;

    public XmlSchemaAnyAttribute getAnyAttribute() {
        return this.anyAttribute;
    }

    public void setAnyAttribute(XmlSchemaAnyAttribute anyAttribute) {
        this.anyAttribute = anyAttribute;
    }

    XmlSchemaObjectCollection attributes;

    public XmlSchemaObjectCollection getAttributes() {
        return this.attributes;
    }

    public void setAttributes(XmlSchemaObjectCollection attributes) {
        this.attributes = attributes;
    }

    String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
