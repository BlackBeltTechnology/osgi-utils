package hu.blackbelt.osgi.utils.lang.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import static com.google.common.base.Charsets.UTF_8;
import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;

public class XmlUtilTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testJaxbToString() {
        String expected = "<document>\n"
                + "    <a>a1</a>\n"
                + "    <b>b1</b>\n"
                + "</document>";
        String result = XmlUtil.jaxbToString(Input.builder().a("a1").b("b1").build());
        assertEquals(expected, result);
    }

    @Test
    public void testJaxbToStringWithnamespace() {
        String expected = "<document>\n"
                + "    <a>a1</a>\n"
                + "    <b>b1</b>\n"
                + "</document>";
        String result = XmlUtil.jaxbToString(InputWithNamespace.builder().a("a1").b("b1").build());
        assertEquals(expected, result);
    }

    @Test
    public void testJaxbToStringError() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectCause(any(JAXBException.class));
        XmlUtil.jaxbToString(new Object());
    }

    @Test
    public void testSchemaValidation() {
        String schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xs:schema elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:element name=\"document\">\n"
                + "    <xs:complexType>\n"
                + "      <xs:all>\n"
                + "        <xs:element name=\"a\" type=\"stringMaxSize5\" maxOccurs=\"1\" minOccurs=\"1\"/>\n"
                + "        <xs:element name=\"b\" type=\"xs:string\" maxOccurs=\"1\" minOccurs=\"1\"/>\n"
                + "      </xs:all>\n"
                + "    </xs:complexType>\n"
                + "  </xs:element>\n"
                + "    <xs:simpleType name=\"stringMaxSize5\">\n"
                + "        <xs:restriction base=\"xs:string\">\n"
                + "            <xs:maxLength value=\"5\"/>\n"
                + "        </xs:restriction>\n"
                + "    </xs:simpleType>"
                + "</xs:schema>\n";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid input object: cvc-maxLength-valid: Value 'tooLongString' with length = '13' "
                + "is not facet-valid with respect to maxLength '5' for type 'stringMaxSize5'.\n"
                + "cvc-type.3.1.3: The value 'tooLongString' of element 'a' is not valid.\n"
                + "cvc-complex-type.2.4.b: The content of element 'document' is not complete. One of '{b}' is expected.");
        XmlUtil.jaxbToString(Input.builder().a("tooLongString").build(), schema.getBytes(UTF_8));
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    @XmlRootElement(name = "document")
    private static final class Input {
        @XmlElement(required = true)
        private String a;
        @XmlElement(required = true)
        private String b;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    @XmlRootElement(name = "document", namespace = "http://example.com/ns")
    private static final class InputWithNamespace {
        @XmlElement(required = true)
        private String a;
        @XmlElement(required = true)
        private String b;
    }
}
