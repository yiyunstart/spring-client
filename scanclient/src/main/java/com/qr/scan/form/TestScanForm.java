package com.qr.scan.form;

import com.google.zxing.Result;
import com.qr.scan.swing.ImagePanel;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Component
public class TestScanForm extends JFrame {
    public TestScanForm( ) {
        initComponents();
    }
    ImagePanel imagePanel = null;
    @SuppressWarnings("unchecked")
    private void initComponents() {

        imagePanel = new ImagePanel(1);


        Container root = getContentPane();
        root.add(imagePanel);
        pack();
    }

    public void showImage(BufferedImage image,Result[] results){
        imagePanel.image = image;
        imagePanel.results = results;
        imagePanel.repaint();

    }
}
