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

/**
 * Common base class of all of the named objects in the XML Schema model.
 * Note that several of these come in both 'named' and anonymous flavors.
 * This class provides an API that keeps track.
 *
 * By definition, all of these objects live in some particular (parent)
 * schema.
 *
 * The parent is intentionally immutable; there's no good reason to move
 * an object from one schema to another, and this simplifies some of the 
 * book-keeping.
 * 
 */
public abstract class XmlSchemaNamed extends XmlSchemaAnnotated {
    
    protected XmlSchema parentSchema;
    // Store the name as a QName for the convenience of QName fans.
    private QName qname;
    private boolean topLevel;

    /**
     * Create a new named object.
     * @param parent the parent schema.
     */
    protected XmlSchemaNamed(XmlSchema parent, boolean topLevel) {
        this.parentSchema = parent;
        this.topLevel = topLevel;
    }

    /**
     * Retrieve the name.
     * @return
     */
    public String getName() {
        if (qname == null) {
            return null;
        } else {
            return qname.getLocalPart();
        }
    }
    
    public boolean isAnonymous() {
        return qname == null;
    }

    /**
     * Set the name. Set to null to render the object anonymous.
     * @param name
     */
    public void setName(String name) {
        if ("".equals(name)) {
            throw new XmlSchemaException("Attempt to set empty name.");
        }
        // even non-top-level named items have a full qname. After all, 
        // if form='qualified', we need to serialize it.
        qname = new QName(parentSchema.getLogicalTargetNamespace(), name);
    }

    /**
     * Retrieve the parent schema.
     * @return
     */
    public XmlSchema getParent() {
        return parentSchema;
    }
    
    public QName getQName() {
        return qname; 
    }

    public boolean isTopLevel() {
        return topLevel;
    }
}
