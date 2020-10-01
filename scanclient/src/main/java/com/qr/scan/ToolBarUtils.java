package com.qr.scan;

import javax.swing.*;
import java.net.URL;

public class ToolBarUtils {
    public static JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText)
    {
        //搜索图片
        String imgLocation=imageName+".jpg";
        URL imageURL=MainApp.class.getResource(imgLocation);
        //初始化工具按钮
        JButton button=new JButton();
        //设置按钮的命令
        button.setActionCommand(actionCommand);
        //设置提示信息
        button.setToolTipText(toolTipText);
//        button.addActionListener(this);
        if(imageURL!=null)
        {
            //找到图像
            button.setIcon(new ImageIcon(imageURL));
        }
        else
        {
            //没有图像
            button.setText(altText);
            System.err.println("Resource not found: "+imgLocation);
        }
        return button;
    }
}
