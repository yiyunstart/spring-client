package com.qr.scan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_version")
public class Version {
    @TableId(type= IdType.AUTO)
    private Long id;
    private String version;
    private String createTime;

}
