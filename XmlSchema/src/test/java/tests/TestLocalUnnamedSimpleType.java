package tests;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchemaCollection;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

/**
 * TestElementForm
 */
public class TestLocalUnnamedSimpleType extends TestCase {
    String schemaXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
             "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\"\n" +
             "targetNamespace=\"http://finance.example.com/CreditCardFaults/xsd\"\n" +
             "xmlns:tns=\"http://finance.example.com/CreditCardFaults/xsd\"\n" +
             "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
             "xsi:schemaLocation=\"http://www.w3.org/2001/XMLSchema\n" +
             "              http://www.w3.org/2001/XMLSchema.xsd\">\n" +
             "\n" +
             "<element name=\"tns:CreditCardNumber\" type=\"string\"></element>\n" +
             "\n" +
             "<element name=\"tns:CreditCardType\">\n" +
             "<simpleType>\n" +
             "<restriction base=\"string\">\n" +
             "<enumeration value=\"AMEX\" />\n" +
             "<enumeration value=\"MASTERCARD\" />\n" +
             "<enumeration value=\"VISA\" />\n" +
             "</restriction>\n" +
             "</simpleType>\n" +
             "</element>\n" +
             "</schema> ";

    public void testLocalUnnamedSimpleType() throws Exception {
        XmlSchemaCollection schema = new XmlSchemaCollection();
        schema.read(new StreamSource(new ByteArrayInputStream(schemaXML.getBytes())), null);
    }
}
