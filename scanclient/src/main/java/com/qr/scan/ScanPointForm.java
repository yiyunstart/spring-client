package com.qr.scan;

import com.qr.scan.entity.Camera;
import com.qr.scan.mapper.CameraMapper;
import com.sun.deploy.panel.NumberDocument;
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


//        constraints.gridwidth=GridBagConstraints.REMAINDER;
//        gbaglayout.setConstraints(panel, constraints);
//
//        super.getContentPane().add(panel);



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

        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200,100));
        leftPanel.setBorder(BorderFactory.createTitledBorder("摄像头"));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(list);
        gbaglayout.setConstraints(leftPanel,constraints);
        super.getContentPane().add(leftPanel);


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
        rightPanel.setLayout(new FlowLayout());
        rightPanel.setPreferredSize(new Dimension(200,0));



        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEADING,5,5));
        panel.setBorder(BorderFactory.createTitledBorder("网格大小"));
        panel.setPreferredSize(new Dimension(200,60));
        JTextField rowsText=new JTextField();
        rowsText.setPreferredSize(new Dimension(40,28));
        rowsText.setHorizontalAlignment(JTextField.CENTER);
        rowsText.setDocument(new NumberDocument());
        rowsText.setText(String.valueOf(rows));
        JLabel portLabel=new JLabel("x");
        JTextField colsText=new JTextField("3");
        colsText.setPreferredSize(new Dimension(40,28));
        colsText.setHorizontalAlignment(JTextField.CENTER);
        colsText.setDocument(new NumberDocument());
        colsText.setText(String.valueOf(cols));

        JButton addButton = new JButton("设置");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cols = Integer.valueOf(rowsText.getText());
                rows = Integer.valueOf(colsText.getText());
                reLoadPoint();
            }
        });

        panel.add(rowsText);
        panel.add(portLabel);
        panel.add(colsText);
        panel.add(addButton);
        rightPanel.add(panel);


        Button leftUpBtn = new Button("左上");
        Button leftBtn = new Button("左");
        Button leftDownBtn = new Button("左下");
        Button upBtn = new Button("上");
        Button rightUpBtn = new Button("右上");
        Button rightBtn = new Button("右");
        Button rightDownBtn = new Button("右下");
        Button downBtn = new Button("下");
        Button autoBtn = new Button("自动");

        JPanel movePanel = new JPanel();
        movePanel.setLayout(new GridLayout(3,6,5,5));

        movePanel.setBorder(BorderFactory.createTitledBorder("方向"));
        movePanel.add(leftUpBtn);
        movePanel.add(upBtn);
        movePanel.add(rightUpBtn);
        movePanel.add(leftBtn);
        movePanel.add(autoBtn);
        movePanel.add(rightBtn);
        movePanel.add(leftDownBtn);
        movePanel.add(downBtn);
        movePanel.add(rightDownBtn);
        movePanel.setPreferredSize(new Dimension(200,120));
        rightPanel.add(movePanel);

        JPanel csPanel = new JPanel();
        csPanel.setLayout(new GridLayout(3,6,5,5));
        csPanel.setBorder(BorderFactory.createTitledBorder("参数"));
        csPanel.setPreferredSize(new Dimension(200,120));

        Button tjBtn_1 = new Button("缩");
        Button tjBtn_2 = new Button("伸");
        Button jjBtn_1 = new Button("近");
        Button jjBtn_2 = new Button("远");
        Button gqBtn_1 = new Button("大");
        Button gqBtn_2 = new Button("小");
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
        yzdPanel.setLayout(new GridLayout(1,3,5,5));
        yzdPanel.setBorder(BorderFactory.createTitledBorder("预置点"));
        yzdPanel.setPreferredSize(new Dimension(200,50));

        Button yzd_dyBtn = new Button("调用");
        Button yzd_szBtn = new Button("设置");
        Button yzd_scBtn = new Button("删除");
        yzdPanel.add(yzd_dyBtn);
        yzdPanel.add(yzd_szBtn);
        yzdPanel.add(yzd_scBtn);

        rightPanel.add(yzdPanel);

        JPanel jPanelVideoPara = new JPanel();


        jPanelVideoPara.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelVideoPara.setLayout(new FlowLayout());
        jPanelVideoPara.setPreferredSize(new Dimension(200,200));

        Dimension dimensionLabel = new Dimension(40,28);
        Dimension dimensionSlide = new Dimension(130,28);
        JLabel jLabel5 = new JLabel("对比度");
        jLabel5.setPreferredSize(dimensionLabel);
        jPanelVideoPara.add(jLabel5);
        JSlider jSliderContrast = new JSlider();
        jSliderContrast.setPreferredSize(dimensionSlide);

        jSliderContrast.setMaximum(10);
        jSliderContrast.setMinimum(1);
        jSliderContrast.setValue(6);
        jSliderContrast.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                jSliderContrastStateChanged(evt);
            }
        });
        jPanelVideoPara.add(jSliderContrast);



        JLabel jLabel6 = new JLabel("饱和度");
        jLabel6.setPreferredSize(dimensionLabel);
        jPanelVideoPara.add(jLabel6);
        JSlider jSliderSaturation = new JSlider();
        jSliderSaturation.setPreferredSize(dimensionSlide);

        jSliderSaturation.setMaximum(10);
        jSliderSaturation.setMinimum(1);
        jSliderSaturation.setValue(6);
        jSliderSaturation.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                jSliderSaturationStateChanged(evt);
            }
        });
        jPanelVideoPara.add(jSliderSaturation);

        JLabel jLabel7 = new JLabel("色度");
        jLabel7.setPreferredSize(dimensionLabel);
        jPanelVideoPara.add(jLabel7);
        JSlider jSliderHue = new JSlider();
        jSliderHue.setPreferredSize(dimensionSlide);

        jSliderHue.setMaximum(10);
        jSliderHue.setMinimum(1);
        jSliderHue.setValue(6);
        jSliderHue.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                jSliderHueStateChanged(evt);
            }
        });
        jPanelVideoPara.add(jSliderHue);

        JLabel jLabel9 = new JLabel("亮度");
        jLabel9.setPreferredSize(dimensionLabel);
        jPanelVideoPara.add(jLabel9);
        JSlider jSliderBright = new JSlider();
        jSliderBright.setPreferredSize(dimensionSlide);
        jSliderBright.setMaximum(10);
        jSliderBright.setMinimum(1);
        jSliderBright.setValue(6);
        jSliderBright.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                jSliderBrightStateChanged(evt);
            }
        });
        jPanelVideoPara.add(jSliderBright);
        rightPanel.add(jPanelVideoPara);

        JButton jButtonDefault = new JButton("默认值");
//        jButtonDefault.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                jButtonDefaultActionPerformed(evt);
//            }
//        });
        jPanelVideoPara.add(jButtonDefault);









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
