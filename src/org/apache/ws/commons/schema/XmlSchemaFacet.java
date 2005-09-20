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

import org.w3c.dom.Element;

/**
 * Abstract class for all facets that are used when simple types are
 * derived by restriction.
 *
 * @author mukund
 */

// Vidyanand - 16th Oct - initial implementation
// Vidyanand - 17th Oct - added the construct method
// Vidyanand -  6th Dec - changed RuntimeExceptions thrown to XmlSchemaExceptions

public abstract class XmlSchemaFacet extends XmlSchemaAnnotated {

    /**
     * Creates new XmlSchemaFacet
     */


    protected XmlSchemaFacet() {
    }

    protected XmlSchemaFacet(Object value, boolean fixed) {
        this.value = value;
        this.fixed = fixed;
    }

    boolean fixed;

    Object value;

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static XmlSchemaFacet construct(Element el) {
        String name = el.getLocalName();
        boolean fixed = false;
        if (el.getAttribute("fixed").equals("true")) {
            fixed = true;
        }
        try {
            // TODO : move this from reflection to a if condition and avoid cost 
            // of reflection
            Class facetClass = Class.forName("org.apache.ws.commons.schema.XmlSchema"
                                             + Character.toUpperCase(name.charAt(0))
                                             + name.substring(1) + "Facet");
            XmlSchemaFacet facet = (XmlSchemaFacet) facetClass.newInstance();
            facet.setFixed(fixed);
            facet.setValue(el.getAttribute("value"));
            return facet;
        } catch (ClassNotFoundException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (InstantiationException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new XmlSchemaException(e.getMessage());
        }

    }
}
