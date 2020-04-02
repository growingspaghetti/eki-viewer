
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Eki {
  private static final Logger LOGGER = Logger.getGlobal();

  public static void main(String[] args) throws IOException {
    WebDriver driver =
        new RemoteWebDriver(new URL("http://127.0.0.1:4444/wd/hub"), DesiredCapabilities.chrome());

    List<String> temporaryWordList = new ArrayList<>();
    temporaryWordList.add("aabits");
    try {

      while (!temporaryWordList.isEmpty()) {
        String currentWord = temporaryWordList.get(0);
        LOGGER.info(currentWord);
        driver.get("http://www.eki.ee/dict/psv/index.cgi?F=M&Q=" + currentWord);
        temporaryWordList.remove(0);

        String pageContents = driver.getPageSource();
        FileUtils.writeStringToFile(
            new File("eki/" + currentWord), pageContents + "\n", StandardCharsets.UTF_8);

        List<WebElement> elements =
            driver.findElements(
                By.xpath("//p[@class='kontekst_p kontekst_leid']/following-sibling::p"));

        if (!elements.isEmpty()) {
          temporaryWordList.clear();
          temporaryWordList.addAll(
              elements.stream().map(e -> e.getAttribute("data-m")).collect(Collectors.toList()));
        }
      }

    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "", ex);
    } finally {
      Optional.ofNullable(driver).ifPresent(WebDriver::quit);
    }
  }
}
