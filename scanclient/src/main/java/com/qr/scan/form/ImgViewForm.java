
package com.qr.scan.form;

import com.google.zxing.Result;
import com.qr.scan.MainApp;
import com.qr.scan.MyAppConst;
import com.qr.scan.SpringContextUtil;
import com.qr.scan.entity.Camera;
import com.qr.scan.entity.CameraPoint;
import com.qr.scan.mapper.CameraPointMapper;
import com.qr.scan.swing.ImagePanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*****************************************************************************
 *类 ：   JFrameImgviewControl
 *类描述 ：云台控制
 ****************************************************************************/
@Component
public class ImgViewForm extends JFrame {

    @Autowired
    private CameraPointMapper cameraPointMapper;

    JPanel concentJPanel = new JPanel();

    private static Map<Integer, BufferedImage> images = new HashMap<Integer, BufferedImage>();
    private static Map<Integer, ImagePanel> imageBoxs = new HashMap<Integer, ImagePanel>();
    private static int cols = 3;
    private static int rows = 2;

    private Map<String,Map<Integer,ImagePanel>> imagePanels = new HashMap<>();


    /*************************************************
     函数:      JFrameImgviewControl
     函数描述:	构造函数   Creates new form JFrameImgviewControl
     *************************************************/
    public ImgViewForm() {

        initComponents();
        this.setBounds(200, 200, 800, 600);
        this.setLocationRelativeTo(null);
    }

    private List<Camera> camera = null;

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        concentJPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 10));

//        for (int i = 1; i <= cols * rows; i++) {
//            ImagePanel imagePanel = new ImagePanel(i);
//            if (images.containsKey(i)) {
//                BufferedImage bufferedImage = images.get(i);
//                imagePanel.image = bufferedImage;
//            }
//            imagePanel.addMouseListener(new java.awt.event.MouseAdapter() {
//                public void mousePressed(MouseEvent evt) {
//                    fullShow(evt, imagePanel);
//                }
//            });
//
//            imageBoxs.put(i, imagePanel);
//            box.add(imagePanel);
//        }

        Container root = getContentPane();

        concentJPanel.setBackground(Color.GRAY);
        root.add(concentJPanel);    //按钮显示在
//            root.add(jPanelImg);	//图片显示在中间
//        root.setSize(600, 800);
        pack();
    }


    public void showImage(CameraPoint cameraPoint, BufferedImage image, Result[] results) {
        if (!imagePanels.containsKey(cameraPoint.getCameraIp()) ) {
            return;
        }
        if (!imagePanels.get(cameraPoint.getCameraIp()).containsKey(cameraPoint.getName()) ) {
            return;
        }
        ImagePanel imagePanel = imagePanels.get(cameraPoint.getCameraIp()).get(cameraPoint.getName());
        imagePanel.image = image;
        imagePanel.results = results;
        if(results ==null || results.length<cameraPoint.getTestQrCount()){
            imagePanel.showBorder = true;
        }
        imagePanel.repaint();


    }

    public void layoutCamera() {
        camera = SpringContextUtil.getBean(MainApp.class).camerasSelected;
        int width = camera.size() > 1 ? (concentJPanel.getWidth() - 30) / 2 : concentJPanel.getWidth() - 20;
        concentJPanel.removeAll();
        for (Camera camera : camera) {
            JPanel cameraPanel = new JPanel();
            cameraPanel.setBackground(Color.DARK_GRAY);
            cameraPanel.setPreferredSize(new Dimension(width, width));
            cameraPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 5));
            int imageWidth = (width - ((camera.getCols() + 1) * 5)) / camera.getCols();
            int imageHeight = (int)((float)imageWidth/ MyAppConst.video_width*MyAppConst.video_height);
            imagePanels.put(camera.getIp(),new HashMap<>());
            for (int i = 0; i < camera.getRows() * camera.getCols(); i++) {
                ImagePanel imagePanel = new ImagePanel(i);
                imagePanel.setPreferredSize(new Dimension(imageWidth,imageHeight));
//                if (images.containsKey(i)) {
//                    BufferedImage bufferedImage = images.get(i);
//                    imagePanel.image = bufferedImage;
//                }
                imagePanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mousePressed(MouseEvent evt) {
                        fullShow(evt, imagePanel);
                    }
                });

//                imageBoxs.put(i, imagePanel);
                imagePanels.get(camera.getIp()).put(i+1,imagePanel);
                cameraPanel.add(imagePanel);
            }
//            List<CameraPoint> cameraPointList = cameraPointMapper.selectByIp(camera.getIp());

            concentJPanel.add(cameraPanel);
        }
        Arrays.stream(concentJPanel.getComponents()).forEach(component -> {
            component.setPreferredSize(new Dimension(width, width));
        });
        concentJPanel.repaint();
        concentJPanel.revalidate();
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
