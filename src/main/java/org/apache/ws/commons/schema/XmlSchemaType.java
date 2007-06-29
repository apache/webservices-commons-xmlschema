/*
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

import org.apache.ws.commons.schema.constants.Constants;

import javax.xml.namespace.QName;


/**
 * The base class for all simple types and complex types.
 */

public class XmlSchemaType extends XmlSchemaAnnotated {

    Object baseSchemaType;
    XmlSchemaDatatype dataType;
    XmlSchemaDerivationMethod deriveBy, finalDerivation, finalResolved;
    boolean isMixed;

    // name of the type
    String name;

    XmlSchema schema;

    /**
     * Creates new XmlSchemaType
     */
    public XmlSchemaType(XmlSchema schema) {
        this.schema = schema;
        finalDerivation = new XmlSchemaDerivationMethod(Constants.BlockConstants.NONE);
    }

    public Object getBaseSchemaType() {
        return baseSchemaType;
    }

    public XmlSchemaDatatype getDataType() {
        return dataType;
    }

    public XmlSchemaDerivationMethod getDeriveBy() {
        return deriveBy;
    }

    public XmlSchemaDerivationMethod getFinal() {
        return finalDerivation;
    }

    public void setFinal(XmlSchemaDerivationMethod finalDerivation) {
        this.finalDerivation = finalDerivation;
    }

    public XmlSchemaDerivationMethod getFinalResolved() {
        return finalResolved;
    }

    public boolean isMixed() {
        return isMixed;
    }

    public void setMixed(boolean isMixed) {
        this.isMixed = isMixed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QName getQName() {
        if(name == null) {
            return null;
        }
        return new QName(schema.logicalTargetNamespace, name);
    }
}
