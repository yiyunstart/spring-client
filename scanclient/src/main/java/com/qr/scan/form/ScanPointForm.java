package com.qr.scan.form;

import com.google.zxing.Result;
import com.qr.scan.MyAppConst;
import com.qr.scan.swing.MyJcheckBox;
import com.qr.scan.VideoPanel;
import com.qr.scan.entity.Camera;
import com.qr.scan.entity.CameraPoint;
import com.qr.scan.mapper.CameraMapper;
import com.qr.scan.mapper.CameraPointMapper;
import com.qr.scan.utils.HCNetSDK;
import com.qr.scan.utils.HCNetUtils;
import com.qr.scan.utils.QrcodeUtils;
import com.sun.deploy.panel.NumberDocument;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.examples.win32.W32API;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 摄像头配置
 */
@Component
public class ScanPointForm extends JFrame {
    private static JPanel contentPanel = new JPanel();    //创建面板
    private static Map<Integer, JPanel> previewPanels = new HashMap<Integer, JPanel>();    //创建面板
    private static Map<Integer, VideoPanel> previewVideoPanels = new HashMap<Integer, VideoPanel>();    //创建面板
    private static Map<Integer, JButton> previewCancelBtns = new HashMap<Integer, JButton>();    //创建面板
    private static Map<Integer, JButton> previewExecBtns = new HashMap<Integer, JButton>();    //创建面板
    private static String EXEC_EVENT_LOADVIDEO = "loadVideo";
    private static String EXEC_EVENT_SAVE = "save";

    private static JScrollPane scrollPane = new JScrollPane(contentPanel);
    private JList<Camera> list = new JList<Camera>();
    private JFrame thiz = null;
    @Autowired
    private CameraMapper cameraMapper;
    @Autowired
    private CameraPointMapper cameraPointMapper;
    @Autowired
    private TestScanForm testScanForm;
    @Autowired
    private MyAppConst myAppConst;
    @Autowired
    private HCNetUtils netUtils;

    private JTextField rowsText = new JTextField();
    private JTextField colsText = new JTextField();
    private int rows = 3, cols = 3; //网格行数和列数，对于摄像头点的布局
//    private String ip = null;


    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    HCNetSDK.NET_DVR_CLIENTINFO m_strClientInfo;//用户参数

    boolean bRealPlay;//是否在预览.

    NativeLong lUserID;//用户句柄

//    int previewWidth = (contentPanel.getWidth() - 20) / rows;
//    int previewWidthFull = contentPanel.getWidth() - 10;

    int previewMarginWidth = 5;
    private static boolean showFull = false;
    private static JPanel showFullJPanel = null;
    private static Panel showFullPanel = null;

    private int m_iBrightness = 6; //亮度
    private int m_iContrast = 6; //对比度
    private int m_iSaturation = 6; //饱和度
    private int m_iHue = 6; //色度


    private Camera camera = null; //当前选中的摄像头
    private List<CameraPoint> cameraPoints = null; //当前选中的摄像头的扫描点


    public ScanPointForm() {
        thiz = this;
        initComponents();
        this.setBounds(200, 200, 900, 600);

        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                reLayoutPoint();

            }
        });

    }


    private void reLayoutPoint() {


        //计算内容布局大小
        if (showFull) {
            int width = (scrollPane.getWidth() - 20);

            //不能小于200 ，要不太丑了
            if (width < 200) {
                width = 200;
            }

            int height = (int) ((float) width / myAppConst.video_width * myAppConst.video_height);
            showFullPanel.setPreferredSize(new Dimension(width,height));
            showFullJPanel.setPreferredSize(
                    new Dimension(width, height + 60));
            contentPanel.setPreferredSize( new Dimension(width, height + 60));
        } else {

            int width = (scrollPane.getWidth() - (previewMarginWidth * (this.cols + 1)) - 20);

            //计算每个扫描点的显宽度
            int previewWidth = width / this.cols;

            //不能小于200 ，要不太丑了
            if (previewWidth < 200) {
                previewWidth = 200;
                width = previewWidth * this.cols+(previewMarginWidth * (this.cols + 1))+20;
            }

            int previewHeight = (int) ((float) previewWidth / myAppConst.video_width * myAppConst.video_height);

            final int PreviewWidth = previewWidth;
            previewVideoPanels.forEach((key, value) -> {
                value.setPreferredSize(new Dimension(PreviewWidth, previewHeight));
            });

            previewPanels.forEach((key, value) -> {
                value.setPreferredSize(new Dimension(PreviewWidth, previewHeight + 60));
            });

            contentPanel.setPreferredSize(new Dimension(width, (previewHeight + 60 + (previewMarginWidth * (this.rows + 1))) * this.rows));
        }
        contentPanel.repaint();
        contentPanel.revalidate();
        thiz.repaint();
        thiz.revalidate();

    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        loadCrameraList();
    }


    JSlider jSliderContrast = new JSlider();
    JSlider jSliderSaturation = new JSlider();

    JSlider jSliderHue = new JSlider();
    JSlider jSliderBright = new JSlider();

    JTextField QrcodeCountText = new JTextField("0");

    private void initComponents() {

        //向JPanel添加FlowLayout布局管理器，将组件间的横向和纵向间隙都设置为20像素
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 5, 5);
        contentPanel.setLayout(flowLayout);
        contentPanel.setBackground(Color.gray);    //设置背景色


        super.getContentPane().setLayout(new BorderLayout());    //使用GridBagLayout布局

        MyJcheckBox cell = new MyJcheckBox();
        list.setCellRenderer(cell);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new SharedListSelectionHandler());
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 100));
        leftPanel.setBorder(BorderFactory.createTitledBorder("摄像头"));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(list);
        super.getContentPane().add(leftPanel, BorderLayout.WEST);

        super.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new FlowLayout());
        rightPanel.setPreferredSize(new Dimension(220, 0));


        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("网格大小"));
        panel.setPreferredSize(new Dimension(200, 60));
        rowsText.setPreferredSize(new Dimension(40, 28));
        rowsText.setHorizontalAlignment(JTextField.CENTER);
        rowsText.setDocument(new NumberDocument());
//        rowsText.setText(String.valueOf(rows));
        JLabel portLabel = new JLabel("x");
        colsText.setPreferredSize(new Dimension(40, 28));
        colsText.setHorizontalAlignment(JTextField.CENTER);
        colsText.setDocument(new NumberDocument());
//        colsText.setText(String.valueOf(cols));

        JButton addButton = new JButton("设置");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rows = Integer.valueOf(rowsText.getText());
                cols = Integer.valueOf(colsText.getText());
                cameraMapper.updateGrid(camera.getIp(), rows, cols);
                //重新加载数据
                camera =  cameraMapper.selectByIp(camera.getIp());
                cameraPoints = cameraPointMapper.selectByIp(camera.getIp());
                reLoadPoint();
            }
        });

        panel.add(rowsText);
        panel.add(portLabel);
        panel.add(colsText);
        panel.add(addButton);
        rightPanel.add(panel);


        JButton leftUpBtn = new JButton("左上");
        leftUpBtn.addMouseListener(new LeftUpAction());

        JButton leftBtn = new JButton("左");
        leftBtn.addMouseListener(new LeftMouseAction());

        JButton leftDownBtn = new JButton("左下");
        leftDownBtn.addMouseListener(new LeftDownMouseAction());
        JButton upBtn = new JButton("上");
        upBtn.addMouseListener(new UpMouseAction());
        JButton rightUpBtn = new JButton("右上");
        rightUpBtn.addMouseListener(new RightUpMouseAction());
        JButton rightBtn = new JButton("右");
        rightBtn.addMouseListener(new RightMouseAction());


        JButton rightDownBtn = new JButton("右下");
        rightDownBtn.addMouseListener(new RightDownAction());
        JButton downBtn = new JButton("下");
        downBtn.addMouseListener(new DownMouseAction());


        JPanel movePanel = new JPanel();
        movePanel.setLayout(new GridLayout(3, 6, 5, 5));

        movePanel.setBorder(BorderFactory.createTitledBorder("方向"));
        movePanel.add(leftUpBtn);
        movePanel.add(upBtn);
        movePanel.add(rightUpBtn);
        movePanel.add(leftBtn);
        movePanel.add(new JLabel());
        movePanel.add(rightBtn);
        movePanel.add(leftDownBtn);
        movePanel.add(downBtn);
        movePanel.add(rightDownBtn);
        movePanel.setPreferredSize(new Dimension(220, 120));
        rightPanel.add(movePanel);

        JPanel csPanel = new JPanel();
        csPanel.setLayout(new GridLayout(3, 6, 5, 5));
        csPanel.setBorder(BorderFactory.createTitledBorder("参数"));
        csPanel.setPreferredSize(new Dimension(200, 120));

        JButton tjBtn_1 = new JButton("缩");
        tjBtn_1.addMouseListener(new ZoomInAction());
        JButton tjBtn_2 = new JButton("伸");
        tjBtn_2.addMouseListener(new ZoomOutAction());

        JButton jjBtn_1 = new JButton("近");
        jjBtn_1.addMouseListener(new FocusNearAction());

        JButton jjBtn_2 = new JButton("远");
        jjBtn_2.addMouseListener(new FocusFarAction());
        JButton gqBtn_1 = new JButton("大");
        gqBtn_1.addMouseListener(new IrisOpenAction());

        JButton gqBtn_2 = new JButton("小");
        gqBtn_2.addMouseListener(new IrisCloseAction());
        csPanel.add(new JLabel("调焦"));
        csPanel.add(tjBtn_1);
        csPanel.add(tjBtn_2);
        csPanel.add(new JLabel("聚焦"));
        csPanel.add(jjBtn_1);
        csPanel.add(jjBtn_2);
        csPanel.add(new JLabel("光圈"));
        csPanel.add(gqBtn_1);
        csPanel.add(gqBtn_2);
        rightPanel.add(csPanel);

        JPanel yzdPanel = new JPanel();
        yzdPanel.setLayout(new FlowLayout(FlowLayout.LEADING,5,5));
        yzdPanel.setBorder(BorderFactory.createTitledBorder("扫码测试"));
        yzdPanel.setPreferredSize(new Dimension(200, 80));
        JLabel label = new JLabel("数量");
        QrcodeCountText.setPreferredSize(new Dimension(60, 28));
        JButton yzd_dyBtn = new JButton("扫描");
        yzdPanel.add(label);
        yzdPanel.add(QrcodeCountText);
        yzdPanel.add(yzd_dyBtn);

        yzd_dyBtn.addActionListener(new TestScanQrcodeAction());

        rightPanel.add(yzdPanel);

        JPanel jPanelVideoPara = new JPanel();


        jPanelVideoPara.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelVideoPara.setLayout(new FlowLayout());
        jPanelVideoPara.setPreferredSize(new Dimension(200, 200));

        Dimension dimensionLabel = new Dimension(40, 28);
        Dimension dimensionSlide = new Dimension(130, 28);
        JLabel jLabel5 = new JLabel("对比度");
        jLabel5.setPreferredSize(dimensionLabel);
        jPanelVideoPara.add(jLabel5);
        jSliderContrast.setPreferredSize(dimensionSlide);

        jSliderContrast.setMaximum(10);
        jSliderContrast.setMinimum(1);
        jSliderContrast.setValue(6);
        jSliderContrast.addChangeListener(new SliderChangeListener(false));
        jPanelVideoPara.add(jSliderContrast);


        JLabel jLabel6 = new JLabel("饱和度");
        jLabel6.setPreferredSize(dimensionLabel);
        jPanelVideoPara.add(jLabel6);
        jSliderSaturation.setPreferredSize(dimensionSlide);

        jSliderSaturation.setMaximum(10);
        jSliderSaturation.setMinimum(1);
        jSliderSaturation.setValue(6);
        jSliderSaturation.addChangeListener(new SliderChangeListener(false));
        jPanelVideoPara.add(jSliderSaturation);

        JLabel jLabel7 = new JLabel("色度");
        jLabel7.setPreferredSize(dimensionLabel);
        jPanelVideoPara.add(jLabel7);
        jSliderHue.setPreferredSize(dimensionSlide);

        jSliderHue.setMaximum(10);
        jSliderHue.setMinimum(1);
        jSliderHue.setValue(6);
        jSliderHue.addChangeListener(new SliderChangeListener(false));
        jPanelVideoPara.add(jSliderHue);

        JLabel jLabel9 = new JLabel("亮度");
        jLabel9.setPreferredSize(dimensionLabel);
        jPanelVideoPara.add(jLabel9);
        jSliderBright.setPreferredSize(dimensionSlide);
        jSliderBright.setMaximum(10);
        jSliderBright.setMinimum(1);
        jSliderBright.setValue(6);
        jSliderBright.addChangeListener(new SliderChangeListener(false));
        jPanelVideoPara.add(jSliderBright);
        rightPanel.add(jPanelVideoPara);

        JButton jButtonDefault = new JButton("默认值");
        jButtonDefault.addActionListener(new SliderChangeListener(true));
        jPanelVideoPara.add(jButtonDefault);


        super.getContentPane().add(rightPanel, BorderLayout.EAST);

    }

    private CameraPoint getListByName(List<CameraPoint> cameraPoints, int name) {
        for (int i = 0; i < cameraPoints.size(); i++) {
            if (cameraPoints.get(i).getName()==name) {
                return cameraPoints.get(i);
            }
        }
        return null;
    }


    public void reLoadPoint() {

        this.rows = camera.getRows();
        this.cols = camera.getCols();
        rowsText.setText(String.valueOf(this.rows));
        colsText.setText(String.valueOf(this.cols));


//        previewWidth = (contentPanel.getWidth() - 20) / rows;
//        previewWidth = previewWidth < 200 ? 200 : previewWidth;
//        previewWidthFull = contentPanel.getWidth() - 10;
//        previewWidthFull = previewWidthFull < 200 ? 200 : previewWidthFull;
//
//        contentPanelWidth = scrollPane.getWidth() - 20;
//        contentPanelHeigh = (previewWidth + 20) * cols;
        previewPanels = new HashMap<Integer, JPanel>();
        contentPanel.removeAll();

        for (int i = 0; i < rows * cols; i++) {
            int pointName = i + 1;
            CameraPoint cameraPoint = getListByName(cameraPoints, pointName);
            JPanel jPanel1 = new JPanel();
//            jPanel1.setPreferredSize(new Dimension(previewWidth, 0));
//            jPanel1.setName();
            jPanel1.add(new JLabel(camera.getIp() + ":扫描点" + pointName));
            VideoPanel videoPanel = new VideoPanel();
            videoPanel.setName(String.valueOf(pointName));

//            videoPanel.setPreferredSize(new Dimension(previewWidth, (int) ((float) previewWidth / MyAppConst.video_width * MyAppConst.video_height)));
            previewPanels.put(pointName, jPanel1);
            previewVideoPanels.put(pointName, videoPanel);
            jPanel1.add(videoPanel);
            videoPanel.addMouseListener(new previewPanelMouseAction(pointName));
            JButton execBtn = new JButton("设置");
            execBtn.setActionCommand(EXEC_EVENT_LOADVIDEO);
            execBtn.addActionListener(new previewVideoAction(pointName, jPanel1));
            previewExecBtns.put(pointName,execBtn);
            JButton beginCancelBtn = new JButton("取消");
            previewCancelBtns.put(pointName,beginCancelBtn);
            beginCancelBtn.setVisible(false);
            beginCancelBtn.addActionListener(new PreviewCancelAction(pointName));

            videoPanel.setLayout(new BorderLayout());
            if (cameraPoint == null) {
                layoutUnSet(videoPanel,execBtn);
            } else {
                layoutSet(videoPanel,execBtn,cameraPoint);
            }
            jPanel1.add(beginCancelBtn);
            jPanel1.add(execBtn);
            contentPanel.add(jPanel1);
//            contentPanel.setPreferredSize(new Dimension(contentPanelWidth, contentPanelHeigh));
//            contentPanel.repaint();
//
//            previewPanels.forEach((key, value) -> {
//                value.setPreferredSize(
//                        new Dimension(previewWidth, (int) ((float) previewWidth / MyAppConst.video_width * MyAppConst.video_height) + 60));
//            });
        }
//        contentPanel.revalidate();
        reLayoutPoint();

    }

    private void layoutUnSet(Panel videoPanel,JButton execBtn){
        execBtn.setText("设置");
        videoPanel.removeAll();
        videoPanel.add(new JLabel("扫描点未设置", JLabel.CENTER));
    }
    private void layoutSet(Panel videoPanel,JButton execBtn,CameraPoint cameraPoint){
        execBtn.setText("修改");
        videoPanel.removeAll();
        if (cameraPoint.getImage() != null) {
            byte[] imageData = Base64Utils.decodeFromString(cameraPoint.getImage());
            BufferedImage bufferedImage = null;
            videoPanel.setLayout(new BorderLayout());
            try {
                bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
                ImagePanel imagePanel = new ImagePanel();
                imagePanel.image = bufferedImage;
                videoPanel.add(imagePanel);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private class PreviewCancelAction implements ActionListener{
        private int pointName;
        public PreviewCancelAction(int pointName){
            this.pointName = pointName;
        }
        @Override
        public void actionPerformed(ActionEvent e) {

            NativeLong realHandle = netUtils.getRealHandle(camera.getIp());
            hCNetSDK.NET_DVR_StopRealPlay(realHandle);

            CameraPoint cameraPoint = getListByName(cameraPoints, pointName);
            JButton execBtn = previewExecBtns.get(pointName);
            Panel videoPanel = previewVideoPanels.get(pointName);
            if(cameraPoint == null){
                layoutUnSet(videoPanel,execBtn);
            }else{
                layoutSet(videoPanel,execBtn,cameraPoint);
            }
            execBtn.setActionCommand(EXEC_EVENT_LOADVIDEO);
            ( (JButton)e.getSource()).setVisible(false);
        }
    }

    private class previewPanelMouseAction extends MouseAdapter {
        int pointName;
        public previewPanelMouseAction(int pointName){
            this.pointName = pointName;
        }
        @Override
        public void mousePressed(java.awt.event.MouseEvent evt) {
            if (evt.getClickCount() == 2) { //双击放大
                if (showFull) {
                    previewPanels.forEach((key, value) -> {
                        if (!key.equals(this.pointName)) {
                            value.setVisible(true);
                        }
                    });
                    showFull = false;
                    showFullPanel = null;
                    showFullJPanel = null;
                } else {
                    previewPanels.forEach((key, value) -> {
                        if (!key.equals(this.pointName)) {
                            value.setVisible(false);
                        }
                    });
                    showFull = true;
                    showFullJPanel = previewPanels.get(this.pointName);
                    showFullPanel = previewVideoPanels.get(this.pointName);
                }
            }
            reLayoutPoint();
//            else {
//                String ip = evt.getComponent().getName();
//                Camera camera = cameraMapper.selectByIp(ip);
//                HCNetUtils.preview(thiz, previewVideoPanels.get(ip), camera);
//            }
        }
    }

    private class previewVideoAction implements ActionListener {
        private int pointName;
        private JPanel parentPanel;

        public previewVideoAction(int pointName, JPanel parentPanel) {
            this.pointName = pointName;
            this.parentPanel = parentPanel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            if (source.getActionCommand().equals(EXEC_EVENT_LOADVIDEO)) {
//                preview(pointName);
                netUtils.preview(thiz, previewVideoPanels.get(pointName), camera);

                source.setText("确定");
                source.setActionCommand(EXEC_EVENT_SAVE);
                previewCancelBtns.get(pointName).setVisible(true);
            } else if (source.getActionCommand().equals(EXEC_EVENT_SAVE)) {

                if (!hCNetSDK.NET_DVR_PTZPreset(netUtils.getRealHandle(camera.getIp()), HCNetSDK.SET_PRESET, Integer.valueOf(pointName)))
                {
                    JOptionPane.showMessageDialog(thiz, "设置预置点失败");
                    return;
                }
                CameraPoint cameraPoint = cameraPointMapper.selectByName(camera.getIp(), pointName);
                byte[] previewImage = netUtils.getPreviewImage(camera.getIp());

                BufferedImage bufferedImage = null;
                try {
                    bufferedImage = ImageIO.read(new ByteArrayInputStream(previewImage));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                Result[] results = QrcodeUtils.decodeQRcode(bufferedImage);
                bufferedImage = QrcodeUtils.printImg(bufferedImage, results);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    ImageIO.write(bufferedImage, "jpg", out);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                String imageBase64 = Base64Utils.encodeToString(out.toByteArray());
                if (cameraPoint == null) {
                    cameraPoint = new CameraPoint();
                    cameraPoint.setCameraIp(camera.getIp());
                    cameraPoint.setName(Integer.valueOf(pointName));
                    cameraPoint.setCreateTime(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    cameraPoint.setTestQrCount(Integer.valueOf(QrcodeCountText.getText()));
                    cameraPoint.setImage(imageBase64);
                    cameraPoint.setTestQrCount(results.length);
                    cameraPointMapper.insert(cameraPoint);
                } else {
                    cameraPoint.setImage(imageBase64);
                    cameraPoint.setTestQrCount(results.length);
                    cameraPointMapper.updateById(cameraPoint);
                }
                source.setText("修改");
                source.setActionCommand(EXEC_EVENT_LOADVIDEO);
                previewCancelBtns.get(pointName).setVisible(false);
                netUtils.previewClose(camera);

                layoutSet(previewVideoPanels.get(pointName),source,cameraPoint);
            }

//            JButton beginSetBtn = new JButton("取消");
////            beginSetBtn.addActionListener();
//            parentPanel.add(beginSetBtn);

        }
    }

    public class ImagePanel extends JPanel {

        private BufferedImage image = null;


        @Override
        protected void paintComponent(Graphics g) {
            //获得窗口的宽高
            int width = this.getWidth();
            int height = this.getHeight();
            this.setBackground(Color.PINK);
            //清除控件显示，在下次重新绘制的时候清除当前显示，不然会出现图片重叠现象
            g.clearRect(0, 0, width, height);
            if (image != null) {
                g.drawImage(image, 0, 0, width, height, this);
                g.setColor(Color.RED);
            }
        }

    }

    private void loadCrameraList() {
        List<Camera> cameras = cameraMapper.selectList(null);
        Camera[] cameraArr = new Camera[cameras.size()];
        cameras.toArray(cameraArr);
        list.setListData(cameraArr);

    }

    class SharedListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        camera =  list.getSelectedValue();
                        cameraPoints = cameraPointMapper.selectByIp(camera.getIp());
                        reLoadPoint();
                    }
                }).start();
            }

        }
    }


    /*************************************************
     函数:      "播放窗口"  双击响应函数
     函数描述:   双击全屏预览当前预览通道
     *************************************************/
    private void panelRealplayMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_panelRealplayMousePressed
    {//GEN-HEADEREND:event_panelRealplayMousePressed
        if (!bRealPlay) {
            return;
        }
        //鼠标单击事件为双击
        if (evt.getClickCount() == 2) {
            //新建JWindow 全屏预览
            final JWindow wnd = new JWindow();
            //获取屏幕尺寸
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            wnd.setSize(screenSize);
            wnd.setVisible(true);

            final W32API.HWND hwnd = new W32API.HWND(Native.getComponentPointer(wnd));
            m_strClientInfo.hPlayWnd = hwnd;
            final NativeLong lRealHandle = hCNetSDK.NET_DVR_RealPlay_V30(lUserID,
                    m_strClientInfo, null, null, true);

            //JWindow增加双击响应函数,双击时停止预览,退出全屏
            wnd.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        //停止预览
                        hCNetSDK.NET_DVR_StopRealPlay(lRealHandle);
                        wnd.dispose();
                    }
                }
            });

        }
    }


    /*************************************************
     函数:       左上 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class LeftUpAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.UP_LEFT, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.UP_LEFT, 1, 0);

        }
    }

    /*************************************************
     函数:       右下 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class RightDownAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.DOWN_RIGHT, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.DOWN_RIGHT, 1, 0);

        }
    }

    /*************************************************
     函数:       上 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class UpMouseAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.TILT_UP, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.TILT_UP, 1, 0);

        }
    }

    /*************************************************
     函数:       下 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class DownMouseAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.TILT_DOWN, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.TILT_DOWN, 1, 0);

        }
    }


    /*************************************************
     函数:       右上 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class RightUpMouseAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.UP_RIGHT, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.UP_RIGHT, 1, 0);
        }
    }

    /*************************************************
     函数:       左下 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/

    private class LeftDownMouseAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.DOWN_LEFT, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.DOWN_LEFT, 1, 0);
        }
    }


    /*************************************************
     函数:       左 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class LeftMouseAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.PAN_LEFT, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.PAN_LEFT, 1, 0);
        }
    }

    /*************************************************
     函数:       右 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class RightMouseAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.PAN_RIGHT, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.PAN_RIGHT, 1, 0);
        }
    }

    /*************************************************
     函数:       调焦 缩 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class ZoomInAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.ZOOM_IN, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.ZOOM_IN, 1, 0);

        }
    }

    /*************************************************
     函数:       调焦 伸 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class ZoomOutAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.ZOOM_OUT, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.ZOOM_OUT, 1, 0);

        }
    }

    /*************************************************
     函数:       聚焦 近 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class FocusNearAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.FOCUS_NEAR, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.FOCUS_NEAR, 1, 0);

        }
    }

    /*************************************************
     函数:       聚焦 远 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class FocusFarAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.FOCUS_FAR, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.FOCUS_FAR, 1, 0);

        }
    }

    /*************************************************
     函数:       光圈 开 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class IrisOpenAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.IRIS_OPEN, 0, 0);

        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.IRIS_OPEN, 1, 0);
        }
    }

    /*************************************************
     函数:       光圈 关 按钮的press和release响应函数
     函数描述:	云台控制函数
     *************************************************/
    private class IrisCloseAction extends MouseAdapter {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.IRIS_CLOSE, 0, 0);
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            netUtils.PTZControlAll(thiz, camera, HCNetSDK.IRIS_CLOSE, 1, 0);
        }
    }

    /*************************************************
     函数:       "自动"按钮  双击响应函数
     函数描述:	云台控制函数  云台开始/停止左右自动扫描
     *************************************************/
    private void jButtonAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAutoActionPerformed

    }


    private boolean setVideoEffect() {
        if (!hCNetSDK.NET_DVR_ClientSetVideoEffect(netUtils.getRealHandle(camera.getIp()), m_iBrightness, m_iContrast, m_iSaturation, m_iHue)) {
            JOptionPane.showMessageDialog(this, "设置预览视频显示参数失败");
            return false;
        } else {
            return true;
        }
    }

    private class SliderChangeListener implements ChangeListener, ActionListener {
        private boolean _default = false;

        public SliderChangeListener(boolean _default) {
            this._default = _default;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            m_iContrast = jSliderContrast.getValue();
            m_iBrightness = jSliderBright.getValue();
            m_iSaturation = jSliderSaturation.getValue();
            m_iHue = jSliderHue.getValue();
            setVideoEffect();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            m_iBrightness = 6;
            m_iContrast = 6;
            m_iSaturation = 6;
            m_iHue = 6;
            stateChanged(null);
        }
    }


    public class TestScanQrcodeAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            testScanForm.setBounds(200, 200, 800, 600);
            testScanForm.setLocationRelativeTo(null);
            testScanForm.setVisible(true);

            BufferedImage bufferedImage = netUtils.getBufferedImage(camera);
            Result[] results = QrcodeUtils.decodeQRcode(bufferedImage);
            bufferedImage = QrcodeUtils.printImg(bufferedImage, results);
            testScanForm.showImage(bufferedImage,results);
            QrcodeCountText.setText(String.valueOf(results.length));
        }
    }

}
