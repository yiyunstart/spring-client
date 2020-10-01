package com.qr.scan;

import javax.swing.*;
import java.awt.*;

public class MyJcheckBox extends JCheckBox implements ListCellRenderer {
    public MyJcheckBox() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        this.setText(value.toString());
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        this.setSelected(isSelected);
        return this;
    }

}
