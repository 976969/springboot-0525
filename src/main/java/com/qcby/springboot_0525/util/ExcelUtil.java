package com.qcby.springboot_0525.util;

import com.qcby.springboot_0525.entity.EvaluationRecord;
import com.qcby.springboot_0525.entity.EvaluationReport;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;

/**
 * Excel导出工具类
 */
@Component
public class ExcelUtil {

    /**
     * 导出评价报告为Excel
     * @param report 评价报告
     * @param records 评价记录列表
     * @param outputStream 输出流
     */
    public void exportEvaluationReport(EvaluationReport report, List<EvaluationRecord> records,
                                        OutputStream outputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建评价明细Sheet
            Sheet detailSheet = workbook.createSheet("评价明细");

            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建表头行
            Row headerRow = detailSheet.createRow(0);
            String[] headers = {"评价指标", "AI评分", "教师评分", "最终得分", "AI评语", "教师评语"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充评价记录数据
            int rowNum = 1;
            for (EvaluationRecord record : records) {
                Row row = detailSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getIndicatorName() != null ? record.getIndicatorName() : "");
                row.createCell(1).setCellValue(record.getAiScore() != null ? record.getAiScore().doubleValue() : 0);
                row.createCell(2).setCellValue(record.getTeacherScore() != null ? record.getTeacherScore().doubleValue() : 0);
                row.createCell(3).setCellValue(record.getFinalScore() != null ? record.getFinalScore().doubleValue() : 0);
                row.createCell(4).setCellValue(record.getAiComment() != null ? record.getAiComment() : "");
                row.createCell(5).setCellValue(record.getTeacherComment() != null ? record.getTeacherComment() : "");
            }

            // 添加总计行
            Row totalRow = detailSheet.createRow(rowNum);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("加权总分");
            totalLabel.setCellStyle(headerStyle);
            totalRow.createCell(3).setCellValue(report.getTotalScore() != null ? report.getTotalScore().doubleValue() : 0);

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                detailSheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }
}
