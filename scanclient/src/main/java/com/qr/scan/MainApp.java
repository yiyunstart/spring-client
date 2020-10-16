package com.qr.scan;

import com.google.zxing.Result;
import com.qr.scan.entity.Camera;
import com.qr.scan.entity.CameraPoint;
import com.qr.scan.form.CameraConfForm;
import com.qr.scan.form.ImgViewForm;
import com.qr.scan.form.ScanPointForm;
import com.qr.scan.form.SysParamConfForm;
import com.qr.scan.mapper.CameraMapper;
import com.qr.scan.mapper.CameraPointMapper;
import com.qr.scan.utils.HCNetSDK;
import com.qr.scan.utils.HCNetUtils;
import com.qr.scan.utils.QrcodeUtils;
import com.qr.scan.utils.SwingUtils;
import com.qr.scan.vo.CameraScanRet;
import com.qr.scan.vo.ScanRet;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

@SpringBootApplication
@Log
public class MainApp extends JFrame {


    @Autowired
    private CameraMapper cameraMapper;

    @Autowired
    CameraConfForm cameraConfForm;
    @Autowired
    ScanPointForm scanPointForm;
    @Autowired
    SysParamConfForm sysParamConfForm;
    @Autowired
    ImgViewForm imgViewForm;

    @Autowired
    SystemInit systemInit;


    @Autowired
    MyAppConst myAppConst;
    @Autowired
    HCNetUtils netUtils;

    private JFrame root = null;

    private JTree qrcodeRetjTree = null;
    private DefaultTreeModel myDefaultTreeModel = null;
    private DefaultMutableTreeNode root_node = null;


    private JScrollPane contentScrollPane = null;
    private static JPanel contentJPanel = new JPanel();
    private static Map<String, JPanel> previewPanels = new HashMap<String, JPanel>();
    private static Map<String, Panel> previewVideoPanels = new HashMap<String, Panel>();
    JPanel leftJPanel = new JPanel();

    private List<Camera> camerasAll = null; //所有的摄像头信息
    public List<Camera> camerasSelected = null; //当前选中的摄像头信息
    public Map<String, CameraScanRet> cameraScanRets = null;//扫描结果缓存

    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;


    public MainApp() {
        root = this;
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);//防止被播放窗口(AWT组件)覆盖
        initComponents();
    }

    private void initComponents() {

        this.setBackground(Color.GRAY);

        this.setBounds(300, 200, 800, 600);
        this.setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initUI() {
        systemInit.init(false);
        BorderLayout borderLayout = new BorderLayout();
        this.setLayout(borderLayout);    //为Frame窗口设置布局为BorderLayout

        layoutTopMenuBar();
        layoutTopBtn();
        layoutLeftPanel();

        //设置中间内容面板
        contentJPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        contentJPanel.setBackground(Color.gray);    //设置背景色
//        contentPanel.setPreferredSize(new Dimension(500, 400));


        contentScrollPane = new JScrollPane(contentJPanel);

        this.add(contentScrollPane, BorderLayout.CENTER);


        layoutRightPanel();

        //修改关闭按钮事件
        this.addWindowListener(new MyWindowEvent());
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    //渲染顶菜单
    private void layoutTopMenuBar() {

        //顶部菜单
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu configMenu = new JMenu("设置");
        configMenu.setIcon(null);
        menuBar.add(configMenu);

        JMenuItem cameraConfMenuItem = new JMenuItem("摄像头配置");
        cameraConfMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cameraConfForm.setVisible(true);
            }
        });
        configMenu.add(cameraConfMenuItem);

        JMenuItem scanConfMenuItem = new JMenuItem("扫描点配置");
        configMenu.add(scanConfMenuItem);
        scanConfMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scanPointForm.setVisible(true);
            }
        });
        JMenuItem sysParamConfMenuItem = new JMenuItem("参数配置");
        configMenu.add(sysParamConfMenuItem);
        sysParamConfMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sysParamConfForm.setVisible(true);
            }
        });
    }

    private void layoutTopBtn() {
        JPanel topPanel = new JPanel();
        JButton scanBtn = new JButton("开始扫描");
        scanBtn.addActionListener(new ScanQrcodeAction());
        JButton showSacnRetBtn = new JButton("查看扫描图片");
        showSacnRetBtn.addActionListener(new ShowScanRetAction());
        topPanel.add(scanBtn);
        topPanel.add(showSacnRetBtn);
        this.add(topPanel, BorderLayout.NORTH);
    }

    //渲染左侧摄像探头列表
    private void layoutLeftPanel() {


        leftJPanel.setBorder(BorderFactory.createTitledBorder("摄像头"));
        leftJPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        leftJPanel.setPreferredSize(new Dimension(200, 0));


        this.add(leftJPanel, BorderLayout.WEST);

    }

    //设置右侧面板
    private void layoutRightPanel() {

        root_node = new DefaultMutableTreeNode("扫描结果");

        qrcodeRetjTree = new JTree();
        myDefaultTreeModel = new DefaultTreeModel(root_node);
        qrcodeRetjTree.setModel(myDefaultTreeModel);
        SwingUtils.expandAll(qrcodeRetjTree, new TreePath(root_node), true);
        JScrollPane scrollRightPane = new JScrollPane(qrcodeRetjTree);
        scrollRightPane.setBorder(BorderFactory.createTitledBorder("扫描结果"));
        scrollRightPane.setPreferredSize(new Dimension(200, 0));
        this.add(scrollRightPane, BorderLayout.EAST);
    }

    public void _windowClosing(WindowEvent e) {
        int option = JOptionPane.showConfirmDialog(this, "确定退出系统?", "提示",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            if (e.getWindow() == this) {
                this.dispose();
                System.exit(0);
            } else {
                return;
            }
        } else if (option == JOptionPane.NO_OPTION) {
            if (e.getWindow() == this) {
                return;
            }
        }
    }

    public static void main(String[] args) {

        ApplicationContext ctx = new SpringApplicationBuilder(MainApp.class)
                .headless(false).run(args);
        EventQueue.invokeLater(() -> {
            SpringContextUtil.setApplicationContext(ctx);
            try {
                javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }
            MainApp ex = ctx.getBean(MainApp.class);
            ex.setVisible(true);
            //加载成功再绘制页面
            ex.initUI();
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                JOptionPane.showMessageDialog(null, "初始化失败");
            }

        });
    }


    public void loadCrameraList() {
        camerasAll = cameraMapper.selectList(null);
        leftJPanel.removeAll();
        for (Camera camera : camerasAll) {
            JLabel label = new JLabel(camera.getIp());
            label.setSize(new Dimension(140, 30));
            JButton button = null;
            if (previewVideoPanels.get(camera.getIp()) == null) {
                button = new JButton("打开");
                button.setActionCommand("open");
            } else {
                button = new JButton("关闭");
                button.setActionCommand("close");
            }
            button.addActionListener(new CameraExecAdction(camera));
            button.setPreferredSize(new Dimension(60, 30));
            leftJPanel.add(label);
            leftJPanel.add(button);
        }
        leftJPanel.repaint();
        leftJPanel.revalidate();
    }


    private class MyWindowEvent implements WindowListener {
        @Override
        public void windowOpened(java.awt.event.WindowEvent e) {
            loadCrameraList();
        }

        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
            _windowClosing(e);
        }

        @Override
        public void windowClosed(java.awt.event.WindowEvent e) {

        }

        @Override
        public void windowIconified(java.awt.event.WindowEvent e) {

        }

        @Override
        public void windowDeiconified(java.awt.event.WindowEvent e) {

        }

        @Override
        public void windowActivated(java.awt.event.WindowEvent e) {

        }

        @Override
        public void windowDeactivated(java.awt.event.WindowEvent e) {

        }
    }

    private class CameraExecAdction implements ActionListener {
        private Camera camera;

        public CameraExecAdction(Camera camera) {
            this.camera = camera;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();

            if (e.getActionCommand().equals("close")) {
                source.setText("打开");
                source.setActionCommand("open");
                netUtils.previewClose(camera);
                contentJPanel.remove(previewPanels.get(camera.getIp()));
                previewPanels.remove(this.camera.getIp());
                contentJPanel.repaint();
                contentJPanel.revalidate();
                camerasSelected.remove(camera);

                DefaultMutableTreeNode ipNode = find(root_node, camera.getIp());
                if (ipNode != null) {
                    root_node.remove(ipNode);
                    myDefaultTreeModel.reload();
                }
                layoutContenPanel();
                return;
            }


            if (previewPanels.containsKey(this.camera.getIp())) {
                return;
            }

            if (camerasSelected == null) {
                camerasSelected = new ArrayList<>();

            }
            camerasSelected.add(camera);
            imgViewForm.layoutCamera();


            Panel jPanel1 = new Panel();
            jPanel1.setVisible(true);
            JPanel jpanel = new JPanel();
            jpanel.add(new JLabel(camera.getIp()));
            jpanel.add(jPanel1);
            jpanel.setName(camera.getIp());

            previewPanels.put(jpanel.getName(), jpanel);
            previewVideoPanels.put(camera.getIp(), jPanel1);
            contentJPanel.add(jpanel);
            DefaultMutableTreeNode ipNode = find(root_node, camera.getIp());
            if (ipNode == null) {
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(camera.getIp());
                root_node.add(treeNode);
                myDefaultTreeModel.reload();
            }

            source.setText("关闭");
            source.setActionCommand("close");
            layoutContenPanel();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    netUtils.preview(root, previewVideoPanels.get(camera.getIp()), camera);
                }
            }).start();


        }
    }

    private void layoutContenPanel() {
        int previewWidth = camerasSelected.size() > 1 ? contentScrollPane.getWidth() / 2 - 15 : contentScrollPane.getWidth() - 10;
        int previewHeight = (int) (myAppConst.video_height * ((float) previewWidth / myAppConst.video_width));
        contentJPanel.setPreferredSize(new Dimension(contentScrollPane.getWidth(), camerasSelected.size() / 2 * (previewHeight + 10)));
        previewVideoPanels.forEach((key, value) -> {
            value.setPreferredSize(new Dimension(previewWidth, previewHeight));
        });
        previewPanels.forEach((key, value) -> {
            value.setPreferredSize(new Dimension(previewWidth, previewHeight + 30));
        });
        contentJPanel.revalidate();
    }


    @Autowired
    private CameraPointMapper cameraPointMapper;

    private class ScanQrcodeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cameraScanRets = new HashMap<>();
                    for (Camera camera : camerasSelected) {
                        scanQrcodeByCamera(camera);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {


                            }
                        }).start();
                    }

                }
            }).start();
        }
    }

    void  scanQrcodeByCamera(Camera camera){
        DefaultMutableTreeNode cameraNode = find(root_node, camera.getIp());
        cameraNode.removeAllChildren();
        myDefaultTreeModel.reload();
        List<CameraPoint> cameraPoints = cameraPointMapper.selectByIp(camera.getIp());
        if (cameraPoints == null || cameraPoints.size() == 0) {
            return;
        }
        List<ScanRet> scanRetList = new ArrayList<>();
        scanQrcodeByPoint(camera, cameraPoints, 0, scanRetList);
        System.out.println(scanRetList.size());
        CameraScanRet cameraScanRet = new CameraScanRet();
        cameraScanRet.setCamera(camera);
        cameraScanRet.setScanRetList(scanRetList);
        cameraScanRets.put(camera.getIp(), cameraScanRet);
    }


    private class ShowScanRetAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            imgViewForm.setVisible(true);
        }
    }

    public List<ScanRet> scanQrcodeByPoint(Camera camera, List<CameraPoint> cameraPoints, int idx, List<ScanRet> scanRetList) {

        //跳转到预置点
        boolean ret = netUtils.gotoPoint(camera, cameraPoints.get(idx).getName());
        try {
            Thread.sleep(myAppConst.jumpPointTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //开始扫描
        Result[] results = null;
        BufferedImage bufferedImage = null;
        int num = 0;
        do {
            bufferedImage = netUtils.getBufferedImage(camera);
            results = QrcodeUtils.decodeQRcode(bufferedImage);
            ++num;
            System.out.println("第" + num + "次尝试");
        } while (results == null && num < 5);

        if (results != null) {

            if (cameraPoints.get(idx).getTestQrCount() > results.length) {
                System.err.println("扫到的结果与预置结果不一样");
            }
        } else {
            System.err.println("没有扫到二维码");
        }

        //处理结果
        CameraPoint cameraPoint = cameraPoints.get(idx);
        DefaultMutableTreeNode cameraNode = find(root_node, camera.getIp());
        DefaultMutableTreeNode pointNode = find(cameraNode, "扫描点：" + cameraPoint.getName());
        ;
        if (pointNode == null) {

            pointNode = new DefaultMutableTreeNode("扫描点：" + cameraPoint.getName());
        }
        if (results != null) {

            for (Result result : results) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode((pointNode.getChildCount() + 1) + ":" + result);
                pointNode.add(newNode);
            }

            bufferedImage = QrcodeUtils.printImg(bufferedImage, results);
        } else {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("无");
            pointNode.add(newNode);
        }
        cameraNode.add(pointNode);
        myDefaultTreeModel.reload();
        SwingUtils.expandAll(qrcodeRetjTree, new TreePath(root_node), true);

        ScanRet scanRet = new ScanRet();
        scanRet.setCameraPoint(cameraPoint);
        scanRet.setImage(bufferedImage);
        scanRet.setQrCodes(results);
        scanRetList.add(scanRet);
        if (imgViewForm.isVisible()) {
            imgViewForm.showImage(cameraPoint, bufferedImage, results);
        }

        if (idx + 1 < cameraPoints.size()) {
            //递归调用其他点
            scanQrcodeByPoint(camera, cameraPoints, idx + 1, scanRetList);

        } else {
            //归位,回到最初的位置
            netUtils.gotoPoint(camera, cameraPoints.get(0).getName());
        }
        return scanRetList;
    }
//
//    private  Map scanQrcode(Camera camera,int idx){
//        BufferedImage bufferedImage = netUtils.getBufferedImage(camera);
//        Result[] results = QrcodeUtils.decodeQRcode(bufferedImage);
//        if(results==null && idx <5){
//            results =  scanQrcode(camera,idx+1);
//        }
//        Map ret = new HashMap();
//        ret.put("qrcode",results);
//        ret.put("img",bufferedImage);
//        return ret;
//    }

    private DefaultMutableTreeNode find(DefaultMutableTreeNode root, String s) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().equalsIgnoreCase(s)) {
                return node;
            }
        }
        return null;
    }
}
