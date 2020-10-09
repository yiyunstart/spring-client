package com.qr.scan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qr.scan.entity.SysParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysParamMapper extends BaseMapper<SysParam> {

    @Update("update t_sys_param set value=#{value},create_time=now() where id=#{id}")
    public void updateValue(@Param("id") Object id, @Param("value") Object value);

    @Select("select * from t_sys_param where code=#{code} ")
    SysParam getByCode(@Param("code") Object code);
}
