
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class EkiAnki {
  public static void main(String[] args) throws Exception {
    new EkiAnki().compile();
  }

  private void compile() throws Exception {
    String ekiOriginalDir = "/selenium-screenshooter/eki";
    String ekiFormatted = ekiOriginalDir + "_formatted/";
    String ekiEnFormatted = ekiOriginalDir + "_en_formatted/";
    File ekiDir = new File(ekiOriginalDir);
    List<String> ankiText = new ArrayList<>();
    String[] words = ekiDir.list();
    Arrays.sort(words);
    for (String w : words) {
      List<String> liEt = FileUtils.readLines(new File(ekiFormatted + w), StandardCharsets.UTF_8);
      String xmlHeader =
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?><div class=\"tervikart\" xmlns=\"http://www.w3.org/1999/xhtml\">";
      String et =
          liEt.stream()
              .filter(s -> !StringUtils.isEmpty(s))
              .map(s -> s.replace(xmlHeader, ""))
              .map(s -> s.replaceAll("</div>$", ""))
              .map(s -> s.replace("<br/>", "<br>"))
              .map(s -> s.replace("\t", "  "))
              .collect(Collectors.joining("<br>"));

      String en = obtainEnText(new File(ekiEnFormatted + w));

      String lookupDir = ekiFormatted + w.toLowerCase().replace(" ", "_") + ".media/";
      String destinationMp3FileName = mp3gen(w, lookupDir);

      String ankiLine = String.format("%s\t[sound:%s]\t%s\t%s", w, destinationMp3FileName, et, en);
      ankiText.add(ankiLine);
    }
    FileUtils.writeLines(new File("eki-anki-tab.txt"), ankiText);
  }

  private String obtainEnText(File fileToRead) throws IOException {
    if (!fileToRead.exists()) {
      return "";
    }
    List<String> liEn = FileUtils.readLines(fileToRead, StandardCharsets.UTF_8);
    return liEn.stream()
        .filter(s -> !StringUtils.isEmpty(s))
        .map(s -> s.replace("\t", "  "))
        .collect(Collectors.joining(""));
  }

  private String mp3gen(String w, String lookupDir) throws Exception {
    if (!new File(lookupDir).exists()) {
      return "";
    }
    List<File> mp3s =
        Arrays.asList(new File(lookupDir).listFiles())
            .stream()
            .sorted()
            .filter(f -> f.getName().endsWith(".mp3"))
            .collect(Collectors.toList());
    String destinationMp3FileName = "eki_" + w.toLowerCase().replace(" ", "_") + ".mp3";
    ffmpeg(destinationMp3FileName, mp3s);
    return destinationMp3FileName;
  }

  private static void ffmpeg(String t, List<File> fs) {
    try {
      List<String> command = new ArrayList<>();

      // ffmpeg -i "concat:file1.mp3|file2.mp3" -acodec copy output.mp3
      StringBuilder sb = new StringBuilder();
      for (File f : fs) {
        sb.append("|" + f.getAbsolutePath());
      }

      command.add("ffmpeg");
      command.add("-i");
      if (fs.isEmpty()) {
        return;
      }
      if (fs.size() == 1) {
        command.add(sb.substring(1));
      } else {
        command.add("concat:" + sb.substring(1));
      }
      command.add("-y");
      command.add("eki_media/" + t);

      ProcessBuilder builder = new ProcessBuilder(command);
      Process process = builder.start();

      try (InputStream stream = process.getErrorStream(); ) {
        String out = IOUtils.toString(stream, "UTF-8");
        System.out.println(out);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
      throw new IllegalStateException(ex);
    }
  }
}
