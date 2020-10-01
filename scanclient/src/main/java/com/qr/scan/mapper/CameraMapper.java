package com.qr.scan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qr.scan.entity.Camera;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CameraMapper extends BaseMapper<Camera> {

    @Update("update t_camera set ip=#{ip} where id=#{id}")
    public void updateIp(@Param("id") Object id, @Param("ip") Object ip);

    @Update("update t_camera set port=#{port} where id=#{id}")
    public void updatePort(@Param("id") Object id, @Param("port") Object port);

    @Update("update t_camera set username=#{username} where id=#{id}")
    public void updateUserNmae(@Param("id") Object id, @Param("username") Object username);

    @Update("update t_camera set passwd=#{passwd} where id=#{id}")
    public void updatePasswd(@Param("id") Object id, @Param("passwd") Object passwd);
}
