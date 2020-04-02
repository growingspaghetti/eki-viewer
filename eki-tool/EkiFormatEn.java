import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.HTMLConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class EkiFormatEn {
  private static final String DEST_DIR = "/selenium-screenshooter/eki_en_formatted/";

  private XPathFactory xPathFactory = XPathFactory.newInstance();
  private XPath xpath = xPathFactory.newXPath();
  private XPathExpression tervikart = xpath.compile("//*[@class='tervikart']");
  private XPathExpression leitud_ss = xpath.compile("//*[@class='leitud_ss']");
  private XPathExpression m = xpath.compile("//*[@class='m']|//*[@CLASS='m']");

  public EkiFormatEn() throws Exception {}

  public static void main(String[] args) throws Exception {
    new EkiFormatEn().compile();
  }

  private void compile() throws Exception {
    File ekiDir = new File("/selenium-screenshooter/eki_en");
    File[] words = ekiDir.listFiles();
    Arrays.sort(words);
    for (File f : words) {
      String s = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
      Document d =
          parseHtml(
              s.replace("\n", "")
                  .replace(
                      "<br>", "<br/>")); // StringUtils.substringBetween(s, "</P>", "<div id='tm'>")
      new File(DEST_DIR + f.getName()).delete();
      NodeList tervikarts = (NodeList) tervikart.evaluate(d, XPathConstants.NODESET);

      for (int i = 0; i < tervikarts.getLength(); i++) {
        Element element = (Element) tervikarts.item(i);

        {
          NodeList leituds = (NodeList) leitud_ss.evaluate(element, XPathConstants.NODESET);
          for (int j = leituds.getLength() - 1; j >= 0; j--) {
            Element e = (Element) leituds.item(j);
            e.setAttribute("style", "background-color:#ffffa0");
            e.removeAttribute("class");
          }
        }

        {
          NodeList ms = (NodeList) m.evaluate(element, XPathConstants.NODESET);
          for (int j = ms.getLength() - 1; j >= 0; j--) {
            Element span = (Element) ms.item(j);
            d.renameNode(span, null, "b");
            span.removeAttribute("class");
            span.removeAttribute("CLASS");
          }
        }

        {
          NodeList nList = element.getElementsByTagName("*");
          for (int j = 0; j < nList.getLength(); j++) {
            Element e = (Element) nList.item(j);
            e.removeAttribute("id");
            e.removeAttribute("ID");
            e.removeAttribute("xmlns");
          }
        }

        String strNode =
            strDoc(element)
                .replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?><div class=\"tervikart\">", "")
                .replaceAll("</div>$", "");
        FileUtils.writeStringToFile(
            new File(DEST_DIR + f.getName()), strNode + "\n", StandardCharsets.UTF_8, true);
      }
    }
  }

  private static Document parseHtml(String s) throws Exception {
    HTMLConfiguration config = new HTMLConfiguration();
    DOMParser parser = new DOMParser(config);
    parser.setProperty("http://cyberneko.org/html/properties/names/elems", "default");
    parser.setProperty("http://cyberneko.org/html/properties/names/attrs", "default");
    parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[0]);
    parser.setFeature("http://cyberneko.org/html/features/balance-tags", false);
    try (InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"))) {
      InputSource in = new InputSource(is);
      in.setEncoding("UTF-8");
      parser.parse(in);
      return parser.getDocument();
    }
  }

  private static String strDoc(Node doc) throws TransformerException {
    StringWriter stringWriter = new StringWriter();
    StreamResult xmlOutput = new StreamResult(stringWriter);
    DOMSource source = new DOMSource(doc);

    TransformerFactory transFactory = TransformerFactory.newInstance();
    Transformer transformer = transFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.transform(source, xmlOutput);
    return xmlOutput.getWriter().toString();
  }
}
