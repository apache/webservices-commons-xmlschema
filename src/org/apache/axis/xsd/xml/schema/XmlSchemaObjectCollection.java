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
 * XmlSchemaObjectCollection.java
 *
 * Created on September 27, 2001, 2:58 AM
 */

package org.apache.axis.xsd.xml.schema;

import java.util.Iterator;
import java.util.Vector;

/**
 * An object collection class to handle XmlSchemaObjects when collections
 * are returned from method calls.
 *
 * @author  mukund
 */
public class XmlSchemaObjectCollection {

    Vector objects;

    /** Creates new XmlSchemaObjectCollection */
    public XmlSchemaObjectCollection() {
        objects = new Vector();
    }

    public int getCount() {
        return objects.size();
    }

    public XmlSchemaObject getItem(int i) {
        return (XmlSchemaObject) objects.elementAt(i);
    }

    public void setItem(int i, XmlSchemaObject item) {
        objects.insertElementAt(item, i);
    }

    public void add(XmlSchemaObject item) {
        objects.addElement(item);
    }

    public boolean contains(XmlSchemaObject item) {
        return objects.contains(item);
    }

    public int indexOf(XmlSchemaObject item) {
        return objects.indexOf(item);
    }

    public void remove(XmlSchemaObject item) {
        objects.remove(item);
    }

    public void removeAt(int index) {
        objects.removeElementAt(index);
    }

    public Iterator getIterator() {
        return objects.iterator();
    }

    public String toString(String prefix, int tab) {
        String xml = new String();

        for (int i = 0; i < getCount(); i++) {
            xml += getItem(i).toString(prefix, tab);
        }


        return xml;

    }
}
