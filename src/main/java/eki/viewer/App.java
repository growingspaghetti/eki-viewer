package eki.viewer;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.DefaultEditorKit;

import org.apache.commons.io.FileUtils;

public class App extends JFrame {
  private Optional<EkiRecord> currentEkiRecord = Optional.empty();

  private DefaultListModel<EkiRecord> model = new DefaultListModel<>();
  private JList<EkiRecord> list = new JList<>(model);
  private List<EkiRecord> ekiRecords = new ArrayList<>();

  JEditorPane jEditorPane = new JEditorPane();
  JTextField searchField = new JTextField();
  JLabel statusLabel = new JLabel("<html>&nbsp;</html>");
  JCheckBox playAudioCheck = new JCheckBox("Play audio");
  JCheckBox ankiCheck = new JCheckBox("Create Anki subset");
  JButton openEkiWebButton = new JButton("Open in web");
  JButton openAnkiSubsetButton = new JButton("Open Anki subset");

  private App(File ankiFile) throws Exception {
    init(ankiFile);
  }

  private void init(File ankiFile) throws IOException {
    setTitle("Eesti Keele Instituut, Basic Estonian Words");
    List<String> ankiLines = FileUtils.readLines(ankiFile, StandardCharsets.UTF_8);
    CompletableFuture.runAsync(
        () -> {
          ankiLines
              .stream()
              .map(AnkiUtils::convert)
              .peek(ekiRecords::add)
              .forEach(e -> SwingUtilities.invokeLater(() -> model.addElement(e)));
        });

    new RepeatingReleasedEventsFixer().install();
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setCellRenderer(new ListCellRenderableLabel());
    Arrays.asList(list.getKeyListeners()).forEach(list::removeKeyListener);

    JPopupMenu menu = new JPopupMenu();
    Action copy = new DefaultEditorKit.CopyAction();
    copy.putValue(Action.NAME, "Copy");
    copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
    menu.add(copy);

    jEditorPane.setContentType("text/html");
    jEditorPane.setEditable(false);
    jEditorPane.setFont(new Font("Arial", Font.PLAIN, 13));
    jEditorPane.setComponentPopupMenu(menu);
    jEditorPane.setText(
        "<html><div style=\"padding:4px\"><ul>"
            + "<li>press ENTER to search both Estonian and English titles</li>"
            + "<li>press CTRL + ENTER to search English titles</li>"
            + "<li>press SHIFT + ENTER to search Estonian titles</li>"
            + "<li>press CTRL + SHIFT + ENTER to search in the entire Estonian sentences</li>"
            + "</ul><hr>Subset of Anki flashcards is created in the directory <i>anki-subset</i></div></html>");

    JPanel browserPanel = new JPanel();
    browserPanel.setLayout(new BorderLayout());
    browserPanel.add(new JScrollPane(jEditorPane), BorderLayout.CENTER);

    JPanel searchPanel = new JPanel();
    searchPanel.setLayout(new BorderLayout());
    searchPanel.add(searchField, BorderLayout.CENTER);
    Arrays.asList(searchField.getKeyListeners()).forEach(l -> searchField.removeKeyListener(l));

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BorderLayout());
    listPanel.add(searchPanel, BorderLayout.NORTH);
    listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
    listPanel.setPreferredSize(new Dimension(300, 800));
    JSplitPane jSplitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    jSplitPane1.setLeftComponent(listPanel);
    jSplitPane1.setRightComponent(browserPanel);

    this.setLayout(new BorderLayout());
    this.add(jSplitPane1, BorderLayout.CENTER);

    JPanel statusPanel = new JPanel();
    JPanel checkBoxPanel = new JPanel();
    statusPanel.setLayout(new BorderLayout());
    checkBoxPanel.setLayout(new BorderLayout());
    statusPanel.add(statusLabel, BorderLayout.CENTER);
    playAudioCheck.setSelected(true);
    checkBoxPanel.add(playAudioCheck, BorderLayout.EAST);
    ankiCheck.setSelected(true);
    checkBoxPanel.add(ankiCheck, BorderLayout.CENTER);
    statusPanel.add(checkBoxPanel, BorderLayout.EAST);
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(openEkiWebButton, BorderLayout.WEST);
    buttonPanel.add(openAnkiSubsetButton, BorderLayout.CENTER);
    statusPanel.add(buttonPanel, BorderLayout.WEST);
    this.add(statusPanel, BorderLayout.SOUTH);
    this.setPreferredSize(new Dimension(1000, 800));
    this.pack();

    applySelectionListener();
    appplyButtonEvent();
    applyKeyListener();
  }

  private void applyKeyListener() {
    searchField.addKeyListener(
        new KeyListener() {
          @Override
          public void keyPressed(KeyEvent e) {}

          @Override
          public void keyReleased(KeyEvent e) {
            String searchText = AnkiUtils.grepableString(searchField.getText());
            if ((e.getKeyCode() == KeyEvent.VK_ENTER)
                && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)
                && ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0)) {
              GrepUtils.etDeepGrep(searchText, model, ekiRecords);
            } else if ((e.getKeyCode() == KeyEvent.VK_ENTER)
                && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
              GrepUtils.enGrep(searchText, model, ekiRecords);
            } else if ((e.getKeyCode() == KeyEvent.VK_ENTER)
                && ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0)) {
              GrepUtils.etShallowGrep(searchText, model, ekiRecords);
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              GrepUtils.facadeGrep(searchText, model, ekiRecords);
            }
          }

          @Override
          public void keyTyped(KeyEvent e) {}
        });
  }

  private void appplyButtonEvent() {
    openEkiWebButton.addActionListener(
        (ActionEvent e) -> {
          String title = currentEkiRecord.map(EkiRecord::getEtTitleRaw).orElse("");
          try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(
                URI.create(
                    "http://www.eki.ee/dict/psv/index.cgi?F=M&Q=" + title.replace(" ", "%20")));
          } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "", ex);
          }
        });
    openAnkiSubsetButton.addActionListener(
        (ActionEvent e) -> {
          if (!new File(AnkiSubsetUtils.ANKI_SUBSET_DIR).exists()) {
            JOptionPane.showMessageDialog(this, "Anki subsets don't exist");
            return;
          }
          try {
            SubsetTabbedPane subsetLists = new SubsetTabbedPane();
            subsetLists.setPreferredSize(new Dimension(600, 800));
            subsetLists.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
            subsetLists.pack();
            subsetLists.setLocationRelativeTo(this);
            subsetLists.setVisible(true);
          } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "", ex);
          }
        });
  }

  private void applySelectionListener() {
    list.addListSelectionListener(
        (ListSelectionEvent e) -> {
          if (e.getValueIsAdjusting()) {
            return;
          }
          EkiRecord ekiRecord = list.getSelectedValue();
          currentEkiRecord = Optional.ofNullable(ekiRecord);
          if (!currentEkiRecord.isPresent()) {
            return;
          }
          jEditorPane.setText(AnkiUtils.buildHtmlView(ekiRecord));
          jEditorPane.setCaretPosition(0);

          if (playAudioCheck.isSelected()) {
            try {
              AnkiSubsetUtils.playMp3(ekiRecord);
            } catch (Exception ex) {
              Logger.getLogger(App.class.getName()).log(Level.SEVERE, "", ex);
            }
          }

          if (ankiCheck.isSelected()) {
            try {
              AnkiSubsetUtils.appendToAnkiSubset(ekiRecord);
            } catch (Exception ex) {
              Logger.getLogger(App.class.getName()).log(Level.SEVERE, "", ex);
            }
          }
        });
  }

  public static void main(String[] args) {
    File ankiFile = args.length == 4 ? new File(args[0]) : new File("eki-anki-tab.txt");
    int defaultCloseOperation = args.length == 4 ? Integer.parseInt(args[1]) : JFrame.EXIT_ON_CLOSE;
    SwingUtilities.invokeLater(
        () -> {
          try {
            App app = new App(ankiFile);
            app.setDefaultCloseOperation(defaultCloseOperation);
            app.setVisible(true);
            if (args.length == 4) {
              app.ankiCheck.setSelected(false);
              app.ankiCheck.setEnabled(false);
              app.openAnkiSubsetButton.setEnabled(false);
              app.setLocation(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
              app.setTitle(app.getTitle() + " | " + new File(args[0]).getName());
            } else {
              app.setLocationRelativeTo(null);
            }
          } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "", ex);
          }
        });
  }
}
