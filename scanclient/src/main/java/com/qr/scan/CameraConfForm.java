package com.qr.scan;

import com.qr.scan.entity.Camera;
import com.qr.scan.mapper.CameraMapper;
import lombok.extern.java.Log;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 摄像头配置
 */
@Component
@Log
public class CameraConfForm extends JFrame {

    /*定义窗口变量*/
    JLabel ipLabel = new JLabel("ip地址");
    JTextField ipText = new JTextField("192.168.1.64");

    JLabel portLabel = new JLabel("端口");
    JTextField portText = new JTextField("8000");

    JLabel userLabel = new JLabel("用户名");
    JTextField userText = new JTextField("admin");

    JLabel passwdLabel = new JLabel("密码");
    JPasswordField passwdText = new JPasswordField("12345678a");

    JTable table = null;
    String[] tableColName = {"ID", "IP地址", "端口", "用户名", "密码"};
    private JPopupMenu popupMenu = null;


    @Autowired
    private CameraMapper cameraMapper;


    public CameraConfForm() {
        initComponents();

        this.setLocationRelativeTo(null);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if(cameraMapper ==null){
            cameraMapper =SpringContextUtil.getBean(CameraMapper.class);
        }
        loadCameraList();
    }

    private void initComponents() {

        //创建表格
        table = new JTable();
        table.addMouseListener(new tableMouseEvent());

        //创建表格右键菜单
        JMenuItem delMenItem = new JMenuItem();
        delMenItem.setText("  删除  ");
        delMenItem.addActionListener(new DelCameraAction());

        popupMenu = new JPopupMenu();
        popupMenu.add(delMenItem);

        //顶部表单布局
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        JButton addButton = new JButton("添加");
        addButton.addActionListener(new AddCameraAction());
        topPanel.add(ipLabel);
        topPanel.add(ipText);
        topPanel.add(portLabel);
        topPanel.add(portText);
        topPanel.add(userLabel);
        topPanel.add(userText);
        topPanel.add(passwdLabel);
        topPanel.add(passwdText);
        topPanel.add(addButton);



        //窗体布局，添加顶部面板
        GridBagLayout gbaglayout = new GridBagLayout();    //创建GridBagLayout布局管理器
        GridBagConstraints constraints = new GridBagConstraints();
        this.setLayout(gbaglayout);    //使用GridBagLayout布局管理器
        constraints.fill = GridBagConstraints.BOTH;    //组件填充显示区域
        constraints.weightx = 0.0;    //恢复默认值
        constraints.gridwidth = GridBagConstraints.REMAINDER;    //结束行
        gbaglayout.setConstraints(topPanel, constraints);
        this.add(topPanel);


        //窗体布局，添加表格面板
        constraints.weightx = 0.5;    // 指定组件的分配区域
        constraints.weighty = 0.2;
        constraints.gridwidth = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;    //结束行
        JScrollPane scrollPane = new JScrollPane(table);
        gbaglayout.setConstraints(scrollPane, constraints);
        this.add(scrollPane);
        this.setBounds(300, 200, 800, 600);

    }

    /**
     * 从数据库中加载配置文件
     */
    private void loadCameraList() {

        List<Camera> cameras = cameraMapper.selectList(null);
        log.info(ToStringBuilder.reflectionToString(cameras));
        Object[][] vData = new Object[cameras.size()][tableColName.length];

        for (int i = 0; i < cameras.size(); i++) {
            Camera camera = cameras.get(i);
            vData[i][0] = camera.getId();
            vData[i][1] = camera.getIp();
            vData[i][2] = camera.getPort();
            vData[i][3] = camera.getUsername();
            vData[i][4] = camera.getPasswd();
        }
        DefaultTableModel model = new DefaultTableModel(vData, tableColName);
        model.addTableModelListener(new TableListener());

        table.setModel(model);
        table.removeColumn(table.getColumnModel().getColumn(0));

    }


    /**
     *  修改摄像头值事件
     */
    private class TableListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            if (e.getType() == TableModelEvent.UPDATE) {
                DefaultTableModel tableModel = (DefaultTableModel) e.getSource();
                Object value = tableModel.getValueAt(e.getFirstRow(), e.getColumn());
                Object id = tableModel.getValueAt(e.getFirstRow(), 0);
                switch (e.getColumn()) {
                    case 1:
                        cameraMapper.updateIp(id, value);
                        break;
                    case 2:
                        cameraMapper.updatePort(id, value);
                        break;
                    case 3:
                        cameraMapper.updateUserNmae(id, value);
                        break;
                    case 4:
                        cameraMapper.updatePasswd(id, value);
                        break;
                }
                SpringContextUtil.getBean(MainApp.class).loadCrameraList();
            }

        }
    }

    /**
     * 添加摄像头事件
     */
    public class AddCameraAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Camera camera = new Camera();
            camera.setIp(ipText.getText());
            camera.setPort(portText.getText());
            camera.setUsername(userText.getText());
            camera.setPasswd(passwdText.getText());

            cameraMapper.insert(camera);
            loadCameraList();
            SpringContextUtil.getBean(MainApp.class).loadCrameraList();
        }
    }

    /**
     * 删除摄像头事件
     */
    public class DelCameraAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //该操作需要做的事
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows == null) {
                return;
            }

            for (int row : selectedRows) {
                Object id = table.getModel().getValueAt(row, 0);
                log.info("删除ID" + id);
                cameraMapper.deleteById(String.valueOf(id));
                SpringContextUtil.getBean(MainApp.class).loadCrameraList();
            }
            for (int row : selectedRows) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.removeRow(table.getSelectedRow());
            }


        }
    }


    /**
     * 表格鼠标右键点击事件，用于删除
     */
    private class  tableMouseEvent extends MouseAdapter {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            //判断是否为鼠标的BUTTON3按钮，BUTTON3为鼠标右键
            if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                //通过点击位置找到点击为表格中的行
                int focusedRowIndex = table.rowAtPoint(evt.getPoint());
                if (focusedRowIndex == -1) {
                    return;
                }
                //弹出菜单
                popupMenu.show(table, evt.getX(), evt.getY());
            }
        }
    }

}
