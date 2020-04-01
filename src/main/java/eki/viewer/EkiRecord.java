package eki.viewer;

import java.util.List;

public class EkiRecord {
  private final String mp3FileName;

  private final String etTitleRaw;
  private final String etBodyRaw;
  private final String enEtBodyRaw;
  private final List<String> enEtEngTitles;

  private final String etTitleGrepable;
  private final String etBodyGrepable;
  private final String enEtEnGrepable;
  private final String enEtEtGrepable;

  public EkiRecord(
      String mp3FileName,
      String etTitleRaw,
      String etBodyRaw,
      String enEtBodyRaw,
      List<String> enEtEngTitles,
      String etTitleGrepable,
      String etBodyGrepable,
      String enEtEnGrepable,
      String enEtEtGrepable) {
    this.mp3FileName = mp3FileName;
    this.etTitleRaw = etTitleRaw;
    this.etBodyRaw = etBodyRaw;
    this.enEtBodyRaw = enEtBodyRaw;
    this.enEtEngTitles = enEtEngTitles;
    this.etTitleGrepable = etTitleGrepable;
    this.etBodyGrepable = etBodyGrepable;
    this.enEtEnGrepable = enEtEnGrepable;
    this.enEtEtGrepable = enEtEtGrepable;
  }

  public String getMp3FileName() {
    return mp3FileName;
  }

  public String getEtTitleRaw() {
    return etTitleRaw;
  }

  public String getEtBodyRaw() {
    return etBodyRaw;
  }

  public String getEnEtBodyRaw() {
    return enEtBodyRaw;
  }

  public String getEtTitleGrepable() {
    return etTitleGrepable;
  }

  public String getEtBodyGrepable() {
    return etBodyGrepable;
  }

  public String getEnEtEnGrepable() {
    return enEtEnGrepable;
  }

  public String getEnEtEtGrepable() {
    return enEtEtGrepable;
  }

  @Override
  public String toString() {
    return this.etTitleRaw
        + "<br><font color='#6f9f9f'>"
        + String.join(", ", enEtEngTitles)
        + "</font>";
  }
}
