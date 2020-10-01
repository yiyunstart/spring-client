package com.qr.scan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qr.scan.entity.User;
import com.qr.scan.entity.Version;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VersionMapper extends BaseMapper<Version> {

}
