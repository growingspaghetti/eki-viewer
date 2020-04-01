package eki.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ListCellRenderableLabel extends JLabel implements ListCellRenderer<Object> {
  private static final long serialVersionUID = 1L;

  public ListCellRenderableLabel() {
    setOpaque(true);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Component getListCellRendererComponent(
      JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    setText("<html><div style=\"padding:4px\">" + value.toString() + "</div></html>");//<div style=\"font-size:14px;\">"
    if (isSelected) {
      setBackground(Color.YELLOW);
    } else if (index % 2 == 0) {
      setBackground(new Color(246, 246, 246));
    } else {
      setBackground(Color.WHITE);
    }
    this.setFont(new Font("Arial", Font.PLAIN, 13));
    return this;
  }
}
