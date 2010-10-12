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

import org.apache.ws.commons.schema.utils.XmlSchemaNamedWithForm;
import org.apache.ws.commons.schema.utils.XmlSchemaNamedWithFormImpl;
import org.apache.ws.commons.schema.utils.XmlSchemaRef;

/**
 * Class for attribute types. Represents the World Wide Web Consortium (W3C) attribute element.
 *
 * ref= attributes are in the parent items collection, not in the map of named items.
 */
public class XmlSchemaAttribute extends XmlSchemaAttributeOrGroupRef implements XmlSchemaNamedWithForm,
    XmlSchemaAttributeGroupMember {

    private String defaultValue;
    private String fixedValue;
    private XmlSchemaSimpleType schemaType;
    private QName schemaTypeName;
    private XmlSchemaUse use;
    private XmlSchemaNamedWithFormImpl namedDelegate;
    private XmlSchemaRef<XmlSchemaAttribute> ref;

    /**
     * Create a new attribute.
     * @param schema containing scheme.
     * @param topLevel true if a global attribute.
     */
    public XmlSchemaAttribute(XmlSchema schema, boolean topLevel) {
        namedDelegate = new XmlSchemaNamedWithFormImpl(schema, topLevel, false);
        ref = new XmlSchemaRef<XmlSchemaAttribute>(schema, XmlSchemaAttribute.class);
        namedDelegate.setRefObject(ref);
        ref.setNamedObject(namedDelegate);
        use = XmlSchemaUse.NONE;
        if (topLevel) {
            schema.getItems().add(this);
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public XmlSchemaRef<XmlSchemaAttribute> getRef() {
        return ref;
    }

    public XmlSchemaSimpleType getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(XmlSchemaSimpleType schemaType) {
        this.schemaType = schemaType;
    }

    public QName getSchemaTypeName() {
        return schemaTypeName;
    }

    public void setSchemaTypeName(QName schemaTypeName) {
        this.schemaTypeName = schemaTypeName;
    }

    public XmlSchemaUse getUse() {
        return use;
    }

    public void setUse(XmlSchemaUse use) {
        if (namedDelegate.isTopLevel() && use != null) {
            throw new XmlSchemaException("Top-level attributes may not have a 'use'");
        }
        this.use = use;
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

        if (namedDelegate.isTopLevel() && namedDelegate.getName() != null) {
            namedDelegate.getParent().getAttributes().remove(getQName());
        }
        namedDelegate.setName(name);
        if (namedDelegate.isTopLevel()) {
            if (name == null) {
                throw new XmlSchemaException("Top-level attributes may not be anonymous");
            }
            namedDelegate.getParent().getAttributes().put(getQName(), this);
        }
    }

    public boolean isFormSpecified() {
        return namedDelegate.isFormSpecified();
    }

    public XmlSchemaForm getForm() {
        return namedDelegate.getForm();
    }

    public void setForm(XmlSchemaForm form) {
        if (namedDelegate.isTopLevel() && form != XmlSchemaForm.NONE) {
            throw new XmlSchemaException("Top-level attributes may not have a 'form'");
        }
        namedDelegate.setForm(form);
    }

    public QName getWireName() {
        return namedDelegate.getWireName();
    }
}
