package tests;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class ImportTest extends TestCase {

    public void testSchemaImport() throws Exception{
        //create a DOM document
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document doc = documentBuilderFactory.newDocumentBuilder().
                parse("test-resources/importBase.xsd");

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        schemaCol.setBaseUri("test-resources");
        XmlSchema schema = schemaCol.read(doc,null);
        assertNotNull(schema);

    }


//    public void testSchemaImportRemote() throws Exception{
//        //create a DOM document
//        String schemaLocation = "http://131.107.72.15/SoapWsdl_BaseDataTypes_XmlFormatter_Service_Indigo/BaseDataTypesDocLitB.svc?xsd=xsd1";
//        java.net.URL u = new java.net.URL(schemaLocation);
//        InputStream uStream = u.openConnection().getInputStream();
//
//        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
//        XmlSchema schema = schemaCol.read(new InputStreamReader(uStream),null);
//        assertNotNull(schema);
//
//    }
}
