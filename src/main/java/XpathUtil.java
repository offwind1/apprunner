import cn.hutool.core.util.ObjectUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.text.Document;
import javax.xml.xpath.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Xpath工具
 */
public class XpathUtil {

    private static final List<String> xpathExpr = Arrays.asList("name", "label", "value", "resource-id", "content-desc", "class", "text", "index");


    public static List<Map<String, Object>> getListFromXPath(String key, Document pageDom) {
        List<Map<String, Object>> nodesMap = new ArrayList<Map<String, Object>>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            XPathExpression compile = xPath.compile(key);

            if (Pattern.matches("string(.*)", key) || Pattern.matches(".*/@[^/]*", key)) {
                final Object attr = compile.evaluate(pageDom, XPathConstants.STRING);
                nodesMap.add(new HashMap<String, Object>() {{
                    put("attribute", attr);
                }});
            } else {
                Object object = compile.evaluate(pageDom, XPathConstants.NODE);
                NodeList nodeList = (NodeList) object;

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Map<String, Object> nodeMap = new HashMap<String, Object>();
                    nodeMap.put("name", "");
                    nodeMap.put("value", "");
                    nodeMap.put("label", "");

                    Node node = nodeList.item(i);
                    nodeMap.put("tag", node.getNodeName());
                    List<Map<String, String>> path = getAttributesFromNode(node);
                    nodeMap.put("xpath", getXPathFromAttributes(path));
                    nodeMap.put(node.getNodeName(), node.getNodeValue());

                    //获得所有节点属性
                    NamedNodeMap nodeAttributes = node.getAttributes();
                    if (ObjectUtil.isNotNull(nodeAttributes)) {
                        for (int a = 0; a < nodeAttributes.getLength(); a++) {
                            if (nodeAttributes.item(a) instanceof Attr) {
                                Attr attr = (Attr) nodeAttributes.item(a);
                                nodeMap.put(attr.getName(), attr.getValue());
                            }
                        }
                    }

                    //todo

                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return null;
    }


    private static List<Map<String, String>> getAttributesFromNode(Node node) {
        List<Map<String, String>> path = new ArrayList<Map<String, String>>();
        getParent(node, path);

        Collections.reverse(path);
        return path;
    }


    private static void getParent(final Node node, List<Map<String, String>> path) {
        if (node.hasAttributes()) {
            NamedNodeMap attributes = node.getAttributes();
            List<Map<String, String>> attributeMap = new ArrayList<Map<String, String>>();

            for (int i = 0; i < attributes.getLength(); i++) {
                Object map = attributeMap.get(i);
                if (map instanceof Attr) {
                    final Attr attr = (Attr) map;
                    attributeMap.add(new HashMap<String, String>() {{
                        put(attr.getName(), attr.getValue());
                    }});
                }
            }

            attributeMap.add(new HashMap<String, String>() {{
                put("tag", node.getNodeName());
            }});
            path.addAll(attributeMap);
        }

        if (ObjectUtil.isNotNull(node.getParentNode())) {
            getParent(node.getParentNode(), path);
        }
    }

    private static String getXPathFromAttributes(List<Map<String, String>> attributes) {
        String xpath = attributes.subList(0, 4).stream().map(attribute -> {

            Map<String, String> newAttribute = attribute;

            xpathExpr.forEach(key -> {
                if ("".equals(attribute.getOrDefault(key, ""))) {
                    newAttribute.remove(key);
                } else {
                    newAttribute.remove("path");
                }
            });

            if (newAttribute.getOrDefault("name", "") == newAttribute.getOrDefault("label", "")) {
                newAttribute.remove("name");
            }

            if (newAttribute.getOrDefault("content-desc", "") == newAttribute.getOrDefault("resource-id", "")) {
                newAttribute.remove("content-desc");
            }

//            if ("".equals(attribute.getOrDefault("resource-id", ""))) {
//                newAttribute = new HashMap<String, String>(){{put("resource-id", attribute.getOrDefault("resource-id", ""))}};
//            }

            String xpathSingle = newAttribute.keySet().stream().map(key -> {
                switch (key) {
                    case "tag":
                        return "";
                    case "name":
                        if (newAttribute.get(key).length() > 50)
                            return "";
                        break;
                    case "text":
                        if (!newAttribute.get("tag").contains("Button") && newAttribute.get("tag").length() > 10)
                            return "";
                        break;
                }

                if (xpathExpr.contains(key) && ObjectUtil.isNotEmpty(newAttribute.get(key))) {
                    //s"@${kv._1}=" + "\"" + kv._2.replace("\"", "\\\"") + "\""
                    return "@" + key + "=\"" + newAttribute.get(key).replace("\"", "\\\"") + "\"";
                }

                return "";
            }).filter(x -> {
                return ObjectUtil.isNotEmpty(x);
            }).collect(Collectors.joining(" and "));


            if (ObjectUtil.isEmpty(xpathSingle)) {
                xpathSingle = "/" + attribute.getOrDefault("class", attribute.getOrDefault("tag", "*"));
            } else {
                xpathSingle = "/*[" + xpathSingle + "]";
            }
            return xpathSingle;
        }).collect(Collectors.joining());

        if (ObjectUtil.isEmpty(xpath)) {
//            log.trace(attributes)
        } else {
            xpath = "/" + xpath;
        }

        return xpath;
    }


}
