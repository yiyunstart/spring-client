package com.qr.scan.swing;

import com.qr.scan.entity.Camera;
import com.qr.scan.utils.HCNetUtils;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class MyVideoPanel extends Panel implements ComponentListener {

    public Component root;

    public Camera camera;

    private boolean play = false;

    public  MyVideoPanel(Component root){
        this.root = root;
        super.addComponentListener(this);
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        if(play || camera ==null ){
            return;
        }
        HCNetUtils.preview(root, this, camera);
        play = true;
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        if(!play){
            return;
        }
        HCNetUtils.previewClose(camera);
        play = false;
    }

}
