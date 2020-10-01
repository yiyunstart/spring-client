package com.qr.scan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_sys_param")
public class SysParam {
    @TableId(type= IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String value;
    private String remark;
    private String typeCode;
    private String createTime;

}
