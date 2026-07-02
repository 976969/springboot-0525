package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 验证码数据访问层
 */
@Mapper
public interface VerificationCodeMapper {

    /** 插入验证码记录 */
    int insert(VerificationCode vc);

    /** 查询指定目标和类型的最新验证码 */
    VerificationCode selectLatestByTargetAndType(@Param("target") String target, @Param("type") String type);

    /** 查询有效的验证码（未使用、未过期、匹配验证码） */
    VerificationCode selectValidCode(@Param("target") String target, @Param("code") String code, @Param("type") String type);

    /** 标记验证码为已使用 */
    int updateUsed(@Param("id") Long id);
}
