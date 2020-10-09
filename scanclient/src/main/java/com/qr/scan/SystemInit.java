package com.qr.scan;

import com.qr.scan.entity.Version;
import com.qr.scan.mapper.SystemMapper;
import com.qr.scan.mapper.VersionMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log
public class SystemInit {

    @Autowired
    private SystemMapper systemMapper;
    @Autowired
    private VersionMapper versionMapper;

    @Autowired
    private MyAppConst myAppConst;

    String version = "1.1";
    public  void init(boolean force){
        int tablesExists = 0;
        tablesExists = systemMapper.checkTableExistsWithSchema("T_VERSION");
        if(tablesExists==0 || force){
            systemMapper.initTableVersion();
        }

        Version version = versionMapper.selectById(1);
        System.out.println("当前系统版本："+version.getVersion());

        //检查参数表是否存在
        tablesExists = systemMapper.checkTableExistsWithSchema("T_SYS_PARAM");
        if(tablesExists==0 || force){
            log.info("初始化系统参数：");
            systemMapper.initTableSysParam();
        }

        //检查摄像头表是否存在
        tablesExists = systemMapper.checkTableExistsWithSchema("T_CAMERA");
        if(tablesExists==0|| force){
            log.info("初始化摄像头表：");
            systemMapper.initTableCamera();
        }
        //检查扫描点表是否存在
        tablesExists = systemMapper.checkTableExistsWithSchema("T_CAMERA_POINT");
        if(tablesExists==0|| force){
            log.info("初始化扫描点表：");
            systemMapper.initTableCameraPoint();
        }


        myAppConst.reLoad();
    }
}
