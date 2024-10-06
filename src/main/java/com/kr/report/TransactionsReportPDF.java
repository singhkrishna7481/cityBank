package com.kr.report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.kr.model.TransactionsHistory;
import com.kr.model.UserAccount;

@Component
public class TransactionsReportPDF {

	public void generateReport(List<TransactionsHistory> list, UserAccount user)
			throws DocumentException, FileNotFoundException {
		Document document = new Document(PageSize._11X17);

		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream("./src/main/resources/reports/report.pdf"));

		document.open();
		PdfContentByte canvas = writer.getDirectContent();
		Font watermarkFont = new Font(FontFamily.HELVETICA, 70, Font.BOLD,new BaseColor(230, 228, 198));
		ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
				new Phrase("City Bank Transactions", watermarkFont),
				(document.getPageSize().getWidth() / 2), (document.getPageSize().getHeight() / 2), 30);
		
//		document.addHeader("City Bank", "city");
		Font logoFont = new Font(FontFamily.TIMES_ROMAN, 40, Font.BOLD | Font.UNDERLINE,new BaseColor(255, 101, 0));
		Paragraph logo = new Paragraph("City Bank Transactions", logoFont);
		logo.setAlignment(Element.ALIGN_CENTER);

		Font headingFont = new Font(FontFamily.HELVETICA, 25, Font.BOLD,new BaseColor(85, 124, 86));
		Paragraph userHeading = new Paragraph("Hello, " + user.getName(), headingFont);
		userHeading.setAlignment(Element.ALIGN_CENTER);
		Paragraph timeStamp = new Paragraph(
				"Report Generated On:: "
						+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM, yyyy hh:mm:ss a")),
				new Font(FontFamily.COURIER, 10, Font.UNDERLINE));
		timeStamp.setAlignment(Element.ALIGN_CENTER);
		Paragraph message = new Paragraph("Your Recent Transactions History");
		message.setAlignment(Element.ALIGN_CENTER);

		document.add(logo);
		document.add(new Paragraph("\n"));
		document.add(userHeading);
		document.add(new Paragraph("\n"));
		document.add(timeStamp);
		document.add(new Paragraph("\n"));
		document.add(message);
		document.add(new Paragraph("\n"));

		PdfPTable table = new PdfPTable(5);
		tableHeader(table);
		addRow(table, list);

		document.add(table);

		// Add metadata to the document
		document.addTitle(user.getUsername() + " Transactions Report");
		document.addSubject("Transaction History");
//		document.addKeywords("PDF, iText, Java");
		document.addAuthor("Krishna Singh");
		document.addCreator("Krishna Singh");
		document.close();
	}

	private void tableHeader(PdfPTable table) {
		Font headerFont = new Font(FontFamily.TIMES_ROMAN, 10, Font.BOLD,BaseColor.BLACK);
		Stream.of("S.No.", "Type", "Amount", "Date", "Time").forEach(title -> {
			PdfPCell cell = new PdfPCell();
			cell.setBackgroundColor(new BaseColor(255, 136, 91));

			cell.setBorderWidth(1);
			cell.setBorderColor(new BaseColor(16, 24, 32));
			cell.setPhrase(new Phrase(title, headerFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.setWidthPercentage(100); // Set table width to 100%
			try {
				table.setWidths(new int[] { 5, 10, 10, 10, 15 });
			} catch (DocumentException e) {
				e.printStackTrace();
			}
			table.addCell(cell);
		});
	}

	private void addRow(PdfPTable table, List<TransactionsHistory> list) {
		list.stream().forEach(history -> {
			String transType = history.getTransType();
			boolean isSent = (transType.equalsIgnoreCase("Deposit")) ? true : false;

			table.addCell(createCell("" + (list.indexOf(history) + 1), isSent));
			table.addCell(createCell(transType, isSent));
			table.addCell(createCell(((isSent) ? "+" : "-") + "$ " + history.getTransAmt(), isSent));
			table.addCell(createCell(history.getTimeStamp().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), isSent));
			table.addCell(createCell(history.getTimeStamp().format(DateTimeFormatter.ofPattern("hh:mm:ss a")), isSent));
		});
	}

	private PdfPCell createCell(String text, boolean isSent) {
		Font rowContentText = new Font(FontFamily.HELVETICA, 8, Font.BOLD);
		PdfPCell cell = new PdfPCell(new Phrase(text, rowContentText));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setBorderColor(new BaseColor(16, 24, 32));
//	    cell.setBackgroundColor(new BaseColor(16, 24, 32));
		if (isSent) {
			rowContentText.setColor(new BaseColor(17, 117, 84));
		} else {
			rowContentText.setColor(new BaseColor(217, 22, 86));
		}
		return cell;
	}
}
