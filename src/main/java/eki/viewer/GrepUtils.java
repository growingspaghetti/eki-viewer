package eki.viewer;

import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;

import org.apache.commons.lang.StringUtils;

public class GrepUtils {
  private GrepUtils() {}

  public static void facadeGrep(
      String searchText, DefaultListModel<EkiRecord> model, List<EkiRecord> ekiRecords) {
    model.removeAllElements();
    if (StringUtils.isEmpty(searchText)) {
      ekiRecords.forEach(model::addElement);
      return;
    }
    ekiRecords
        .parallelStream()
        .filter(
            ek ->
                StringUtils.contains(ek.getEtTitleGrepable(), searchText)
                    || StringUtils.contains(ek.getEnEtEnGrepable(), searchText))
        .forEachOrdered(model::addElement);
  }

  public static void etShallowGrep(
      String searchText, DefaultListModel<EkiRecord> model, List<EkiRecord> ekiRecords) {
    model.removeAllElements();
    if (StringUtils.isEmpty(searchText)) {
      ekiRecords.forEach(model::addElement);
      return;
    }
    ekiRecords
        .parallelStream()
        .filter(ek -> StringUtils.startsWith(ek.getEtTitleGrepable(), searchText))
        .forEachOrdered(model::addElement);
  }

  public static void etDeepGrep(
      String searchText, DefaultListModel<EkiRecord> model, List<EkiRecord> ekiRecords) {
    model.removeAllElements();
    if (StringUtils.isEmpty(searchText)) {
      ekiRecords.forEach(model::addElement);
      return;
    }
    ekiRecords
        .parallelStream()
        .filter(
            ek ->
                StringUtils.contains(ek.getEtTitleGrepable(), searchText)
                    || StringUtils.contains(ek.getEtBodyGrepable(), searchText)
                    || StringUtils.contains(ek.getEnEtEtGrepable(), searchText))
        .forEachOrdered(model::addElement);
  }

  public static void enGrep(
      String searchText, DefaultListModel<EkiRecord> model, List<EkiRecord> ekiRecords) {
    model.removeAllElements();
    if (StringUtils.isEmpty(searchText)) {
      ekiRecords.forEach(model::addElement);
      return;
    }
    ekiRecords
        .parallelStream()
        .filter(
            ek ->
                Arrays.asList(StringUtils.split(ek.getEnEtEnGrepable(), " "))
                    .parallelStream()
                    .anyMatch(w -> StringUtils.startsWith(w, searchText)))
        .forEachOrdered(model::addElement);
  }
}
