
package com.qr.scan;

import com.google.zxing.Result;
import com.qr.scan.utils.HCNetSDK;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/*****************************************************************************
 *类 ：   JFrameImgviewControl
 *类描述 ：云台控制
 ****************************************************************************/
@Component
public class JFrameImgviewControl extends JFrame {

    /************成员变量*****************/
    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    static Map<Integer, BufferedImage> images = new HashMap<Integer, BufferedImage>();
    static Map<Integer, ImagePanel> imageBoxs = new HashMap<Integer, ImagePanel>();
    static int cols = 3;
    static int rows = 2;

    /*************************************************
     函数:      JFrameImgviewControl
     函数描述:	构造函数   Creates new form JFrameImgviewControl
     *************************************************/
    public JFrameImgviewControl() {

        initComponents();
        this.setBounds(200,200,800,600);
        this.setLocationRelativeTo(null);
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        JPanel box = new JPanel();
        box.setLayout(new GridLayout(rows, cols, 10, 10));

        for (int i = 1; i <= cols * rows; i++) {
            ImagePanel imagePanel = new ImagePanel(i);
            if (images.containsKey(i)) {
                BufferedImage bufferedImage = images.get(i);
                imagePanel.image = bufferedImage;
            }
            imagePanel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(MouseEvent evt) {
                    fullShow(evt, imagePanel);
                }
            });

            imageBoxs.put(i, imagePanel);
            box.add(imagePanel);
        }

        Container root = getContentPane();

        box.setBackground(Color.orange);
        root.add(box);    //按钮显示在
//            root.add(jPanelImg);	//图片显示在中间
//        root.setSize(600, 800);
        pack();
    }

    public void showImage(int i, BufferedImage image, Result[] results) {
        if (imageBoxs.containsKey(i)) {
            images.put(i, image);
            imageBoxs.get(i).image = image;
            imageBoxs.get(i).results = results;
            imageBoxs.get(i).repaint();
        }
    }


    public class ImagePanel extends JPanel {

        private BufferedImage image = null;
        private Result[] results = null;
        private Integer num = 1;

        public ImagePanel(Integer num) {
            this.num = num;
        }

        @Override
        protected void paintComponent(Graphics g) {
            //获得窗口的宽高
            int width = this.getWidth();
            int height = this.getHeight();
            this.setBackground(Color.PINK);
            //清除控件显示，在下次重新绘制的时候清除当前显示，不然会出现图片重叠现象
            g.clearRect(0, 0, width, height);
            if (image != null) {
                g.drawImage(image, 0, 0, width, height, null);
                g.setColor(Color.RED);
                g.drawString("图片:" + num, 10, 10);
            }
            if (results != null) {
                for (int i = 1; i <= results.length; i++) {
                    g.drawString("二维码" + i + ":" + results[i - 1].getText(), 10, i * 20 + 10);
                }
            }

        }


    }

    /*************************************************
     函数描述:   双击全屏预览当前预览通道
     *************************************************/
    private void fullShow(MouseEvent evt, ImagePanel imagePanel)//GEN-FIRST:event_panelRealplayMousePressed
    {//GEN-HEADEREND:event_panelRealplayMousePressed

        //鼠标单击事件为双击
        if (evt.getClickCount() == 2) {
            JFrameImgviewFullControl fullControl = new JFrameImgviewFullControl(imagePanel.num, imagePanel.image, imagePanel.results);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle bounds = new Rectangle(screenSize);
            fullControl.setBounds(bounds);
            fullControl.setVisible(true);
        }
    }


    class JFrameImgviewFullControl extends JFrame {
        public JFrameImgviewFullControl(Integer num, BufferedImage image, Result[] results) {
            getContentPane().setSize(600, 800);
            this.num = num;
            this.image = image;
            this.results = results;
            initComponents();
        }

        private BufferedImage image = null;
        private Result[] results = null;
        private Integer num = 1;

        @SuppressWarnings("unchecked")
        private void initComponents() {

            ImagePanel imagePanel = new ImagePanel(this.num);
            imagePanel.image = this.image;
            imagePanel.results = this.results;

            Container root = getContentPane();
            root.add(imagePanel);
            pack();
        }

    }
}
