/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.ws.commons.schema.utils;

import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import java.util.*;

/**
 * Implementation of {@link NamespaceContext}, which is based on a DOM node.
 */
public class NodeNamespaceContext implements NamespacePrefixList {
    private static final Collection XML_NS_PREFIX_COLLECTION = Collections.singletonList(Constants.XML_NS_PREFIX);
    private static final Collection XMLNS_ATTRIBUTE_COLLECTION = Collections.singletonList(Constants.XMLNS_ATTRIBUTE);
    private Node node;
    private Map declarations;
    private String[] prefixes;

    /**
     * Creates a new instance with the given nodes context.
     */
    public NodeNamespaceContext(Node pNode) {
        node = pNode;
    }

    private Map getDeclarations() {
        if (declarations == null) {
            declarations = new HashMap();
            //FIXME: Do we really need to add this mapping? shows up in the serialized schema as xmlns="" 
            //declarations.put(Constants.DEFAULT_NS_PREFIX, Constants.NULL_NS_URI);
            new PrefixCollector(){
                protected void declare(String pPrefix, String pNamespaceURI) {
                    declarations.put(pPrefix, pNamespaceURI);
                }
            }.searchAllPrefixDeclarations(node);
            Collection keys = declarations.keySet();
            prefixes = (String[]) keys.toArray(new String[keys.size()]);
        }
        return declarations;
    }

    public String getNamespaceURI(String pPrefix) {
        if (pPrefix == null) {
            throw new IllegalArgumentException("The prefix must not be null.");
        }
        if (Constants.XML_NS_PREFIX.equals(pPrefix)) {
            return Constants.XML_NS_URI;
        }
        if (Constants.XMLNS_ATTRIBUTE.equals(pPrefix)) {
            return Constants.XMLNS_ATTRIBUTE_NS_URI;
        }
        final String uri = (String) getDeclarations().get(pPrefix);
        return uri == null ? Constants.NULL_NS_URI : uri;
    }

    public String getPrefix(String pNamespaceURI) {
        if (pNamespaceURI == null) {
            throw new IllegalArgumentException("The namespace URI must not be null.");
        }
        if (Constants.XML_NS_URI.equals(pNamespaceURI)) {
            return Constants.XML_NS_PREFIX;
        }
        if (Constants.XMLNS_ATTRIBUTE_NS_URI.equals(pNamespaceURI)) {
            return Constants.XMLNS_ATTRIBUTE;
        }
        Map decl = getDeclarations();
        for (Iterator iter = decl.entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (pNamespaceURI.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    public Iterator getPrefixes(String pNamespaceURI) {
        if (pNamespaceURI == null) {
            throw new IllegalArgumentException("The namespace URI must not be null.");
        }
        if (Constants.XML_NS_URI.equals(pNamespaceURI)) {
            return XML_NS_PREFIX_COLLECTION.iterator();
        }
        if (Constants.XMLNS_ATTRIBUTE_NS_URI.equals(pNamespaceURI)) {
            return XMLNS_ATTRIBUTE_COLLECTION.iterator();
        }
        final List list = new ArrayList();
        for (Iterator iter = getDeclarations().entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (pNamespaceURI.equals(entry.getValue())) {
                list.add(entry.getKey());
            }
        }
        return list.iterator();
    }

    public String[] getDeclaredPrefixes() {
        getDeclarations(); // Make sure, that the prefixes array is valid
        return prefixes;
    }
}
