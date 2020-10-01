package com.qr.scan;

import lombok.Data;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ReviewPanel extends JPanel {

    public ReviewPanel(){
        this.setLayout(new FlowLayout(FlowLayout.LEADING,20,20));
        this.setBackground(Color.gray);    //设置背景色
    }

    private List ipList = new ArrayList();
    private  Map<String,JPanel> previewPanels = new HashMap<String,JPanel>();    //创建面板


    @Override
    public void repaint() {
        super.repaint();
        //先判断移除
        if(previewPanels!= null){
            for(Object ip : previewPanels.keySet()){

                if(!ipList.contains(ip)){
                    this.remove(previewPanels.get(ip));
                }

            }
        }
        if(ipList!= null) {
            for (Object ip : ipList) {
                System.out.println(ip);
                if (previewPanels.containsKey(ip)) {
                    continue;
                }
                JPanel jPanel1 = new JPanel();
                jPanel1.setPreferredSize(new Dimension(100, 100));
                jPanel1.setName(String.valueOf(ip));
                jPanel1.setVisible(true);
                previewPanels.put(jPanel1.getName(), jPanel1);
                this.add(jPanel1);
            }
        }
    }
}
