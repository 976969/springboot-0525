package com.qcby.springboot_0525.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

/**
 * Apache Tika 文档解析工具
 */
@Component
public class TikaUtil {

    private final Tika tika = new Tika();

    /**
     * 解析文件提取文本内容
     */
    public String parseFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            String content = tika.parseToString(fis);
            // 限制内容长度，避免过大
            if (content != null && content.length() > 10000) {
                content = content.substring(0, 10000) + "\n...（内容过长已截断）";
            }
            return content;
        }
    }
}
