package eki.viewer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class AnkiSubsetUtils {
  private AnkiSubsetUtils() {}

  public static final String ANKI_SUBSET_DIR = "anki-subset/";
  private static final String COLLECTION_MEDIA_DIR = "collection.media/";

  public static void playMp3(EkiRecord ekiRecord) throws Exception {
    String mp3 = AnkiUtils.untagMp3FileName(ekiRecord.getMp3FileName());
    if (StringUtils.isEmpty(mp3)) {
      return;
    }
    File mp3File = new File(COLLECTION_MEDIA_DIR + mp3);
    if (!mp3File.exists()) {
      return;
    }
    CompletableFuture.runAsync(() -> new Mp3Player().testPlay(mp3File));
  }

  public static void appendToAnkiSubset(EkiRecord ekiRecord) throws IOException {
    String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
    new File(ANKI_SUBSET_DIR).mkdir();
    new File(ANKI_SUBSET_DIR + COLLECTION_MEDIA_DIR).mkdir();
    String ankiLine =
        String.format(
            "%s\t%s\t%s\t%s",
            ekiRecord.getEtTitleRaw(),
            ekiRecord.getMp3FileName(),
            ekiRecord.getEtBodyRaw(),
            ekiRecord.getEnEtBodyRaw());

    File ankiSubset = new File(ANKI_SUBSET_DIR + today + "-anki-tab.txt");
    if (!ankiSubset.exists()) {
      ankiSubset.createNewFile();
    }
    List<String> contents = FileUtils.readLines(ankiSubset, StandardCharsets.UTF_8);
    if (contents.parallelStream().noneMatch(s -> s.equals(ankiLine))) {
      FileUtils.writeStringToFile(
          new File(ANKI_SUBSET_DIR + today + "-anki-tab.txt"),
          ankiLine + "\n",
          StandardCharsets.UTF_8,
          true);
    }
    copyAudio(ekiRecord);
    copyImgs(ekiRecord);
  }

  private static void copyImgs(EkiRecord ekiRecord) throws IOException {
    List<String> imgs = AnkiUtils.extractImgs(ekiRecord.getEtBodyRaw());
    for (String img : imgs) {
      File imgFile = new File(COLLECTION_MEDIA_DIR + img);
      if (!imgFile.exists()) {
        return;
      }
      if (new File(ANKI_SUBSET_DIR + COLLECTION_MEDIA_DIR + img).exists()) {
        return;
      }
      FileUtils.copyFileToDirectory(imgFile, new File("anki-subset/collection.media/"));
    }
  }

  private static void copyAudio(EkiRecord ekiRecord) throws IOException {
    String mp3 = AnkiUtils.untagMp3FileName(ekiRecord.getMp3FileName());
    if (StringUtils.isEmpty(mp3)) {
      return;
    }
    File mp3File = new File(COLLECTION_MEDIA_DIR + mp3);
    if (!mp3File.exists()) {
      return;
    }
    if (new File(ANKI_SUBSET_DIR + COLLECTION_MEDIA_DIR + mp3).exists()) {
      return;
    }
    FileUtils.copyFileToDirectory(mp3File, new File(ANKI_SUBSET_DIR + COLLECTION_MEDIA_DIR));
  }
}
