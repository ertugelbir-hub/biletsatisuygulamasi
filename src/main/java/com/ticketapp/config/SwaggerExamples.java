package com.ticketapp.config;

public final class SwaggerExamples {
    private SwaggerExamples() {}

    // Auth
    public static final String LOGIN_REQ = "{\n" +
            "  \"username\": \"admin\",\n" +
            "  \"password\": \"admin123\"\n" +
            "}";
    public static final String LOGIN_RES = "{\n" +
            "  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n" +
            "}";

    // Event
    public static final String EVENT_CREATE_REQ = "{\n" +
            "  \"title\": \"Swagger Test Konseri\",\n" +
            "  \"city\": \"Ankara\",\n" +
            "  \"type\": \"Concert\",\n" +
            "  \"venue\": \"Arena\",\n" +
            "  \"dateTime\": \"2025-12-10T20:00\",\n" +
            "  \"totalSeats\": 10,\n" +
            "  \"price\": 150\n" +
            "}";
    public static final String EVENT_RES = "{\n" +
            "  \"id\": 1,\n" +
            "  \"title\": \"Swagger Test Konseri\",\n" +
            "  \"city\": \"Ankara\",\n" +
            "  \"venue\": \"Arena\",\n" +
            "  \"dateTime\": \"2025-12-10T20:00\",\n" +
            "  \"totalSeats\": 10,\n" +
            "  \"price\": 150\n" +
            "}";

    // Ticket
    public static final String TICKET_PURCHASE_REQ = "{\n" +
            "  \"eventId\": 1,\n" +
            "  \"quantity\": 3\n" +
            "}";

    // Report
    public static final String REPORT_FULL_LIST = "[{\n" +
            "  \"eventId\": 1,\n" +
            "  \"title\": \"Swagger Test Konseri\",\n" +
            "  \"totalSeats\": 10,\n" +
            "  \"soldInRange\": 0,\n" +
            "  \"soldAllTime\": 3,\n" +
            "  \"remaining\": 7,\n" +
            "  \"price\": 150,\n" +
            "  \"revenueRange\": 0,\n" +
            "  \"revenueAllTime\": 450\n" +
            "}]";
    public static final String REPORT_FULL_PAGE = "{\n" +
            "  \"content\": " + REPORT_FULL_LIST + ",\n" +
            "  \"pageable\": {\"pageNumber\": 0, \"pageSize\": 10, \"offset\": 0, \"paged\": true},\n" +
            "  \"last\": true, \"totalElements\": 1, \"totalPages\": 1,\n" +
            "  \"size\": 10, \"number\": 0, \"first\": true, \"numberOfElements\": 1, \"empty\": false\n" +
            "}";
    // Hata modeli örneği (opsiyonel)
    public static final String ERROR_RES = "{\n" +
            "  \"error\": \"validation\",\n" +
            "  \"details\": [ { \"field\": \"dateTime\", \"message\": \"Tarih formatı yyyy-MM-dd'T'HH:mm olmalı\" } ]\n" +
            "}";
    public static final String EVENTS_SEARCH_EXAMPLE_QUERY =
            "city=Ankara&type=Concert&q=Test&from=2025-12-01T00:00&to=2025-12-31T23:59&page=0&size=5&sort=dateTime&dir=asc";
}
//Bu sınıfı kullanmak için anotasyonlarda value = SwaggerExamples.LOGIN_REQ gibi yazacağız.