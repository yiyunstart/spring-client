package com.qr.scan;

import com.qr.scan.entity.SysParam;
import com.qr.scan.mapper.SysParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyAppConst{

    @Autowired
    private SysParamMapper sysParamMapper;
    //视频分辨率
    public  int video_width = 2560;
    public  int video_height = 1440;

    //摄像头跳转到扫描点等待事件
    public  int jumpPointTime = 4000;

    //扫描不到二维码最大重试次数
    public  int max_scan =5;


    public void reLoad(){
        List<SysParam> sysParams = sysParamMapper.selectList(null);
        sysParams.forEach(sysParam -> {
            switch (sysParam.getCode()){
               case  "JumpPointTime":
                   int value = Integer.valueOf(sysParam.getValue())*1000;
                   jumpPointTime = value>0?value:4000;
                   break;
                case "maxScan":
                    max_scan = Integer.valueOf(sysParam.getValue())*1000;
                    break;
                case "videoWidth":
                    video_width = Integer.valueOf(sysParam.getValue())*1000;
                    break;
                case "videoHeight":
                    video_height = Integer.valueOf(sysParam.getValue())*1000;
                    break;
                default:
                    break;
            }
        });
    }
}
