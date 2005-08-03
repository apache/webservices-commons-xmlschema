/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * XmlSchemaFacet.java
 *
 * Created on September 27, 2001, 3:21 AM
 */

package org.apache.axis.xsd.xml.schema;

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
            Class facetClass = Class.forName("org.apache.axis.xsd.xml.schema.XmlSchema"
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
