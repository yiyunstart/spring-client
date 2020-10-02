package com.qr.scan.utils;

import com.qr.scan.MainApp;
import com.qr.scan.MyAppConst;
import com.qr.scan.ScanPointForm;
import com.qr.scan.entity.Camera;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HCNetUtils {
    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    public static NativeLong getRealHandle(String ip){

        return lPreviewHandleMap.get(ip);
    }

    /*************************************************
     函数名:    PTZControlAll
     函数描述:	云台控制函数
     输入参数:
     lRealHandle: 预览句柄
     iPTZCommand: PTZ控制命令
     iStop: 开始或是停止操作
     输出参数:
     返回值:
     *************************************************/
    public static void PTZControlAll(Component component, String ip, int iPTZCommand, int iStop, int iSpeed) {
//        int iSpeed = jComboBoxSpeed.getSelectedIndex();
        NativeLong lRealHandle  = lPreviewHandleMap.get(ip);
        if (lRealHandle.intValue() >= 0) {
            boolean ret;
            if (iSpeed >= 1)//有速度的ptz
            {
                ret = hCNetSDK.NET_DVR_PTZControlWithSpeed(lRealHandle, iPTZCommand, iStop, iSpeed);
                if (!ret) {
                    JOptionPane.showMessageDialog(component, "云台控制失败");
                    return;
                }
            } else//速度为默认时
            {
                ret = hCNetSDK.NET_DVR_PTZControl(lRealHandle, iPTZCommand, iStop);
                if (!ret) {
                    JOptionPane.showMessageDialog(component, "云台控制失败");
                    return;
                }
            }
        }
    }

    private static Map<String,NativeLong> lUserIDMap = new HashMap<String,NativeLong>();
    private static Map<String,NativeLong> lPreviewHandleMap = new HashMap<String,NativeLong>();

    public  static  NativeLong register(Component parentComponent,  Camera camera) {
//        Camera camera = cameraMapper.selectByIp(ip);
//        m_sDeviceIP = jTextFieldIPAddress.getText();//设备ip地址
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int iPort = Integer.parseInt(camera.getPort());
        NativeLong lUserID = hCNetSDK.NET_DVR_Login_V30(camera.getIp(),
                (short) iPort, camera.getUsername(), camera.getPasswd(), m_strDeviceInfo);

        long userID = lUserID.longValue();
        if (userID == -1) {
//            m_sDeviceIP = "";//登录未成功,IP置为空
            JOptionPane.showMessageDialog(parentComponent, "注册失败");
        } else {
            lUserIDMap.put(camera.getIp(),lUserID);
//            log.info("注册成功");
//            CreateDeviceTree();
            return lUserID;
        }
        return  null;
    }

    /**
     * 获取截图
     */
    public static BufferedImage getPreviewImageToBufferdImage(String ip) {
        final byte[] imageData = getPreviewImage(ip);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }

    /**
     * 获取截图
     */
    public static byte[] getPreviewImage(String ip) {
        NativeLong lUserID = lUserIDMap.get(ip);
        if(lUserID==null){
            return null;
        }
        HCNetSDK.NET_DVR_JPEGPARA jpeginfo = new HCNetSDK.NET_DVR_JPEGPARA();
        jpeginfo.wPicQuality = 2;
        jpeginfo.wPicSize = 0;
        int dwPicSize = MyAppConst.video_width * MyAppConst.video_height;
        IntByReference lpSizeReturned = new IntByReference();
        lpSizeReturned.setValue(0);
        NativeLong DVRChannel = new NativeLong();
//        DVRChannel.setValue(getChannelNumber());
        DVRChannel.setValue(1);
        Pointer p = new Memory(MyAppConst.video_width * MyAppConst.video_height);

        hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(lUserID, DVRChannel, jpeginfo, p, dwPicSize, lpSizeReturned);
        byte[] imageData = p.getByteArray(0, lpSizeReturned.getValue());

        return imageData;
    }
    //预览
    public static NativeLong preview(Component parentComponent, Panel panel,Camera camera) {

        //获取窗口句柄
        W32API.HWND hwnd = new W32API.HWND(Native.getComponentPointer(panel));

        NativeLong lUserID = lUserIDMap.get(camera.getIp());
        if(lUserID ==null){
            lUserID = register(parentComponent,camera);
        }
        if(lUserID ==null){
            return null;
        }
        if(lPreviewHandleMap.containsKey(camera.getIp())&&lPreviewHandleMap.get(camera.getIp()).equals(hwnd)){
            return lPreviewHandleMap.get(camera.getIp());
        }
        if(lPreviewHandleMap.containsKey(camera.getIp())&&!lPreviewHandleMap.get(camera.getIp()).equals(hwnd)){
            hCNetSDK.NET_DVR_StopRealPlay(lPreviewHandleMap.get(camera.getIp()));
        }

        //获取通道号
        int iChannelNum = 1;//通道号
        if (iChannelNum == -1) {
            JOptionPane.showMessageDialog(parentComponent, "请选择要预览的通道");
            return null;
        }

        HCNetSDK.NET_DVR_CLIENTINFO m_strClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();
        m_strClientInfo.lChannel = new NativeLong(iChannelNum);

        //在此判断是否回调预览,0,不回调 1 回调
        m_strClientInfo.hPlayWnd = hwnd;
        NativeLong  lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V30(lUserID,
                m_strClientInfo, null, null, true);

        long previewSucValue = lPreviewHandle.longValue();

        //预览失败时:
        if (previewSucValue == -1) {
            JOptionPane.showMessageDialog(parentComponent, "预览失败");
            lPreviewHandleMap.remove(camera.getIp());
//            previewVideoPanels.get(key).play = false;
            return null;
        }else{
//            previewVideoPanels.get(key).play = true;
            lPreviewHandleMap.put(camera.getIp(),lPreviewHandle);
        }
        return lPreviewHandle;
    }

}
