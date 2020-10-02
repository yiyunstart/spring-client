package com.qr.scan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qr.scan.entity.Camera;
import com.qr.scan.entity.CameraPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CameraPointMapper extends BaseMapper<CameraPoint> {

    @Select("select * from t_camera_point where camera_ip=#{cameraIp} order by name")
    public List<CameraPoint> selectByIp(@Param("cameraIp") Object cameraIp);
    @Select("select * from t_camera_point where camera_ip=#{cameraIp} and name=#{name} order by name")
    public CameraPoint selectByName(@Param("cameraIp") Object cameraIp,@Param("name") Object name);
}
