# 📌 TicketApp — Bilet Satış Uygulaması (Java 21 + Spring Boot)

TicketApp; etkinlik yönetimi, bilet satışı, kullanıcı kayıt/giriş ve satış raporlama özellikleri içeren tam kapsamlı bir **Java Spring Boot** backend uygulamasıdır.
JWT tabanlı güvenlik, rol bazlı erişim kontrolü, kapsamlı testler ve Swagger/OpenAPI dokümantasyonu içerir.

## 🚀 Teknolojiler
- Java 21
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA (Hibernate)
- H2 Database (in-memory)
- JUnit 5 + Mockito + MockMvc
- Swagger / OpenAPI
- Gradle / Maven

## 📘 Swagger (API Dokümantasyonu)
http://localhost:8080/swagger-ui.html

## 🔐 Kimlik Doğrulama
```
POST /api/auth/register
POST /api/auth/login
```

### Roller:
- ROLE_USER
- ROLE_ADMIN

## 🎫 Event Endpoint’leri
```
GET    /api/events
GET    /api/events/{id}
POST   /api/events        (ADMIN)
PUT    /api/events/{id}   (ADMIN)
DELETE /api/events/{id}   (ADMIN)
```

## 🎟 Ticket Endpoint’leri
```
POST /api/tickets/purchase
{
  "eventId": 1,
  "quantity": 2
}
```

## 📊 Raporlama
```
GET /api/reports/sales
GET /api/reports/sales/full.pdf   (ADMIN)
GET /api/reports/sales/full.csv   (ADMIN)
```

## 📁 Proje Yapısı
```
src/
 ├── main/java/com.ticketapp
 │    ├── controller
 │    ├── service
 │    ├── repository
 │    ├── dto
 │    ├── entity
 │    └── security
 └── test/java/com.ticketapp
      ├── controller
      ├── service
      └── TicketAppApplicationTests
```

## 🧪 Test Kapsamı
- 25+ test
- Controller testleri
- Service testleri
- JWT filtre mock testleri
- Tümü geçiyor
- 1 adet @Disabled güvenlik testi (bilinçli)

## 🗃 Varsayılan Kullanıcılar
| Rol | Username | Şifre |
|------|----------|--------|
| Admin | ahmet | ahmet123 |
| User | ayse | ayse123 |

## 🏁 Çalıştırma
### Maven:
```
mvn spring-boot:run
```
### Gradle:
```
./gradlew bootRun
```

### Testler:
```
mvn test
```

## 🧱 Gelecek Adımlar
- React/Next.js frontend
- JWT login entegrasyonu
- Event listesi arayüzü
- Bilet satın alma UI
- Admin paneli
- Docker Compose entegrasyonu

## 📄 Lisans
MIT License
