package tests;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import sun.text.CompactShortArray;
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

public class TestElementRefs extends TestCase {
    public void testElementRefs() throws Exception {
        QName ELEMENT_QNAME = new QName("http://soapinterop.org/types",
                "attTests");
        InputStream is = new FileInputStream("test-resources/elementreferences.xsd");
        XmlSchemaCollection schema = new XmlSchemaCollection();
        schema.read(new StreamSource(is), null);

        XmlSchemaElement elem = schema.getElementByQName(ELEMENT_QNAME);

        assertNotNull(elem);

        XmlSchemaComplexType cmplxType = (XmlSchemaComplexType)elem.getSchemaType();
        XmlSchemaObjectCollection items = ((XmlSchemaSequence)cmplxType.getParticle()).getItems();

        Iterator it = items.getIterator();
        while (it.hasNext()) {
            XmlSchemaElement innerElement =  (XmlSchemaElement)it.next();
            assertNotNull(innerElement.getRefName());
        }



    }

}
