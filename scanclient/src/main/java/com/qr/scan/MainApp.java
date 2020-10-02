package com.qr.scan;

import com.google.zxing.Result;
import com.qr.scan.entity.Camera;
import com.qr.scan.mapper.CameraMapper;
import com.qr.scan.utils.HCNetSDK;
import com.qr.scan.utils.PlayCtrl;
import com.qr.scan.utils.QrcodeUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import lombok.extern.java.Log;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

@SpringBootApplication
@Log
public class MainApp extends JFrame {



    @Autowired
    private CameraMapper cameraMapper;

    JList list=new JList();
    @Autowired
    CameraConfForm cameraConfForm ;
    @Autowired
    ScanPointForm scanPointForm;
    @Autowired
    SysParamConfForm sysParamConfForm;
    @Autowired
    JFrameImgviewControl jFrameImgviewControl;

    @Autowired
    SystemInit systemInit;

    private static JPanel contentPanel =new JPanel();    //创建面板
    private static Map<String,Panel> previewPanels = new HashMap<String,Panel>();    //创建面板

    private JFrame root = null;


    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    static PlayCtrl playControl = PlayCtrl.INSTANCE;

    public static NativeLong g_lVoiceHandle;//全局的语音对讲句柄

    HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;//设备信息
    HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg;//IP参数
    HCNetSDK.NET_DVR_CLIENTINFO m_strClientInfo;//用户参数

    boolean bRealPlay;//是否在预览.
    String m_sDeviceIP;//已登录设备的IP地址

    NativeLong lUserID;//用户句柄
    NativeLong lPreviewHandle;//预览句柄
    NativeLongByReference m_lPort;//回调预览时播放库端口指针
//    FRealDataCallBack fRealDataCallBack;//预览回调函数实现

//    private int video_wh_ratio =video_width / video_width;

    JTree tree = null;
    DefaultTreeModel myDefaultTreeModel = null;
    DefaultMutableTreeNode root_node = null;


    public MainApp() {
        root = this;
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);//防止被播放窗口(AWT组件)覆盖
        initComponents();

        lUserID = new NativeLong(-1);
        lPreviewHandle = new NativeLong(-1);
        g_lVoiceHandle = new NativeLong(-1);
        m_lPort = new NativeLongByReference(new NativeLong(-1));
//        m_iTreeNodeNum = 0;
//        fRealDataCallBack = new FRealDataCallBack();
    }

    private void initComponents() {

        this.setBackground(Color.GRAY);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

//        this.setBounds(300,200,800,600);
    }
    private void initUI() {
        systemInit.init(false);
        BorderLayout borderLayout = new BorderLayout();
        this.setLayout(borderLayout);    //为Frame窗口设置布局为BorderLayout
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu configMenu = new JMenu("\u914D\u7F6E");
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
        Object[] listData= {111,222,333,444};

        list.setListData(listData);
        MyJcheckBox cell = new MyJcheckBox();
        list.setCellRenderer(cell);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel() {
            boolean gestureStarted = false;
            @Override
            public void setSelectionInterval(int index0, int index1) {
                System.out.println(index0+":"+index1);
                if(!gestureStarted) {
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
        };
        defaultListSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
        list.setSelectionModel(defaultListSelectionModel);
//        list.addListSelectionListener(new SharedListSelectionHandler());
        JButton all =new JButton("全选");




//        JButton btn1=new JButton("1");    //创建按钮
//        JButton btn2=new JButton("2");
//        JButton btn3=new JButton("3");
//        JButton btn4=new JButton("4");
//        JButton btn5=new JButton("5");
//        JButton btn6=new JButton("6");
//        JButton btn7=new JButton("7");
//        JButton btn8=new JButton("8");
//        JButton btn9=new JButton("9");
//        jPanel.add(btn1);    //面板中添加按钮
//        jPanel.add(btn2);
//        jPanel.add(btn3);
//        jPanel.add(btn4);
//        jPanel.add(btn5);
//        jPanel.add(btn6);
//        jPanel.add(btn7);
//        jPanel.add(btn8);
//        jPanel.add(btn9);
//        JPanel jPanel1 = new JPanel();
//        jPanel1.setPreferredSize(new Dimension(100, 100));
//        jPanel1.setName(String.valueOf(1));
//        previewPanels.put(jPanel1.getName(),jPanel1);
//        contentPanel.add(jPanel1);
        //向JPanel添加FlowLayout布局管理器，将组件间的横向和纵向间隙都设置为20像素
        contentPanel.setLayout(new FlowLayout(FlowLayout.LEADING,20,20));
        contentPanel.setBackground(Color.gray);    //设置背景色
        contentPanel.setPreferredSize(new Dimension(500,400));


        //创建工具栏
        JToolBar toolBar=new JToolBar();
        toolBar.add(Box.createHorizontalGlue());
        JButton button=null;
        button=ToolBarUtils.makeNavigationButton("new1","NEW","新建一个文件","新建");
        toolBar.add(button);
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(20,20));
        sep.setMaximumSize(new Dimension(20,20));
        sep.setMinimumSize(new Dimension(20,20));
        toolBar.add(sep);
        button=ToolBarUtils.makeNavigationButton("open1","OPEN","打开一个文件","打开");
        toolBar.add(button);
        toolBar.add(sep);

        button=ToolBarUtils.makeNavigationButton("save1","SAVE","保存当前文件","保存");
        toolBar.add(button);



//        JButton button1=new JButton ("上");
        JButton button2=new JButton("左");
        JButton button3=new JButton("中");
        JButton button4=new JButton("右");
        JButton button5=new JButton("下");
        Dimension dimension = new Dimension();
        dimension.width = 200;
        button2.setPreferredSize(dimension);


        JPanel topPanel = new JPanel();
        JButton scanBtn = new JButton("开始扫描");
        scanBtn.addActionListener(new ScanQrcodeAction());
        JButton showSacnRetBtn = new JButton("查看扫描图片");
        showSacnRetBtn.addActionListener(new ShowScanRetAction());
        topPanel.add(scanBtn);
        topPanel.add(showSacnRetBtn);
        this.add(topPanel,BorderLayout.NORTH);
        JScrollPane scrollLeftPane = new JScrollPane(list);
        scrollLeftPane.setPreferredSize(new Dimension(200,0));
        scrollLeftPane.setBorder(BorderFactory.createTitledBorder("摄像头"));

        this.add(scrollLeftPane,BorderLayout.WEST);
        JScrollPane scrollPane = new JScrollPane(contentPanel);

        this.add(scrollPane,BorderLayout.CENTER);
//
//        DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("软件部");
//        node1.add(new DefaultMutableTreeNode("小花"));
//        node1.add(new DefaultMutableTreeNode("小虎"));
//        node1.add(new DefaultMutableTreeNode("小龙"));
//
//        DefaultMutableTreeNode node2 = new DefaultMutableTreeNode("销售部");
//        node2.add(new DefaultMutableTreeNode("小叶"));
//        node2.add(new DefaultMutableTreeNode("小雯"));
//        node2.add(new DefaultMutableTreeNode("小夏"));

        root_node = new DefaultMutableTreeNode("扫描结果");

//        top.add(new DefaultMutableTreeNode("s"));
//        top.add(node1);
//        top.add(node2);
        tree = new JTree();
        myDefaultTreeModel = new DefaultTreeModel(root_node);
        tree.setModel(myDefaultTreeModel);
        expandAll(tree, new TreePath(root_node), true);
        JScrollPane scrollRightPane = new JScrollPane(tree);
        scrollRightPane.setBorder(BorderFactory.createTitledBorder("扫描结果"));
        scrollRightPane.setPreferredSize(new Dimension(200,0));
        this.add(scrollRightPane,BorderLayout.EAST);
        this.add(button5,BorderLayout.SOUTH);
//        this.setBounds(300,200,800,600);
        this.setVisible(true);


        this.addWindowListener(new MyWindowEvent() );
        //按关闭按钮，啥事也不做
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    // 展开树的所有节点的方法
    private static void expandAll(JTree tree, TreePath parent, boolean expand)
    {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0)
        {
            for (Enumeration e = node.children(); e.hasMoreElements();)
            {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        if (expand)
        {
            tree.expandPath(parent);
        } else
        {
            tree.collapsePath(parent);
        }
    }



    public void _windowClosing(WindowEvent e) {
        int option = JOptionPane.showConfirmDialog(this, "确定退出系统?", "提示",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
        {
            if (e.getWindow() == this) {
                this.dispose();
                System.exit(0);
            } else {
                return;
            }
        }
        else if(option == JOptionPane.NO_OPTION){
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
//            ex.showUser();
            //加载成功再绘制页面
            ex.initUI();
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                JOptionPane.showMessageDialog(null, "初始化失败");
            }

        });
    }


    public void loadCrameraList(){
        List<Camera> cameras = cameraMapper.selectList(null);
        Object[] vData = new Object[cameras.size()];

        for (int i = 0; i < cameras.size(); i++) {
            Camera camera = cameras.get(i);
            vData[i] = camera.getIp();
        }
        list.setListData(vData);

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


    class SharedListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {

            boolean isAdjusting = e.getValueIsAdjusting();
//            if(isAdjusting){
//                JList lsm = (JList)e.getSource();
                //System.out.printf("LeadSelectionIndex is %s%n",lsm.getLeadSelectionIndex());
                int[] selectedIndices = list.getSelectedIndices();
                System.out.println(ToStringBuilder.reflectionToString(selectedIndices));
                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
            List ipList = list.getSelectedValuesList();
            System.out.println(ToStringBuilder.reflectionToString(ipList));
            //先判断移除
            List removeList =new ArrayList();
            for(Object ip : previewPanels.keySet()){
                if(!ipList.contains(ip)){
                    contentPanel.remove(previewPanels.get(ip));
                    removeList.add(ip);
                }
            }
            for (Object removeIP:removeList){
                previewPanels.remove(removeIP);
            }

            contentPanel.repaint();
            for (Object ip : ipList){
                if(previewPanels.containsKey(ip)){
                    continue;
                }
                Panel jPanel1 = new Panel();
                jPanel1.setPreferredSize(new Dimension(200, (int)(MyAppConst.video_height*((float)200/MyAppConst.video_width))));
                jPanel1.setVisible(true);
                jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mousePressed(java.awt.event.MouseEvent evt) {
                        panelRealplayMousePressed(evt);
                    }
                });
                JPanel jpanel = new JPanel();
                jpanel.add(new JLabel(ip.toString()));
                jpanel.add(jPanel1);
                jpanel.setName(String.valueOf(ip));
                jpanel.setPreferredSize(new Dimension(200, 200));


                previewPanels.put(jpanel.getName(),jPanel1);

                contentPanel.add(jpanel);
                register(ip);
                preview(ip);
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(ip);
                root_node.add(treeNode);
                myDefaultTreeModel.reload();

            }

            contentPanel.revalidate();

//            }

        }
    }

    private void register(Object ip) {
        Camera camera = cameraMapper.selectByIp(ip);
//        m_sDeviceIP = jTextFieldIPAddress.getText();//设备ip地址
        m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int iPort = Integer.parseInt(camera.getPort());
        lUserID = hCNetSDK.NET_DVR_Login_V30(camera.getIp(),
                (short) iPort, camera.getUsername(), camera.getPasswd(), m_strDeviceInfo);

        long userID = lUserID.longValue();
        if (userID == -1) {
//            m_sDeviceIP = "";//登录未成功,IP置为空
            JOptionPane.showMessageDialog(MainApp.this, "注册失败");
        } else {
            log.info("注册成功");
//            CreateDeviceTree();
        }
    }

    //预览
    private void preview(Object ip) {
        //获取窗口句柄
        W32API.HWND hwnd = new W32API.HWND(Native.getComponentPointer(previewPanels.get(ip)));

        //获取通道号
        int iChannelNum = 1;//通道号
        if (iChannelNum == -1) {
            JOptionPane.showMessageDialog(this, "请选择要预览的通道");
            return;
        }

        m_strClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();
        m_strClientInfo.lChannel = new NativeLong(iChannelNum);

        //在此判断是否回调预览,0,不回调 1 回调
        m_strClientInfo.hPlayWnd = hwnd;
        lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V30(lUserID,
                m_strClientInfo, null, null, true);

        long previewSucValue = lPreviewHandle.longValue();

        //预览失败时:
        if (previewSucValue == -1) {
            JOptionPane.showMessageDialog(this, "预览失败");
            return;
        }

        //预览成功的操作
//        jButtonRealPlay.setText("停止预览");
        bRealPlay = true;
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

    private class ScanQrcodeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    gotoAndGetImage(1);
                }
            }).start();
        }
    }


    private class ShowScanRetAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            jFrameImgviewControl.setVisible(true);
        }
    }

    public void gotoAndGetImage(int point)  {

        boolean ret = hCNetSDK.NET_DVR_PTZPreset(lPreviewHandle
                , hCNetSDK.GOTO_PRESET, point);
        System.out.println(ret);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getImageToView(point);
    }
    public void gotoPoint(int point) {

        boolean ret = hCNetSDK.NET_DVR_PTZPreset(lPreviewHandle
                , hCNetSDK.GOTO_PRESET, point);
    }

    public void getImageToView(final int view) {
        HCNetSDK.NET_DVR_JPEGPARA jpeginfo = new HCNetSDK.NET_DVR_JPEGPARA();
        jpeginfo.wPicQuality = 2;
        jpeginfo.wPicSize = 0;
        int dwPicSize = MyAppConst.video_width * MyAppConst.video_height;
        IntByReference lpSizeReturned = new IntByReference();
        lpSizeReturned.setValue(0);
        NativeLong DVRChannel = new NativeLong();
//        DVRChannel.setValue(getChannelNumber());
        DVRChannel.setValue(1);
        Pointer p = new Memory(1600 * 1200);
        hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(lUserID, DVRChannel, jpeginfo, p, dwPicSize, lpSizeReturned);
        final byte[] imageData = p.getByteArray(0, lpSizeReturned.getValue());

        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Result[] results = QrcodeUtils.decodeQRcode(bufferedImage);


        if (results != null) {
           for(Result result : results){
               DefaultMutableTreeNode node = find(root_node, "192.168.1.64");
               DefaultMutableTreeNode newNode = new DefaultMutableTreeNode((node.getChildCount()+1)+":"+result);
               node.add(newNode);
               myDefaultTreeModel.reload();
               expandAll(tree, new TreePath(root_node), true);
           }
            bufferedImage = QrcodeUtils.printImg(bufferedImage, results);
        }
        if(jFrameImgviewControl.isVisible()) {
            jFrameImgviewControl.showImage(view, bufferedImage, results);
        }
        if (view < 6) {

                gotoAndGetImage(view + 1);

        }else{
            //归位
            gotoPoint(1);

        }


    }

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
