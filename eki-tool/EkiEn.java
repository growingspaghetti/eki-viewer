
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class EkiEn {
  private static final Logger LOGGER = Logger.getGlobal();

  public static void main(String[] args) throws IOException {
    File ekiDir = new File("eki");
    for (String f : ekiDir.list()) {
      LOGGER.log(Level.INFO, f);
      URL u =
          new URL(
              "http://www.eki.ee/dict/ies/index.cgi?&F=V&Q="
                  + URLEncoder.encode(f, StandardCharsets.UTF_8.toString()));
      FileUtils.writeStringToFile(
          new File("eki_en/" + f),
          IOUtils.toString(u, StandardCharsets.UTF_8),
          StandardCharsets.UTF_8);
    }
  }
}
