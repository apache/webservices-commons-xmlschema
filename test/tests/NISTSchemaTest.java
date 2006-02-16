package tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Element;

public class NISTSchemaTest extends XMLTestCase {

    private List failed = new ArrayList();

    private List passed = new ArrayList();

    private String schemaLocation = "target/xmlschema2002-01-16/nisttest/NISTTestsAll";
    
    public NISTSchemaTest(String name) {
        super(name);
        
        schemaLocation = System.getProperty("nistTestLocation", schemaLocation);
    }

    public void testReadWrite() throws Exception {
        traverse(new File(schemaLocation));
        
        System.out.println("Passed: " + passed.size() + "/" + (passed.size() + failed.size()));
        
        if (failed.size() > 0) {
            fail("Some schemas didn't write correctly!");
        }
        
        if (passed.size() == 0) {
            fail("No schemas were located! Make sure the schema location is correct: " 
                    + schemaLocation);
        }
    }

    public XmlSchema loadSchema(File f) throws Exception {
        XmlSchemaCollection col = new XmlSchemaCollection();
        col.setBaseUri(schemaLocation);
        XmlSchema xmlSchema = col.read(new FileReader(f), null);
        return xmlSchema;
    }

    public void traverse(File f) throws Exception {

        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                traverse(files[i]);
            }
        } else if (f.getAbsolutePath().endsWith("xsd")) {
            XmlSchema schema = loadSchema(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            schema.write(baos);

            Diff diff = new Diff(new FileReader(f), new InputStreamReader(
                    new ByteArrayInputStream(baos.toByteArray())));

            DetailedDiff detaileddiffs = new DetailedDiff(diff);
            detaileddiffs.overrideDifferenceListener(new SchemaAttrDiff());
            boolean result = detaileddiffs.similar();

            if (result) {
                passed.add(f.getName());
            } else {
                failed.add(f.getName());
                System.out.println("Failed: " + f.getName());
            }
        }
    }

    static class SchemaAttrDiff extends
            IgnoreTextAndAttributeValuesDifferenceListener {

        public int differenceFound(Difference difference) {
            
            if (difference.getId() == DifferenceConstants.ELEMENT_NUM_ATTRIBUTES.getId()) {
                // control and test have to be elements
                // check if they are schema elements .. they only
                // seem to have the added attributeFormDefault and
                // elementFormDefault attributes
                // so shldnt have more than 2 attributes difference
                Element actualEl = (Element) difference.getControlNodeDetail().getNode();
                Element testEl = (Element) difference.getTestNodeDetail().getNode();
                
                if (actualEl.getLocalName().equals("schema")) {

                    int expectedAttrs = Integer.parseInt(difference.getControlNodeDetail().getValue());
                    int actualAttrs = Integer.parseInt(difference.getTestNodeDetail().getValue());
                    if (Math.abs(actualAttrs - expectedAttrs) <= 2) {
                        return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                    }
                }
            }
            else if (difference.getId() == DifferenceConstants.ATTR_NAME_NOT_FOUND_ID) {
                // sometimes the serializer throws in a few extra attributes...
                Element actualEl = (Element) difference.getControlNodeDetail().getNode();
                
                if (actualEl.getLocalName().equals("schema")) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
            
            return super.differenceFound(difference);
        }
    }

}
