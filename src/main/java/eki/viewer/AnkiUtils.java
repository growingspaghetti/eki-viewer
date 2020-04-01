package eki.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class AnkiUtils {
  private AnkiUtils() {}

  private static final Pattern ENG_PA = Pattern.compile("<b lang=\"en\">([^\\\\<]+?)</b>");
  private static final Pattern IMG_PA = Pattern.compile("<img.*? src=\"([^\"]+?)\"");

  public static EkiRecord convert(String ankiLine) {
    String[] columns = StringUtils.splitPreserveAllTokens(ankiLine, '\t');
    String enEtBodyRaw = columns[3];
    Matcher m = ENG_PA.matcher(enEtBodyRaw);
    List<String> englishWords = new ArrayList<>();
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      englishWords.add(m.group(1));
    }
    m.appendTail(sb);
    return new EkiRecord(
        columns[1], // mp3FileName
        columns[0], // etTitleRaw
        columns[2], // etBodyRaw
        enEtBodyRaw, // enEtBodyRaw
        englishWords, // enEtEngTitles
        grepableString(columns[0]), // etTitleGrepable
        grepableString(columns[2]), // etBodyGrepable
        grepableString(String.join(" ", englishWords)),
        grepableString(sb.toString()));
  }

  public static List<String> extractImgs(String html) {
    Matcher m = IMG_PA.matcher(html);
    List<String> imgs = new ArrayList<>();
    while (m.find()) {
      imgs.add(m.group(1));
    }
    return imgs;
  }

  public static String grepableString(String s) {
    return StringEscapeUtils.unescapeHtml(s)
        .replaceAll("\\<.*?\\>", "")
        .toLowerCase()
        .replace('š', 's')
        .replace('ž', 'z')
        .replace('õ', 'o')
        .replace('ä', 'a')
        .replace('ö', 'o')
        .replace('ü', 'u')
        .replace("`", "");
  }

  public static final String buildHtmlView(EkiRecord ekiRecord) {
    return "<html><div style=\"padding:4px\">"
        + "<h1>"
        + ekiRecord.getEtTitleRaw()
        + "</h1>"
        + "<hr/>"
        + ekiRecord
            .getEtBodyRaw()
            .replace("small>", ">")
            .replace("src=\"eki_", "src=\"file:collection.media/eki_")
        + "<hr/>"
        + ekiRecord.getEnEtBodyRaw()
        + "</div></html>";
  }

  public static String untagMp3FileName(String ankiSoundTag) {
    return ankiSoundTag.replace("[sound:", "").replace("]", "");
  }
}
