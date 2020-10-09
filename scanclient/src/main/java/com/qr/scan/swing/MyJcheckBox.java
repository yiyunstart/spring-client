package com.qr.scan.swing;

import com.qr.scan.entity.Camera;

import javax.swing.*;
import java.awt.*;

public class MyJcheckBox extends JCheckBox implements ListCellRenderer {
    public MyJcheckBox() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        this.setText(((Camera)value).getIp());
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        this.setSelected(isSelected);
        return this;
    }

}
