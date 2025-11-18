# 🎫 TicketApp

Java Spring Boot ile geliştirilmiş basit bir **bilet satış uygulaması**.

Bu proje, kullanıcıların etkinlikleri görüntüleyip bilet satın
alabildiği; admin tarafında ise etkinlik ekleme, silme, güncelleme gibi
işlemlerin yapılabildiği temel bir backend uygulamasıdır.

------------------------------------------------------------------------

## 📌 Özellikler

### 👤 Kullanıcı İşlemleri

-   Kayıt olma\
-   Giriş yapma (JWT ile)\
-   Bilet satın alma

### 🎭 Etkinlik İşlemleri (Admin)

-   Etkinlik oluşturma\
-   Etkinlik güncelleme\
-   Etkinlik silme\
-   Etkinlik listeleme

### 🎫 Bilet Satın Alma

-   Koltuk kontrolü\
-   Etkinlik ve kullanıcı doğrulama\
-   Yeterli koltuk yoksa hata döndürme\
-   Optimistic locking (aynı anda satın alma çakışmalarını önleme)

### 📊 Satış Raporu

-   Etkinlik başına satılan bilet sayısı\
-   Kalan kapasite\
-   Toplam gelir

------------------------------------------------------------------------

## 🛠 Kullanılan Teknolojiler

-   Java 21\
-   Spring Boot\
-   Spring Web\
-   Spring Data JPA\
-   Spring Security (JWT)\
-   Lombok\
-   H2 Database (test)\
-   MySQL / H2 (dev)\
-   Mockito + JUnit test

------------------------------------------------------------------------

## 📂 Proje Yapısı

    src/main/java/com/ticketapp
     ├── controller/
     ├── service/
     ├── repository/
     ├── security/
     ├── exception/
     └── dto/

------------------------------------------------------------------------

## ▶️ Çalıştırma

1.  Projeyi klonla:

```{=html}
<!-- -->
```
    git clone <repo-link>

2.  Proje klasörüne gir:

```{=html}
<!-- -->
```
    cd ticket-app

3.  Uygulamayı başlat:

```{=html}
<!-- -->
```
    mvn spring-boot:run

------------------------------------------------------------------------

## 🔐 Swagger Arayüzü

Projeyi çalıştırınca şu adresten erişilir:

    http://localhost:8080/swagger-ui.html

------------------------------------------------------------------------

## 🧪 Test Çalıştırma

Tüm testler:

    mvn test

Sadece TicketService test:

    mvn -Dtest=TicketServiceTest test

------------------------------------------------------------------------

## ⚙️ Profiller

-   Varsayılan profil: **dev**\
-   Testler otomatik olarak: **test** profili

------------------------------------------------------------------------

## 👨‍💻 Geliştirici

**Mehmet Ertuğ Elbir**
