package com.edward.gmall.manage.mapper;

import com.edward.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmsBaseAttrInfoMapper extends tk.mybatis.mapper.common.Mapper<PmsBaseAttrInfo> {
    //不是对象里面的属性的时候，请加上@Param注解
    List<PmsBaseAttrInfo> selectAttrValueListByValueId(@Param("valueIdStr") String valueIdStr);
}
