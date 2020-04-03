
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

// if Anki directory is in exfat file system
public class AnkiSha1Move {
  private static final String MEDIA_PATH = "/eki-viewer/collection.media/";
  private static final File MEDIA_DIR = new File(MEDIA_PATH);
  private static final MessageDigest SHA1 = DigestUtils.getSha1Digest();

  public static void main(String[] args) throws Exception {
    for (File m : MEDIA_DIR.listFiles()) {
      if (m.isDirectory()) {
        continue;
      }
      SHA1.reset();
      byte[] hash = SHA1.digest(m.getName().getBytes(StandardCharsets.UTF_8));
      String h1 = new String(Hex.encodeHex(Arrays.copyOfRange(hash, 0, 1), true));
      String h2 = new String(Hex.encodeHex(Arrays.copyOfRange(hash, 1, 2), true));
      String destinationDir = String.format("%seki_/%s/%s/", MEDIA_PATH, h1, h2);
      FileUtils.moveFileToDirectory(m, new File(destinationDir), true);
      new File(destinationDir + ".nomedia").createNewFile();
      new File(String.format("%seki_/%s/.nomedia", MEDIA_PATH, h1)).createNewFile();
    }
  }
}
