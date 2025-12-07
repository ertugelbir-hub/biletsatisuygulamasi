package com.ticketapp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.ticketapp.dto.TicketNotificationEvent;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.awt.Color;

@Service
public class PdfService {

    public byte[] createTicketPdf(TicketNotificationEvent event) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 1. PDF Dokümanı Oluştur (A5 boyutu bilet için idealdir)
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, baos);
            document.open();

            // 2. Fontlar
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

            // 3. Başlık
            Paragraph title = new Paragraph("TicketApp - Dijital Bilet 🎫", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 4. Bilet Bilgileri (Basit bir liste)
            addInfo(document, "Etkinlik:", event.getEventTitle(), labelFont, valueFont);
            addInfo(document, "Kullanıcı:", event.getUsername(), labelFont, valueFont);
            addInfo(document, "Bilet No:", "#" + event.getTicketId(), labelFont, valueFont);
            addInfo(document, "Adet:", String.valueOf(event.getQuantity()), labelFont, valueFont);
            addInfo(document, "Toplam Tutar:", event.getTotalPrice() + " TL", labelFont, valueFont);

            // 5. QR Kod Oluştur ve Ekle
            // QR içeriği: Bilet ID ve Kullanıcı adını içeren benzersiz bir metin
            String qrContent = "TICKET_ID:" + event.getTicketId() + "|USER:" + event.getUsername();
            Image qrImage = generateQrCodeImage(qrContent);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setSpacingBefore(30);
            qrImage.scaleAbsolute(150, 150); // Boyut ayarı
            document.add(qrImage);

            // Alt not
            Paragraph footer = new Paragraph("Kapıda bu QR kodu okutunuz.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF oluşturulamadı: " + e.getMessage());
        }
    }

    // Yardımcı: QR Kod Resmini Üretir
    private Image generateQrCodeImage(String text) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        return Image.getInstance(pngOutputStream.toByteArray());
    }

    // Yardımcı: Satır Ekleme
    private void addInfo(Document doc, String label, String value, Font labFont, Font valFont) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labFont));
        p.add(new Chunk(value, valFont));
        p.setSpacingAfter(5);
        doc.add(p);
    }
}