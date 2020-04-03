
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

// if Anki directory is in exfat file system
public class AnkiSha1TabText {
  private static final MessageDigest SHA1 = DigestUtils.getSha1Digest();

  private static final Pattern IMG_PA = Pattern.compile(" src=\"([^\"]+?)\"");
  private static final Pattern MP3_PA = Pattern.compile("\\[sound:([^\\]]+?)\\]");

  private String getHashDir(String fileName) {
    SHA1.reset();
    byte[] hash = SHA1.digest(fileName.getBytes(StandardCharsets.UTF_8));
    String h1 = new String(Hex.encodeHex(Arrays.copyOfRange(hash, 0, 1), true));
    String h2 = new String(Hex.encodeHex(Arrays.copyOfRange(hash, 1, 2), true));
    return String.format("%s/%s/%s", h1, h2, fileName);
  }

  private void compile() throws Exception {
    List<String> lines =
        FileUtils.readLines(new File("/eki-viewer/eki-anki-tab.txt"), "UTF-8");
    List<String> results = new ArrayList<>();
    for (String contents: lines) {
      {
        Matcher m = IMG_PA.matcher(contents);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
          m.appendReplacement(sb, " src=\"eki_/" + getHashDir(m.group(1)) + "\"");
        }
        m.appendTail(sb);
        contents = sb.toString();
      }
      {
        Matcher m = MP3_PA.matcher(contents);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
          m.appendReplacement(sb, "[sound:eki_/" + getHashDir(m.group(1)) + "]");
        }
        m.appendTail(sb);
        contents = sb.toString();
      }
     results.add(contents);
    }
    FileUtils.writeLines(new File("/eki-viewer/eki-anki-tab.txt-sha1.txt"), results);
  }

  public static void main(String[] args) throws Exception {
    new AnkiSha1TabText().compile();
  }
}
