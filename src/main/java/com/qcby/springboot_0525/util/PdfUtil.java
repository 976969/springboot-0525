package com.qcby.springboot_0525.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.qcby.springboot_0525.entity.EvaluationRecord;
import com.qcby.springboot_0525.entity.EvaluationReport;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;

/**
 * PDF导出工具
 */
@Component
public class PdfUtil {

    public void exportEvaluationReport(EvaluationReport report, List<EvaluationRecord> records,
                                        OutputStream outputStream) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // 中文字体支持
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(bfChinese, 18, Font.BOLD);
        Font headerFont = new Font(bfChinese, 12, Font.BOLD);
        Font normalFont = new Font(bfChinese, 10, Font.NORMAL);

        // 标题
        Paragraph title = new Paragraph("实训评价报告", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // 基本信息
        document.add(new Paragraph("学生：" + (report.getStudentName() != null ? report.getStudentName() : ""),
                normalFont));
        document.add(new Paragraph("实训任务：" + (report.getTaskTitle() != null ? report.getTaskTitle() : ""),
                normalFont));
        document.add(new Paragraph("加权总分：" + (report.getTotalScore() != null ? report.getTotalScore().toString() : "0"),
                headerFont));
        document.add(Chunk.NEWLINE);

        // 评价明细表格
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 1.5f, 1.5f, 3f});

        // 表头
        addCell(table, "评价指标", headerFont, true);
        addCell(table, "AI评分", headerFont, true);
        addCell(table, "最终得分", headerFont, true);
        addCell(table, "评语", headerFont, true);

        // 数据行
        for (EvaluationRecord record : records) {
            addCell(table, record.getIndicatorName() != null ? record.getIndicatorName() : "", normalFont, false);
            addCell(table, record.getAiScore() != null ? record.getAiScore().toString() : "0", normalFont, false);
            addCell(table, record.getFinalScore() != null ? record.getFinalScore().toString() : "0", normalFont, false);
            String comment = "";
            if (record.getTeacherComment() != null && !record.getTeacherComment().isEmpty()) {
                comment = record.getTeacherComment();
            } else if (record.getAiComment() != null) {
                comment = record.getAiComment();
            }
            addCell(table, comment, normalFont, false);
        }

        document.add(table);
        document.close();
    }

    private void addCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        if (isHeader) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        }
        table.addCell(cell);
    }
}
