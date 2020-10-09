package com.qr.scan.swing;

import com.google.zxing.Result;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {

    public BufferedImage image = null;
    public Result[] results = null;
    public Integer num = 1;
    public boolean showBorder = false;

    public ImagePanel(Integer num) {
        this.num = num;
    }

    @Override
    protected void paintComponent(Graphics g) {
        //获得窗口的宽高
        int width = this.getWidth();
        int height = this.getHeight();
        this.setBackground(Color.PINK);
        Graphics2D g2 = (Graphics2D)g;
        //清除控件显示，在下次重新绘制的时候清除当前显示，不然会出现图片重叠现象
        g.clearRect(0, 0, width, height);
        if (image != null) {
            g.drawImage(image, 0, 0, width, height, null);
            g.setColor(Color.RED);
            g.drawString("扫描点:" + num, 10, 10);
            if(showBorder){
                float thickness = 3; //设置宽度
                Stroke oldStroke = g2.getStroke(); //保存旧笔触
                g2.setStroke(new BasicStroke(thickness)); //设置新笔触
                g2.drawRect(0, 0, (int) (width-thickness), (int) (height-thickness)); //绘制矩形
                g2.setStroke(oldStroke);  //恢复旧笔触
             }
        }
        if (results != null) {
            for (int i = 1; i <= results.length; i++) {
                g.drawString("二维码" + i + ":" + results[i - 1].getText(), 10, i * 20 + 10);
            }
        }


    }


}