package eki.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

public class SubsetTabbedPane extends JFrame {
  public SubsetTabbedPane() throws IOException {
    init();
  }

  private void init() throws IOException {
    this.setLayout(new BorderLayout());
    JTabbedPane jTabbedPane = new JTabbedPane();
    List<File> subsets =
        Arrays.asList(new File(AnkiSubsetUtils.ANKI_SUBSET_DIR).listFiles())
            .stream()
            .sorted()
            .filter(
                f -> f.getName().matches("[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-anki-tab\\.txt"))
            .collect(Collectors.toList());
    Collections.reverse(subsets);

    for (File f : subsets) {
      DefaultListModel<EkiRecord> model = new DefaultListModel<>();
      JList<EkiRecord> list = new JList<>(model);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setCellRenderer(new ListCellRenderableLabel());
      Arrays.asList(list.getKeyListeners()).forEach(list::removeKeyListener);

      JPopupMenu popup =
          new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
              EkiRecord ekiRecord = list.getSelectedValue();
              if (!Optional.ofNullable(ekiRecord).isPresent()) {
                return;
              }
              super.show(invoker, x, y);
            }
          };
      JMenuItem menuItem = new JMenuItem("Remove");
      menuItem.addActionListener(
          (ActionEvent a) -> {
            EkiRecord ekiRecord = list.getSelectedValue();
            if (!Optional.ofNullable(ekiRecord).isPresent()) {
              return;
            }
            model.removeElement(ekiRecord);
            try {
              AnkiSubsetUtils.remove(ekiRecord, f);
            } catch (IOException ex) {
              Logger.getLogger(SubsetTabbedPane.class.getName()).log(Level.SEVERE, "", ex);
            }
          });
      popup.add(menuItem);
      list.setComponentPopupMenu(popup);

      List<String> ankiLines = FileUtils.readLines(f, StandardCharsets.UTF_8);
      CompletableFuture.runAsync(
          () ->
              ankiLines
                  .stream()
                  .map(AnkiUtils::convert)
                  .forEach(e -> SwingUtilities.invokeLater(() -> model.addElement(e))));
      JPanel jPanel = new JPanel(new BorderLayout());
      jPanel.add(new JScrollPane(list), BorderLayout.CENTER);
      JButton jButton = new JButton("Open this subset");
      jPanel.add(jButton, BorderLayout.NORTH);
      jButton.addActionListener(
          (ActionEvent e) ->
              CompletableFuture.runAsync(
                  () ->
                      App.main(
                          new String[] {
                            f.getAbsolutePath(),
                            Integer.toString(javax.swing.WindowConstants.HIDE_ON_CLOSE),
                            Integer.toString(this.getLocation().x),
                            Integer.toString(this.getLocation().y)
                          })));
      jTabbedPane.addTab(f.getName().replace("-anki-tab.txt", ""), jPanel);
    }
    this.add(jTabbedPane, BorderLayout.CENTER);
  }

  public static void main(String[] args) throws IOException {
    SwingUtilities.invokeLater(
        () -> {
          try {
            SubsetTabbedPane app = new SubsetTabbedPane();
            app.setPreferredSize(new Dimension(1000, 800));
            app.pack();
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            app.setLocationRelativeTo(null);
            app.setVisible(true);
          } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "", ex);
          }
        });
  }
}
