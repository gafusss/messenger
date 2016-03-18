package arhangel.dim.container;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

class BeanXmlReader {

    private static final String TAG_BEAN = "bean";
    private static final String TAG_PROPERTY = "property";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_VALUE = "val";
    private static final String ATTR_REF = "ref";
    private static final String ATTR_BEAN_ID = "id";
    private static final String ATTR_BEAN_CLASS = "class";

    public List<Bean> parseBeans(String pathToFile) throws InvalidConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setSchema(null);
        factory.setIgnoringElementContentWhitespace(true);
        List<Bean> beanList = new ArrayList<>();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder(); //throws ParserConfigurationException
            File file = new File(pathToFile);
            Document doc = builder.parse(file); //throws IOException
            Element root = doc.getDocumentElement();
            NodeList beanNodes = root.getElementsByTagName(TAG_BEAN);
            for (int nodeIndex = 0; nodeIndex < beanNodes.getLength(); nodeIndex++) {
                Node beanNode = beanNodes.item(nodeIndex);
                if (beanNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element beanElement = (Element) beanNode;
                    if (!(beanElement.hasAttribute(ATTR_BEAN_ID) && beanElement.hasAttribute(ATTR_BEAN_CLASS))) {
                        throw new InvalidConfigurationException("Missing bean ID or class");
                    }
                    String beanId = beanElement.getAttribute(ATTR_BEAN_ID);
                    String beanClass = beanElement.getAttribute(ATTR_BEAN_CLASS);
                    NodeList beanPropertyNodes = beanElement.getElementsByTagName(TAG_PROPERTY);
                    Map<String, Property> properties = new HashMap<>();
                    for (int propertyIndex = 0; propertyIndex < beanPropertyNodes.getLength(); propertyIndex++) {
                        Node beanPropertyNode = beanPropertyNodes.item(propertyIndex);
                        if (beanPropertyNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element beanPropertyElement = (Element) beanPropertyNode;
                            if (!beanPropertyElement.hasAttribute(ATTR_NAME)) {
                                throw new InvalidConfigurationException("Bean property missing name");
                            }
                            String propertyName = beanPropertyElement.getAttribute(ATTR_NAME);
                            Property property;
                            if (beanPropertyElement.hasAttribute(ATTR_VALUE)) {
                                String propertyValue = beanPropertyElement.getAttribute(ATTR_VALUE);
                                property = new Property(propertyName, propertyValue, ValueType.VAL);
                            } else if (beanPropertyElement.hasAttribute(ATTR_REF)) {
                                String propertyRef = beanPropertyElement.getAttribute(ATTR_REF);
                                property = new Property(propertyName, propertyRef, ValueType.REF);
                            } else {
                                throw new InvalidConfigurationException("Bean property missing either value or ref");
                            }
                            properties.put(property.getName(), property);
                        } else {
                            throw new InvalidConfigurationException("Invalid configuration");
                        }
                    }
                    Bean bean = new Bean(beanId, beanClass, properties);
                    beanList.add(bean);
                } else {
                    throw new InvalidConfigurationException("Invalid configuration");
                }
            }
            return beanList;
        } catch (InvalidConfigurationException e) {
            throw e;
        } catch (ParserConfigurationException e) {
            throw new InvalidConfigurationException("Parser error:" + e.getMessage());
        } catch (SAXException e) {
            throw new InvalidConfigurationException("SAX Exception" + e.getMessage());
        } catch (IOException e) {
            throw new InvalidConfigurationException("IO Exception" + e.getMessage());
        } catch (Exception e) {
            throw new InvalidConfigurationException("Unknown exception: " + e.getMessage());
        }
    }
}