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

package org.apache.ws.commons.schema.tools;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import org.apache.ws.commons.schema.XmlSchemaCollection;

/**
 * 
 */
public class ReadSchemaFromURL {

    /**
     * Read a schema from a URL, perhaps provoking errors.
     * @param args
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        if (args.length != 1) {
            System.err.println("Usage: ReadSchemaFromURL URL");
            return;
        }
        XmlSchemaCollection collection = new XmlSchemaCollection();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(args[0]);
        collection.read(document, args[0], null);
        System.out.println("Success.");
    }

}
