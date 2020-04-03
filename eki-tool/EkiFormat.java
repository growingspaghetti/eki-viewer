
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.lang.model.util.Elements;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.HTMLConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class EkiFormat {
  private static final Logger LOGGER = Logger.getGlobal();
  private static final String DEST_DIR =
      "/selenium-screenshooter/eki_formatted/";

  private XPathFactory xPathFactory = XPathFactory.newInstance();
  private XPath xpath = xPathFactory.newXPath();
  private XPathExpression tervikart = xpath.compile("//*[@class='tervikart']");
  private XPathExpression teisedallikad = xpath.compile("//*[@class='teisedallikad']");
  private XPathExpression paradigma = xpath.compile("//*[@class='paradigma']");
  private XPathExpression example  = xpath.compile("//*[@style='font-style:italic;color:black;']");
  private XPathExpression mvf = xpath.compile("//*[@class='mvf']");
  private XPathExpression gray  = xpath.compile("//*[@style='color:gray']");
  private XPathExpression gkg_c_gkn = xpath.compile("//*[@class='gkg_c_gkn']");
  private XPathExpression tp_c_tnr = xpath.compile("//*[@class='tp_c_tnr']");
  private XPathExpression period  = xpath.compile("//*[@style='color:firebrick']");
  
  public EkiFormat() throws Exception {}

  public static void main(String[] args) throws Exception {
    new EkiFormat().compile();
  }

  private void compile() throws Exception {
    File ekiDir = new File("/selenium-screenshooter/eki");
    File[] words = ekiDir.listFiles();
    Arrays.sort(words);
    for (File f : words) {
      String s = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
      Document d = parseHtml(s);

      new File(DEST_DIR + f.getName()).delete();

      NodeList tervikarts = (NodeList) tervikart.evaluate(d, XPathConstants.NODESET);

      for (int i = 0; i < tervikarts.getLength(); i++) {
        Element element = (Element) tervikarts.item(i);

        {
          NodeList teisedallikads =
              (NodeList) teisedallikad.evaluate(element, XPathConstants.NODESET);
          for (int j = 0; j < teisedallikads.getLength(); j++) {
            Element toRemove = (Element) teisedallikads.item(j);
            toRemove.getParentNode().removeChild(toRemove);
          }
        }

        {
          NodeList sources = element.getElementsByTagName("source");
          for (int j = 0; j < sources.getLength(); j++) {
            Element e = (Element) sources.item(j);
            String src = Optional.ofNullable(e.getAttribute("src")).orElse("");
            LOGGER.info(src);
            if (src.startsWith("__synt/") && src.endsWith(".wav")) {
              byte[] b = IOUtils.toByteArray(new URL("http://www.eki.ee/dict/psv/" + src));
              File mediaDir =
                  new File(DEST_DIR + f.getName().toLowerCase().replace(" ", "_") + ".media");
              mediaDir.mkdirs();
              FileUtils.writeByteArrayToFile(
                  new File(mediaDir.getAbsolutePath() + "/" + f.getName() + "." + j + ".wav"), b);
            }
          }
        }

        {
          NodeList audios = element.getElementsByTagName("audio");
          for (int j = audios.getLength() - 1; j >= 0; j--) {
            Node toRemove = audios.item(j);
            toRemove.getParentNode().removeChild(toRemove);
          }
        }
        
        {
            NodeList gkgs =  (NodeList) gkg_c_gkn.evaluate(element, XPathConstants.NODESET);
            for (int j = gkgs.getLength() - 1; j >= 0; j--) {
              Node toRemove = gkgs.item(j);
              toRemove.getParentNode().removeChild(toRemove);
            }
          }
        
        {
            NodeList periods =  (NodeList) period.evaluate(element, XPathConstants.NODESET);
            for (int j = periods.getLength() - 1; j >= 0; j--) {
              Node toRemove = periods.item(j);
              toRemove.getParentNode().removeChild(toRemove);
            }
          }
        
        {
            NodeList hrs =  element.getElementsByTagName("hr");
            for (int j = hrs.getLength() - 1; j >= 0; j--) {
              Node toRemove = hrs.item(j);
              toRemove.getParentNode().removeChild(toRemove);
            }
          }
        
        {
         NodeList paradigmas = (NodeList) paradigma.evaluate(element, XPathConstants.NODESET);
            for (int j = paradigmas.getLength() - 1; j >= 0; j--) {
              Node toRemove = paradigmas.item(j);
              toRemove.getParentNode().removeChild(toRemove);
            }
          }
        
        {
            NodeList ps = element.getElementsByTagName("p");
            for (int j = ps.getLength() - 1; j >= 0; j--) {

                Node toRemove = ps.item(j);
                NodeList children = toRemove.getChildNodes();
                IntStream
                    .range(0, children.getLength())
                    .mapToObj(children::item)
                    .filter(Objects::nonNull)
                    .forEach(n -> toRemove.getParentNode().insertBefore(n.cloneNode(true), toRemove));
                toRemove.getParentNode().removeChild(toRemove);
            }
        }
        
        {
            NodeList as = element.getElementsByTagName("a");
            for (int j = as.getLength() - 1; j >= 0; j--) {
                Node toRemove = as.item(j);
                NodeList children = toRemove.getChildNodes();
                IntStream
                    .range(0, children.getLength())
                    .mapToObj(children::item)
                    .filter(Objects::nonNull)
                    .forEach(n -> toRemove.getParentNode().insertBefore(n.cloneNode(true), toRemove));
                toRemove.getParentNode().removeChild(toRemove);
            }
        }
        
        {
            NodeList examples = (NodeList) example.evaluate(element, XPathConstants.NODESET);
               for (int j = examples.getLength() - 1; j >= 0; j--) {
                   Element font = d.createElement("font");
                   Element ita  = d.createElement("i");
                   font.setAttribute("color", "#0000A0");
                   font.appendChild(ita);
                   
                 Node toRemove = examples.item(j);
                 toRemove.getParentNode().insertBefore(font, toRemove);
                 NodeList children = toRemove.getChildNodes();
                 IntStream
                     .range(0, children.getLength())
                     .mapToObj(children::item)
                     .filter(Objects::nonNull)
                     .forEach(n -> ita.appendChild(n.cloneNode(true)));
                 toRemove.getParentNode().removeChild(toRemove);
               }
             }
        
        {
            NodeList mvfs = (NodeList) mvf.evaluate(element, XPathConstants.NODESET);
               for (int j = mvfs.getLength() - 1; j >= 0; j--) {
                   Element font = d.createElement("font");
                   Element sma  = d.createElement("small");
                   font.setAttribute("color", "#808080");
                   font.appendChild(sma);
                   
                 Node toRemove = mvfs.item(j);
                 toRemove.getParentNode().insertBefore(font, toRemove);
                 NodeList children = toRemove.getChildNodes();
                 IntStream
                     .range(0, children.getLength())
                     .mapToObj(children::item)
                     .filter(Objects::nonNull)
                     .forEach(n -> sma.appendChild(n.cloneNode(true)));
                 toRemove.getParentNode().removeChild(toRemove);
               }
             }
        {
            NodeList tps = (NodeList) tp_c_tnr.evaluate(element, XPathConstants.NODESET);
               for (int j = tps.getLength() - 1; j >= 0; j--) {
                   Element font = d.createElement("font");
                   Element b  = d.createElement("b");
                   font.setAttribute("color", "#585800");
                   font.appendChild(b);
                   
                 Node toRemove = tps.item(j);
                 toRemove.getParentNode().insertBefore(font, toRemove);
                 NodeList children = toRemove.getChildNodes();
                 IntStream
                     .range(0, children.getLength())
                     .mapToObj(children::item)
                     .filter(Objects::nonNull)
                     .forEach(n -> b.appendChild(n.cloneNode(true)));
                 toRemove.getParentNode().removeChild(toRemove);
               }
             }
        
        {
            NodeList grays = (NodeList) gray.evaluate(element, XPathConstants.NODESET);
               for (int j = grays.getLength() - 1; j >= 0; j--) {
                 Node toRemove = grays.item(j);
                 NodeList children = toRemove.getChildNodes();
                 IntStream
                     .range(0, children.getLength())
                     .mapToObj(children::item)
                     .filter(Objects::nonNull)
                     .forEach(n -> toRemove.getParentNode().insertBefore(n.cloneNode(true), toRemove));
                 toRemove.getParentNode().removeChild(toRemove);
               }
             }
        {
          NodeList imgs = element.getElementsByTagName("img");
          for (int j = imgs.getLength() - 1; j >= 0; j--) {
            Element e = (Element) imgs.item(j);
            String src = Optional.ofNullable(e.getAttribute("src")).orElse("");
            LOGGER.info(src);
            if (src.startsWith("__pildid/") && src.endsWith(".jpg")) {
              byte[] b =
                  IOUtils.toByteArray(
                      new URL("http://www.eki.ee/dict/psv/" + src.replace(" ", "%20")));
              File mediaDir =
                  new File(DEST_DIR + f.getName().toLowerCase().replace(" ", "_") + ".media");
              mediaDir.mkdirs();
              String fname = "eki_" + new File(src).getName().toLowerCase().replace(" ", "_");
              FileUtils.writeByteArrayToFile(new File(mediaDir.getAbsolutePath() + "/" + fname), b);
              e.setAttribute("src", fname);
            } else {
              e.getParentNode().removeChild(e);
            }
          }
        }
        
        {
        	NodeList nList = element.getElementsByTagName("*");
            for (int j = 0; j < nList.getLength(); j++) {
                Element e = (Element) nList.item(j);
                e.removeAttribute("id");
                e.removeAttribute("xmlns");
                e.removeAttribute("lang");
            }
        }
        
        String strNode = strDoc(element).replace("<br/><font color=\"#0000A0\"><i>", "<br/><font color=\"#0000A0\">・<i>");
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
