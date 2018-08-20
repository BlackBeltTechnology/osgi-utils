package hu.blackbelt.osgi.utils.lang.util;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static java.lang.Boolean.TRUE;

/**
 * XML related utils. E.g.: serialization/deserialization
 */
@Slf4j
public final class XmlUtil {
    private XmlUtil() {
    }

    /**
     * Serialize any JAXB annotated input object to XML string.
     *
     * @param input object with JAXB annotations
     * @param schemaData schema for validation
     * @return serialized XML
     *
     * @throws IllegalArgumentException in case of JAXB related problem
     */
    public static String jaxbToString(Object input, byte[] schemaData) {
        StringWriter writer = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(input.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, TRUE);
            if (schemaData != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(schemaData)));
                marshaller.setSchema(schema);
                MessageCollectorValidationEventHandler eventHandler = new MessageCollectorValidationEventHandler();
                marshaller.setEventHandler(eventHandler);
                marshaller.marshal(input, writer);
                if (!eventHandler.isValid()) {
                    throw new IllegalArgumentException("Invalid input object: " + eventHandler.getMessages());
                }
            } else {
                marshaller.marshal(input, writer);
            }
        } catch (JAXBException je) {
            throw new IllegalArgumentException("Unable to serialize " + input + " to xml using jaxb.", je);
        } catch (SAXException se) {
            throw new IllegalArgumentException("Invalid xsd found in schema.", se);
        }
        // TODO fix the hack
        String result = writer.toString();
        result = result.replaceAll(" xmlns:ns2=\\S*>", ">");
        result = result.replaceAll(" xmlns=\\S*>", ">");
        result = result.replaceAll("ns2:", "");
        return result;
    }

    /**
     * Serialize any JAXB annotated input object to XML string.
     *
     * @param input object with JAXB annotations
     * @return serialized XML
     *
     * @throws IllegalArgumentException in case of JAXB related problem
     */
    public static String jaxbToString(Object input) {
        return jaxbToString(input, null);
    }

    private static class MessageCollectorValidationEventHandler implements ValidationEventHandler {
        private boolean valid;
        private StringBuilder messages;

        public MessageCollectorValidationEventHandler() {
            valid = true;
            messages = new StringBuilder();
        }

        public boolean handleEvent(ValidationEvent event) {
            if (!valid) {
                messages.append("\n");
            }
            messages.append(event.getMessage());
            valid = false;
            return true;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessages() {
            return messages.toString();
        }
    }
}
