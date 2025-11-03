// src/main/java/com/ticketapp/service/ReportService.java
package com.ticketapp.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ticketapp.dto.FullSalesReport;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.entity.Event;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {
//Yeni bir servis oluşturuyoruz: tüm event’leri dolaş, her biri için sold/remaining/revenue hesapla.
    private final EventRepository eventRepo;
    private final TicketRepository ticketRepo;

    public ReportService(EventRepository eventRepo, TicketRepository ticketRepo) {
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
    }

    // A) Purchase time

    public List<SalesSummaryItem> summaryByPurchase(LocalDateTime from, LocalDateTime to) {
        return eventRepo.findAll().stream().map(e -> {
            int soldInRange = ticketRepo.sumQuantityByEventIdBetweenPurchase(e.getId(), from, to);
            int allTimeSold = ticketRepo.sumQuantityByEventId(e.getId());
            int remaining   = Math.max(0, e.getTotalSeats() - allTimeSold);

            var price   = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            var revenue = price.multiply(BigDecimal.valueOf(soldInRange));

            return new SalesSummaryItem(
                    e.getId(), e.getTitle(), e.getTotalSeats(),
                    soldInRange, remaining, price, revenue
            );
        }).toList();
    }
    // B) Event date
    public List<SalesSummaryItem> summaryByEvent(LocalDateTime from, LocalDateTime to) {
        return eventRepo.findAll().stream().map(e -> {
            int soldInRange = ticketRepo.sumQuantityByEventIdBetweenEventDate(e.getId(), from, to);
            int allTimeSold = ticketRepo.sumQuantityByEventId(e.getId());
            int remaining   = Math.max(0, e.getTotalSeats() - allTimeSold);

            var price   = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            var revenue = price.multiply(BigDecimal.valueOf(soldInRange));

            return new SalesSummaryItem(
                    e.getId(), e.getTitle(), e.getTotalSeats(),
                    soldInRange, remaining, price, revenue
            );
        }).toList();
    }
    // C) All time
    public SalesReport allTimeSales(Long eventId) {
        var e = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        int soldAllTime = ticketRepo.sumQuantityByEventId(e.getId());
        int remaining   = Math.max(0, e.getTotalSeats() - soldAllTime);

        var price   = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
        var revenue = price.multiply(BigDecimal.valueOf(soldAllTime));

        return new SalesReport(
                e.getId(), e.getTitle(), e.getCity(), e.getVenue(),
                e.getDateTime(), e.getTotalSeats(),
                soldAllTime, remaining, price, revenue
        );


    }
    /** Birleşik rapor: tarih aralığı + all-time metrikler tek listede */
    public List<FullSalesReport> full(LocalDateTime from, LocalDateTime to) {
        // Güvenlik: from > to geldiyse takas et
        LocalDateTime f = from;
        LocalDateTime t = to;
        if (f != null && t != null && f.isAfter(t)) {
            LocalDateTime tmp = f; f = t; t = tmp;
        }
        // 2) tüm etkinlikleri çek
        List<Event> events = eventRepo.findAll();
        List<FullSalesReport> out = new ArrayList<>(events.size());
        // 3) her etkinlik için metrikleri hesapla
        for (Event e : events) {
            Long eventId = e.getId();

            // all-time satış
            int soldAllTime = ticketRepo.sumQuantityByEventId(eventId);
            if (soldAllTime < 0) soldAllTime = 0;

            // tarih aralığı satışı (from/to ikisi de verilmişse)
            int soldInRange = 0;
            if (f != null && t != null) {
                soldInRange = ticketRepo.sumQuantityByEventIdBetweenPurchase(eventId, f, t);
                if (soldInRange < 0) soldInRange = 0;
            }
            // kalan koltuk
            int totalSeats = e.getTotalSeats();
            int remaining = totalSeats - soldAllTime;
            if (remaining < 0) remaining = 0;

            // fiyat ve gelirler
            BigDecimal price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            BigDecimal revenueRange = price.multiply(BigDecimal.valueOf(soldInRange));
            BigDecimal revenueAllTime = price.multiply(BigDecimal.valueOf(soldAllTime));
            // DTO
            out.add(new FullSalesReport(
                    eventId,
                    e.getTitle(),
                    totalSeats,
                    soldInRange,
                    soldAllTime,
                    remaining,
                    price,
                    revenueRange,
                    revenueAllTime
            ));
        }

        return out;
    }
    /**
     * Excel-dostu CSV (UTF-8 BOM + Türkçe başlıklar).
     * Mevcut full(from,to) verisini kullanır.
     */
    public byte[] fullCsv(LocalDateTime from, LocalDateTime to) {
        // 1) Veriyi hazırla
        List<FullSalesReport> list = full(from, to);

        StringBuilder sb = new StringBuilder();
        // Başlık satırı
        sb.append("eventId,title,totalSeats,soldInRange,soldAllTime,remaining,price,revenueRange,revenueAllTime\n");
        // 3) Satırlar
        for (FullSalesReport r : list) {
            // başlıkta/isimde virgül vb. varsa Excel için tırnakla ve iç tırnakları kaçır
            String title = r.title == null ? "" : r.title.replace("\"", "\"\"");
            String price = r.price == null ? "0" : r.price.toPlainString();
            String revenueRange = r.revenueRange == null ? "0" : r.revenueRange.toPlainString();
            String revenueAllTime = r.revenueAllTime == null ? "0" : r.revenueAllTime.toPlainString();

            sb.append(r.eventId).append(",");
            // CSV güvenliği: başlıkta virgül olursa tırnakla
            sb.append("\"").append(r.title == null ? "" : r.title.replace("\"", "\"\"")).append("\",");
            sb.append(r.totalSeats).append(",");
            sb.append(r.soldInRange).append(",");
            sb.append(r.soldAllTime).append(",");
            sb.append(r.remaining).append(",");
            sb.append(r.price == null ? "0" : r.price).append(",");
            sb.append(r.revenueRange == null ? "0" : r.revenueRange).append(",");
            sb.append(r.revenueAllTime == null ? "0" : r.revenueAllTime).append("\n");
        }

        // 4) UTF-8 BOM ile byte dizisi hazırla (Excel Türkçe karakterleri sorunsuz görür)
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream(bom.length + body.length);
        out.write(bom, 0, bom.length);
        out.write(body, 0, body.length);
        return out.toByteArray();

    }
    /** İndirilecek dosya adını tarih aralığına göre üret (controller kullanacak) */
    public String fullCsvFilename(LocalDateTime from, LocalDateTime to) {
        DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        if (from != null && to != null) {
            // from > to takasını full(...) zaten yapıyor; burada isim için min/max alabiliriz
            LocalDateTime a = from.isAfter(to) ? to : from;
            LocalDateTime b = from.isAfter(to) ? from : to;
            return "full-sales-report_" + F.format(a) + "_to_" + F.format(b) + ".csv";
        }
        return "full-sales-report.csv";
    }
    // --- SAYFALI FULL RAPOR (Page<FullSalesReport>) ---

    /** Full raporun sayfalı sürümü: page/size/sort/dir parametresi ile */
    public Page<FullSalesReport> fullPaged(LocalDateTime from, LocalDateTime to,
                                           int page, int size, String sort, String dir) {
        // 1) Sıralama ve sayfalama ayarları
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        // 2) Event'leri sayfalı çek
        var eventPage = eventRepo.findAll(pageable);

        // 3) from>to ise takas (null'lar sorun değil)
        LocalDateTime f = from, t = to;
        if (f != null && t != null && f.isAfter(t)) { LocalDateTime tmp = f; f = t; t = tmp; }

        // 4) Her event için metrikleri hesapla (full(...) ile aynı mantık)
        List<FullSalesReport> content = new ArrayList<>(eventPage.getContent().size());
        for (Event e : eventPage.getContent()) {
            Long eventId = e.getId();

            int soldAllTime = ticketRepo.sumQuantityByEventId(eventId);
            if (soldAllTime < 0) soldAllTime = 0;

            int soldInRange = 0;
            if (f != null && t != null) {
                soldInRange = ticketRepo.sumQuantityByEventIdBetweenPurchase(eventId, f, t);
                if (soldInRange < 0) soldInRange = 0;
            }

            int totalSeats = e.getTotalSeats();
            int remaining = Math.max(0, totalSeats - soldAllTime);

            BigDecimal price = (e.getPrice() == null) ? BigDecimal.ZERO : e.getPrice();
            BigDecimal revenueRange   = price.multiply(BigDecimal.valueOf(soldInRange));
            BigDecimal revenueAllTime = price.multiply(BigDecimal.valueOf(soldAllTime));

            content.add(new FullSalesReport(
                    eventId, e.getTitle(), totalSeats,
                    soldInRange, soldAllTime, remaining,
                    price, revenueRange, revenueAllTime
            ));
        }

        // 5) Page metadata ile dön
        return new PageImpl<>(content, pageable, eventPage.getTotalElements());
    }
    // PDF dosya adı
    public String fullPdfFilename(LocalDateTime from, LocalDateTime to) {
        DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        if (from != null && to != null) {
            LocalDateTime a = from.isAfter(to) ? to : from;
            LocalDateTime b = from.isAfter(to) ? from : to;
            return "full-sales-report_" + F.format(a) + "_to_" + F.format(b) + ".pdf";
        }
        return "full-sales-report.pdf";
    }
    /** Full raporu PDF olarak üretir (Türkçe karakter desteği CP1254). */
    public byte[] fullPdf(LocalDateTime from, LocalDateTime to) {
        List<FullSalesReport> list = full(from, to);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 24, 24); // yatay A4
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Türkçe karakterler için CP1254 kodlamalı standart font
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1254", BaseFont.EMBEDDED);
            Font headerFont = new Font(bf, 12, Font.BOLD);
            Font cellFont   = new Font(bf, 10, Font.NORMAL);

            // Başlık
            String title = "Birleşik Satış Raporu";
            Paragraph p = new Paragraph(title, headerFont);
            p.setSpacingAfter(10f);
            doc.add(p);

            // Tablo (9 sütun)
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10f, 26f, 10f, 12f, 12f, 10f, 10f, 12f, 12f});

            // Header hücreleri
            addHeader(table, "ID", headerFont);
            addHeader(table, "Etkinlik Adı", headerFont);
            addHeader(table, "Toplam", headerFont);
            addHeader(table, "Aralık Satış", headerFont);
            addHeader(table, "Tüm Zaman", headerFont);
            addHeader(table, "Kalan", headerFont);
            addHeader(table, "Fiyat", headerFont);
            addHeader(table, "Aralık Geliri", headerFont);
            addHeader(table, "Toplam Gelir", headerFont);

            // Satırlar
            for (FullSalesReport r : list) {
                addCell(table, String.valueOf(r.eventId), cellFont);
                addCell(table, r.title == null ? "" : r.title, cellFont);
                addCell(table, String.valueOf(r.totalSeats), cellFont);
                addCell(table, String.valueOf(r.soldInRange), cellFont);
                addCell(table, String.valueOf(r.soldAllTime), cellFont);
                addCell(table, String.valueOf(r.remaining), cellFont);
                addCell(table, r.price == null ? "0" : r.price.toPlainString(), cellFont);
                addCell(table, r.revenueRange == null ? "0" : r.revenueRange.toPlainString(), cellFont);
                addCell(table, r.revenueAllTime == null ? "0" : r.revenueAllTime.toPlainString(), cellFont);
            }

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("PDF oluşturulamadı", ex);
        }
    }

    // küçük yardımcılar
    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font)); // hücre + yazı
        cell.setPadding(5f); // biraz boşluk bırak
        table.addCell(cell); // tabloya ekle
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));// normal hücre
        cell.setPadding(4f); // biraz dar boşluk
        table.addCell(cell);
    }

}

