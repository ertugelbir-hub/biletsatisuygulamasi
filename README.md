🎫 Ticket App - Bilet Satış ve Etkinlik Yönetim Sistemi

Modern mimari ile geliştirilmiş, Dockerize edilmiş, güvenli ve ölçeklenebilir Bilet Satış Platformu.
Kullanıcılar etkinlikleri arayabilir ve bilet satın alabilir; Yöneticiler (Admin) ise gelişmiş panel üzerinden etkinlikleri, satışları ve kullanıcıları yönetebilir.

🚀 Özellikler

👤 Kullanıcı Paneli

Gelişmiş Arama: Şehir, Tür ve İsim bazlı dinamik filtreleme (Specification API).

Bilet Satın Alma: Stok takibi ve eşzamanlılık (concurrency) koruması ile güvenli satın alım.

Biletlerim: Satın alınan biletlerin listelenmesi ve iptal/iade işlemleri.

Profil Yönetimi: Şifre değiştirme ve profil görüntüleme.

👔 Yönetici (Admin) Paneli

Etkinlik Yönetimi: Ekleme, Düzenleme, Silme (CRUD).

Satış Raporları: Tarih aralığına göre ciro, satılan bilet ve doluluk oranları.

Rapor Dışa Aktarma: Satış verilerini PDF ve Excel (CSV) olarak indirme.

Kullanıcı ve Bilet Yönetimi: Kullanıcıların geçmişini görme, bilet iptal etme.

🛠️ Teknolojiler

Backend (Java & Spring Boot)

Framework: Spring Boot 3.5.3

Dil: Java 21

Veritabanı: PostgreSQL 15

ORM: Hibernate 6 + Spring Data JPA (Specification Pattern)

Güvenlik: Spring Security + JWT (JSON Web Token)

Dokümantasyon: Swagger UI (OpenAPI 3)

Raporlama: OpenPDF

Frontend (React)

Framework: React 18 + Vite

Stil: Bootstrap 5 + Özel CSS

HTTP İstemcisi: Axios (Interceptor destekli)

Bildirimler: React Toastify

Altyapı (DevOps)

Container: Docker & Docker Compose

Veritabanı Yönetimi: Otomatik init.sql ve Volume yapılandırması.

⚙️ Kurulum ve Çalıştırma

Projeyi ayağa kaldırmak için bilgisayarınızda Docker ve Docker Compose yüklü olması yeterlidir.

1. Projeyi İndirin

git clone [https://github.com/ertugelbir-hub/biletsatisuygulamasi](https://github.com/ertugelbir-hub/biletsatisuygulamasi-frontend)
cd ticket-app


2. Docker ile Başlatın (Önerilen)

Tek bir komutla Veritabanı, Backend ve Frontend servislerini başlatın:

docker-compose up -d --build


Bu işlem ilk seferde kütüphanelerin indirilmesi nedeniyle birkaç dakika sürebilir.

3. Uygulamaya Erişin

Frontend (Web Arayüzü): http://localhost:5173

Backend API: http://localhost:8080

Swagger API Dokümantasyonu: http://localhost:8080/swagger-ui.html

🧪 Test Kullanıcıları

Sistem ilk açıldığında otomatik olarak aşağıdaki kullanıcıları oluşturur:

Rol

Kullanıcı Adı

Şifre

Yetkiler

Admin

admin

admin123

Tam yetki (Panel erişimi, Raporlar, CRUD)

User

ahmet

ahmet123

Bilet alma, Biletlerim

User

ayse

ayse123

Bilet alma, Biletlerim

📂 Proje Yapısı

ticket-app/
├── docker-compose.yml      # Docker orkestrasyon dosyası
├── ticket-app/             # Backend (Spring Boot) Kodları
│   ├── src/main/java/      # Controller, Service, Repository, Entity
│   └── Dockerfile          # Backend imaj dosyası
└── ticketapp-frontend/     # Frontend (React) Kodları
    ├── src/components/     # Admin, Auth, Events bileşenleri
    └── Dockerfile          # Frontend imaj dosyası

👨‍💻 Geliştirici

Mehmet Ertuğ Elbir