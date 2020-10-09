package com.qr.scan.swing;

import javax.swing.*;

public class MultipleListSelectionModel extends DefaultListSelectionModel {
    boolean gestureStarted = false;

    @Override
    public void setSelectionInterval(int index0, int index1) {
        System.out.println(index0 + ":" + index1);
        if (!gestureStarted) {
            if (index0 >= 0 && super.isSelectedIndex(index0)) {
                super.removeSelectionInterval(index0, index1);
            } else {
                super.addSelectionInterval(index0, index1);
            }
        }
        gestureStarted = true;

    }

    @Override
    public void setValueIsAdjusting(boolean isAdjusting) {
        if (isAdjusting == false) {
            gestureStarted = false;
        }
    }
}
