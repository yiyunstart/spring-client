package com.qr.scan.form;

import com.qr.scan.MyAppConst;
import com.qr.scan.entity.SysParam;
import com.qr.scan.mapper.SysParamMapper;
import lombok.extern.java.Log;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 参数配置
 */
@Component
@Log
public class SysParamConfForm extends JFrame {

    JTable table = null;
    String[] tableColName = {"ID", "名称", "编码", "值", "备注说明", "创建时间"};


    @Autowired
    private SysParamMapper sysParamMapper;

    @Autowired
    private MyAppConst myAppConst;

    public SysParamConfForm() {
        initComponents();

        this.setLocationRelativeTo(null);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        loadSysParamList();
    }

    private void initComponents() {

        //创建表格
        table = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 2) {
                    return super.isCellEditable(row, column);
                } else {
                    return false;
                }
            }
        };

        //窗体布局，添加顶部面板
        GridBagLayout gbaglayout = new GridBagLayout();    //创建GridBagLayout布局管理器
        GridBagConstraints constraints = new GridBagConstraints();
        this.setLayout(gbaglayout);    //使用GridBagLayout布局管理器
        constraints.fill = GridBagConstraints.BOTH;    //组件填充显示区域

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
    private void loadSysParamList() {

        List<SysParam> sysParams = sysParamMapper.selectList(null);
        log.info(ToStringBuilder.reflectionToString(sysParams));
        Object[][] vData = new Object[sysParams.size()][tableColName.length];

        for (int i = 0; i < sysParams.size(); i++) {
            SysParam sysParam = sysParams.get(i);
            vData[i][0] = sysParam.getId();
            vData[i][1] = sysParam.getName();
            vData[i][2] = sysParam.getCode();
            vData[i][3] = sysParam.getValue();
            vData[i][4] = sysParam.getRemark();
            vData[i][5] = sysParam.getCreateTime();
        }
        DefaultTableModel model = new DefaultTableModel(vData, tableColName);
        model.addTableModelListener(new TableListener());

        table.setModel(model);
        table.removeColumn(table.getColumnModel().getColumn(0));

    }


    /**
     * 修改值事件
     */
    private class TableListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            if (e.getType() == TableModelEvent.UPDATE) {
                DefaultTableModel tableModel = (DefaultTableModel) e.getSource();
                Object value = tableModel.getValueAt(e.getFirstRow(), e.getColumn());
                Object id = tableModel.getValueAt(e.getFirstRow(), 0);
                sysParamMapper.updateValue(id, value);
                myAppConst.reLoad();
            }

        }
    }

}
