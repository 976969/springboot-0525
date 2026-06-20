package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BannerMapper {

    List<Banner> selectAll();

    List<Banner> selectActive();

    Banner selectById(@Param("id") Long id);

    int insert(Banner banner);

    int updateById(Banner banner);

    int deleteById(@Param("id") Long id);
}
