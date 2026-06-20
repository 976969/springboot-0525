package com.qcby.springboot_0525.service;

import cn.dev33.satoken.stp.StpUtil;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.TrainingResult;
import com.qcby.springboot_0525.mapper.TrainingResultMapper;
import com.qcby.springboot_0525.util.TikaUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 实训成果业务逻辑层（文件上传、覆盖提交）
 */
@Service
public class TrainingResultService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Resource
    private TrainingResultMapper resultMapper;

    @Resource
    private TikaUtil tikaUtil;

    /** 允许上传的文件扩展名 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "doc", "docx", "pdf", "jpg", "jpeg", "png"
    );

    public TrainingResult getById(Long id) {
        return resultMapper.selectById(id);
    }

    public List<TrainingResult> list() {
        return resultMapper.selectList();
    }

    public List<TrainingResult> listByTeacherId(Long teacherId) {
        return resultMapper.selectByTeacherId(teacherId);
    }

    public List<TrainingResult> listByTaskId(Long taskId) {
        return resultMapper.selectByTaskId(taskId);
    }

    public List<TrainingResult> listByStudentId(Long studentId) {
        return resultMapper.selectByStudentId(studentId);
    }

    /**
     * 查询当前登录学生的成果
     */
    public List<TrainingResult> listByCurrentUser() {
        String loginId = StpUtil.getLoginIdAsString();
        Long studentId = null;
        if (loginId != null && loginId.contains(":")) {
            studentId = Long.parseLong(loginId.split(":")[1]);
        } else {
            studentId = Long.parseLong(loginId);
        }
        return resultMapper.selectByStudentId(studentId);
    }

    /**
     * 上传实训成果文件
     * @param file 上传的文件
     * @param taskId 任务ID
     * @param overwrite 是否覆盖已有的成果
     */
    public TrainingResult upload(MultipartFile file, Long taskId, boolean overwrite) throws IOException {
        // 获取当前登录学生的真实ID
        String loginId = StpUtil.getLoginIdAsString();  // "student:1" 格式
        Long studentId = null;
        if (loginId != null && loginId.contains(":")) {
            studentId = Long.parseLong(loginId.split(":")[1]);
        } else {
            studentId = Long.parseLong(loginId);
        }

        // 检查是否已经提交过
        TrainingResult existingResult = resultMapper.selectByTaskAndStudent(taskId, studentId);
        if (existingResult != null && !overwrite) {
            // 已提交过,且不是覆盖,抛异常提示用户(使用409状态码表示冲突)
            throw new BusinessException(409, "该任务已提交过成果,是否覆盖提交?");
        }

        // 校验文件
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException("不支持的文件格式，仅支持 .doc/.docx/.pdf/.jpg/.jpeg/.png，推荐使用.docx以获得最佳AI识别效果");
        }

        // 生成存储文件名
        String storageName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String dirPath = uploadPath + "result/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created && !dir.exists()) {
                throw new BusinessException("创建上传目录失败：" + dirPath);
            }
        }

        // 保存文件
        File dest = new File(dir, storageName);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败，目录：" + dest.getAbsolutePath() + "，原因：" + e.getMessage());
        }

        // 解析文件内容
        String parsedContent = "";
        try {
            parsedContent = tikaUtil.parseFile(dest);
        } catch (Exception e) {
            // 解析失败不影响上传，记录日志
            parsedContent = "文件解析失败：" + e.getMessage();
        }

        TrainingResult result;
        if (existingResult != null && overwrite) {
            // 覆盖模式: 删除旧文件和关联数据
            String oldFilePath = existingResult.getFilePath();
            if (oldFilePath != null) {
                File oldFile = new File(uploadPath + oldFilePath);
                if (oldFile.exists()) {
                    oldFile.delete();  // 删除旧文件
                }
            }

            // 删除关联的评价记录和报告
            // TODO: 调用EvaluationRecordMapper和EvaluationReportMapper删除
            
            // 更新已有记录
            existingResult.setFilePath("result/" + storageName);
            existingResult.setFileName(originalFilename);
            existingResult.setFileType(ext.toLowerCase());
            existingResult.setFileSize(file.getSize());
            existingResult.setParsedContent(parsedContent);
            existingResult.setStatus(0); // 重置为待核查
            existingResult.setUploadTime(new java.util.Date());
            
            resultMapper.updateById(existingResult);
            result = existingResult;
        } else {
            // 新增模式: 插入新记录
            result = new TrainingResult();
            result.setTaskId(taskId);
            result.setStudentId(studentId);
            result.setFilePath("result/" + storageName);
            result.setFileName(originalFilename);
            result.setFileType(ext.toLowerCase());
            result.setFileSize(file.getSize());
            result.setParsedContent(parsedContent);
            result.setStatus(0); // 待核查
            
            resultMapper.insert(result);
        }
        
        return result;
    }

    /**
     * 更新解析内容
     */
    public void updateParsedContent(Long id, String parsedContent) {
        TrainingResult result = new TrainingResult();
        result.setId(id);
        result.setParsedContent(parsedContent);
        resultMapper.updateById(result);
    }

    /**
     * 更新状态
     */
    public void updateStatus(Long id, Integer status) {
        TrainingResult result = new TrainingResult();
        result.setId(id);
        result.setStatus(status);
        resultMapper.updateById(result);
    }

    public void delete(Long id) {
        resultMapper.deleteById(id);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
