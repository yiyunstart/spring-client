package com.qr.scan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * @author qitengfei
 */
@Data
@TableName("t_camera_point")
public class CameraPoint {
    @TableId(type= IdType.AUTO)
    private Long id;
    private String cameraIp;
    private int name ;
    private String image;
    private int testQrCount;
    private String testImage;
    private String createTime;

}
