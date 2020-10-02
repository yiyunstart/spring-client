package com.qr.scan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemMapper extends BaseMapper {
    /**
     * 使用information_schema检查表是否存在
     *
     * @param tableName
     * @return
     */
    Integer checkTableExistsWithSchema(@Param("tableName") String tableName);
    Integer initTableVersion();

    Integer initTableSysParam();

    Integer initTableCamera();

    Integer initTableCameraPoint();
}
