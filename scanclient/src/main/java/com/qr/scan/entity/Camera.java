package com.qr.scan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * @author qitengfei
 */
@Data
@TableName("t_camera")
public class Camera {
    @TableId(type= IdType.AUTO)
    private Long id;

    private String name;

    private String ip;
    private String port;
    private String username;
    private String passwd;
    private String createTime;

}
