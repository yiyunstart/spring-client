package com.qr.scan;

import com.qr.scan.entity.Camera;
import com.qr.scan.mapper.CameraMapper;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.*;
import java.util.List;

@SpringBootApplication
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
    SystemInit systemInit;

    private static JPanel contentPanel =new JPanel();    //创建面板
    private static Map<String,JPanel> previewPanels = new HashMap<String,JPanel>();    //创建面板

    private JFrame root = null;

    public MainApp() {
        root = this;
        initComponents();
    }

    private void initComponents() {

        this.setBackground(Color.GRAY);
        this.setBounds(300,200,800,600);
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
        this.add(toolBar,BorderLayout.NORTH);
        this.add(list,BorderLayout.WEST);
        JScrollPane scrollPane = new JScrollPane(contentPanel);


        this.add(scrollPane,BorderLayout.CENTER);

        DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("软件部");
        node1.add(new DefaultMutableTreeNode("小花"));
        node1.add(new DefaultMutableTreeNode("小虎"));
        node1.add(new DefaultMutableTreeNode("小龙"));

        DefaultMutableTreeNode node2 = new DefaultMutableTreeNode("销售部");
        node2.add(new DefaultMutableTreeNode("小叶"));
        node2.add(new DefaultMutableTreeNode("小雯"));
        node2.add(new DefaultMutableTreeNode("小夏"));

        DefaultMutableTreeNode top = new DefaultMutableTreeNode("职员管理");

        top.add(new DefaultMutableTreeNode("总经理"));
        top.add(node1);
        top.add(node2);
        final JTree tree = new JTree(top);
        expandAll(tree, new TreePath(top), true);
        tree.setPreferredSize(new Dimension(300,0));
        this.add(tree,BorderLayout.EAST);
        this.add(button5,BorderLayout.SOUTH);
        this.setBounds(300,200,800,600);
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

            MainApp ex = ctx.getBean(MainApp.class);
            ex.setVisible(true);
//            ex.showUser();
            //加载成功再绘制页面
            ex.initUI();

        });
    }

    private void loadCrameraList(){
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
                JPanel jPanel1 = new JPanel();
                jPanel1.setPreferredSize(new Dimension(200, 200));
                jPanel1.setName(String.valueOf(ip));
                jPanel1.setVisible(true);
                previewPanels.put(jPanel1.getName(),jPanel1);
                contentPanel.add(jPanel1);
            }

            contentPanel.revalidate();

//            }

        }
    }

}
