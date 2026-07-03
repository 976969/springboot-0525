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
    int insert(VerificationCode verificationCode);

    /** 根据目标、验证码、类型查询最新有效且未使用的记录 */
    VerificationCode selectValidCode(@Param("target") String target,
                                     @Param("code") String code,
                                     @Param("type") String type);

    /** 标记验证码为已使用 */
    int updateUsed(@Param("id") Long id);

    /** 查询最近一次发送时间（用于频率限制） */
    VerificationCode selectLatestByTargetAndType(@Param("target") String target,
                                                 @Param("type") String type);
}
