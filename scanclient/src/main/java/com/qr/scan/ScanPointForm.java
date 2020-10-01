package com.qr.scan;

import com.qr.scan.entity.Camera;
import com.qr.scan.mapper.CameraMapper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 摄像头配置
 */
@Component
public class ScanPointForm extends JFrame {
    private static JPanel contentPanel =new JPanel();    //创建面板
    private static Map<String,JPanel>  previewPanels = new HashMap<String,JPanel>();    //创建面板

    private static JScrollPane scrollPane = new JScrollPane(contentPanel);
    private JList list=new JList();

    @Autowired
    private CameraMapper cameraMapper;

    private int cols =3 ,rows = 3;
    private Object ip= null;



    public ScanPointForm(){
        initComponents();
        this.setBounds(300, 200, 800, 600);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                // some calculation
                System.out.println("contentPanel:"+contentPanel.getWidth()+":"+contentPanel.getHeight());
                contentPanel.setPreferredSize(new Dimension(scrollPane.getWidth()-20,(scrollPane.getWidth()-20-20)/3*rows));
                if(showFull) {

                    showFullPanel.setPreferredSize(
                            new Dimension(scrollPane.getWidth()-20, scrollPane.getWidth()-20));
                }
                else{
                    previewPanels.forEach((key, value) -> {
//                        if (!key.equals(showFullPanel.getName())) {
//                            value.setVisible(true);
//                        }
                        value.setPreferredSize(
                                new Dimension((scrollPane.getWidth()-20-20)/3, (scrollPane.getWidth()-20-20)/3));
                    });

                }
                System.out.println("scrollPane:"+scrollPane.getWidth()+":"+scrollPane.getHeight());
            }
        });
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        loadCrameraList();
    }

    private void initComponents() {

//
//        JButton btn1=new JButton("1");    //创建按钮
//        JButton btn2=new JButton("2");
//        JButton btn3=new JButton("3");
//        JButton btn4=new JButton("4");
//        JButton btn5=new JButton("5");
//        JButton btn6=new JButton("6");
//        JButton btn7=new JButton("7");
//        JButton btn8=new JButton("8");
//        JButton btn9=new JButton("9");
//
//        JPanel jPanel1 = new JPanel();
////        jPanel1.setPreferredSize(new Dimension(300,300));
//        jPanel1.add(btn1);
//
//        contentPanel.add(jPanel1);    //面板中添加按钮
//        contentPanel.add(btn2);
//        contentPanel.add(btn3);
//        contentPanel.add(btn4);
//        contentPanel.add(btn5);
//        contentPanel.add(btn6);
//        contentPanel.add(btn7);
//        contentPanel.add(btn8);
//        contentPanel.add(btn9);


        //向JPanel添加FlowLayout布局管理器，将组件间的横向和纵向间隙都设置为20像素
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 5, 5);
        contentPanel.setLayout(flowLayout);
        contentPanel.setBackground(Color.gray);    //设置背景色


        GridBagLayout gbaglayout=new GridBagLayout();    //创建GridBagLayout布局管理器
        GridBagConstraints constraints=new GridBagConstraints();
        super.getContentPane().setLayout(gbaglayout);    //使用GridBagLayout布局管理器
        constraints.fill=GridBagConstraints.BOTH;    //组件填充显示区域
        constraints.weightx=0.0;    //恢复默认值
        constraints.gridwidth = GridBagConstraints.REMAINDER;    //结束行

        JPanel panel = new JPanel();
//        GroupLayout groupLayout = new GroupLayout(panel);
        panel.setLayout(new FlowLayout(FlowLayout.LEADING,5,5));
        JLabel ipLabel=new JLabel("网格大小");
        JTextField rowsText=new JTextField("3");

        JLabel portLabel=new JLabel("x");
        JTextField colsText=new JTextField("3");
//
//        JLabel userLabel=new JLabel("用户名");
//        JTextField userText=new JTextField("admin");
//
//        JLabel passwdLabel=new JLabel("密码");
//        JPasswordField passwdText=new JPasswordField("12345678a");

        JButton addButton = new JButton("设置");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cols = Integer.valueOf(rowsText.getText());
                rows = Integer.valueOf(colsText.getText());
                reLoadPoint();
            }
        });

        panel.add(ipLabel);
        panel.add(rowsText);
        panel.add(portLabel);
        panel.add(colsText);
//        panel.add(userLabel);
//        panel.add(userText);
//        panel.add(passwdLabel);
//        panel.add(passwdText);
        panel.add(addButton);

        constraints.gridwidth=GridBagConstraints.REMAINDER;
        gbaglayout.setConstraints(panel, constraints);

        super.getContentPane().add(panel);



        Object[] listData= {111,222,333,444};

        list.setListData(listData);
        MyJcheckBox cell = new MyJcheckBox();
        list.setCellRenderer(cell);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel() {
//            boolean gestureStarted = false;
//            @Override
//            public void setSelectionInterval(int index0, int index1) {
//                System.out.println(index0+":"+index1);
//                if(!gestureStarted) {
//                    if (index0 >= 0 && super.isSelectedIndex(index0)) {
//                        super.removeSelectionInterval(index0, index1);
//                    } else {
//                        super.addSelectionInterval(index0, index1);
//                    }
//                }
//                gestureStarted = true;
//
//            }
//            @Override
//            public void setValueIsAdjusting(boolean isAdjusting) {
//                if (isAdjusting == false) {
//                    gestureStarted = false;
//                }
//            }
//        };
//        defaultListSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
//        list.setSelectionModel(defaultListSelectionModel);
        list.addListSelectionListener(new SharedListSelectionHandler());

//        JPanel leftPanel = new JPanel();
//        leftPanel.add(new Button("ces"));
        constraints.fill = GridBagConstraints.VERTICAL;

        constraints.weightx=0;    // 指定组件的分配区域
        constraints.weighty=0;
        constraints.gridwidth=1;
//        constraints.gridwidth=GridBagConstraints.REMAINDER;    //结束行

        gbaglayout.setConstraints(list,constraints);
        super.getContentPane().add(list);


        constraints.weightx=1;    // 指定组件的分配区域
        constraints.weighty=1;
        constraints.gridwidth=1;
        constraints.fill = GridBagConstraints.BOTH;

//        makeButton("7",super.getContentPane(),gbaglayout,constraints);    //调用方法，添加按钮组件
//        makeButton("8",super.getContentPane(),gbaglayout,constraints);
//        constraints.gridwidth=GridBagConstraints.REMAINDER;    //结束行
//        JScrollPane scrollPane = new JScrollPane(contentPanel);
//        contentPanel.setAutoscrolls(true);
        gbaglayout.setConstraints(scrollPane,constraints);
        super.getContentPane().add(scrollPane);

        JPanel rightPanel = new JPanel();
        rightPanel.add(new Button("c2es"));
        constraints.weightx=0;    // 指定组件的分配区域
        constraints.weighty=0;
        constraints.gridwidth=GridBagConstraints.REMAINDER;    //结束行
        constraints.fill = GridBagConstraints.VERTICAL;

        gbaglayout.setConstraints(rightPanel,constraints);
        super.getContentPane().add(rightPanel);

    }

    public  void reLoadPoint()
    {

        int previewWidth = (contentPanel.getWidth()-20)/rows;
        int previewWidthFull = contentPanel.getWidth()-10;

        int contentPanelWidth = scrollPane.getWidth()-20;
        int contentPanelHeigh = (previewWidth+20)*cols;
//        contentPanel.setPreferredSize(new Dimension(400,0));
//        scrollPane.setPreferredSize(new Dimension(400,0));
        previewPanels =  new HashMap<String,JPanel>();
        contentPanel.removeAll();

        for(int i =0 ;i<rows*cols;i++){
//            for(int j =0 ; j<cols ;j++) {
            JPanel jPanel1 = new JPanel();
            jPanel1.setPreferredSize(new Dimension(previewWidth, previewWidth));
            jPanel1.setName(String.valueOf(i));
            jPanel1.add(new JLabel(ip+":"+i));
            previewPanels.put(jPanel1.getName(),jPanel1);
            jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    System.out.println(evt.getComponent().getName());
                    if(showFull) {
                        previewPanels.forEach((key, value) -> {
                            if (!key.equals(evt.getComponent().getName())) {
                                value.setVisible(true);
                            }
                            value.setPreferredSize(new Dimension(previewWidth, previewWidth));
                        });
                        showFull = false;
                        showFullPanel = null;
                    }
                    else{
                        previewPanels.forEach((key, value) -> {
                            if (!key.equals(evt.getComponent().getName())) {
                                value.setVisible(false);
                            }
                        });
                        previewPanels.get(evt.getComponent().getName()).setPreferredSize(
                                new Dimension(previewWidthFull, previewWidthFull));
                        showFull = true;
                        showFullPanel = previewPanels.get(evt.getComponent().getName());
                    }
                }
            });
            contentPanel.add(jPanel1);
            contentPanel.setPreferredSize(new Dimension(contentPanelWidth,contentPanelHeigh));

            previewPanels.forEach((key, value) -> {
//                        if (!key.equals(showFullPanel.getName())) {
//                            value.setVisible(true);
//                        }
                value.setPreferredSize(
                        new Dimension(previewWidth, previewWidth));
            });
//            }
        }
        contentPanel.revalidate();
    }

    private static boolean showFull = false;
    private static JPanel showFullPanel = null;


    private void loadCrameraList(){
        List<Camera> cameras = cameraMapper.selectList(null);
        Object[] vData = new Object[cameras.size()];

        for (int i = 0; i < cameras.size(); i++) {
            Camera camera = cameras.get(i);
            vData[i] = camera.getIp();
        }
        list.setListData(vData);

    }

    public static void makeButton(String title,Container frame,GridBagLayout gridBagLayout,GridBagConstraints constraints)
    {
        JButton button=new JButton(title);    //创建Button对象
        gridBagLayout.setConstraints(button,constraints);
        frame.add(button);
    }


    //鼠标右键点击事件
    private void mouseRightButtonClick(MouseEvent evt, JTable table, JPopupMenu m_popupMenu) {
        //判断是否为鼠标的BUTTON3按钮，BUTTON3为鼠标右键
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            //通过点击位置找到点击为表格中的行
            int focusedRowIndex = table.rowAtPoint(evt.getPoint());
            if (focusedRowIndex == -1) {
                return;
            }
            //将表格所选项设为当前右键点击的行
            table.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
            //弹出菜单
            m_popupMenu.show(table, evt.getX(), evt.getY());
        }

    }


    class SharedListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ip= list.getSelectedValue();
                    reLoadPoint();


                }
            }).start();


//            }

        }
    }

}
