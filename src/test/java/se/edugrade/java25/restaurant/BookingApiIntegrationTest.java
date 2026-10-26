package se.edugrade.java25.restaurant;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.notification.NotificationClient;
import se.edugrade.java25.restaurant.repository.BookingRepository;
import se.edugrade.java25.restaurant.support.TestDataSeeder;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

/* Test med RestAssured - Vad händer här?
1. @SpringBootTest(RANDOM_PORT) startar en riktig HTTP-server.
2. @ActiveProfiles("test") aktiverar min separata H2-testdatabas.
3. @Import(TestDataSeeder.class) gör min testdataklass tillgänglig.
4. @MockitoBean ersätter den riktiga JWT-avkodaren.
5. testDataSeeder.reset() återställer testdatan före varje test.
6. when(jwtDecoder.decode(...)) bestämmer vilken JWT Spring Security ska få när testet skickar "test-token"

RestAssured + mockad JwtDecoder är bra för att testa HTTP-flödet inklusive JWT-rollkonvertering
MockMvc + jwt() för enklare tester av exempelvis idempotens och åtkomstregler, där man inte behöver
testa själva JWT-avkodningen.
**/

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // För att TestDataSeeder säkert ska användas och inte SampleData
@Import(TestDataSeeder.class) // registrerar klassen i Spring-kontexten, så att @Autowired fungerar.
public class BookingApiIntegrationTest {

    @LocalServerPort private int port;

    private RequestSpecification requestSpecGuest, requestSpecStaff, requestSpecAdmin;
    private Long bookingId;
    private Long restaurantId;

    // *1 - Se förklaringar längst ner
    @Autowired private TestDataSeeder testDataSeeder;

    // Spring skapar här ett mock-objekt med Mockito och använder det i stället
    // för den riktiga beanen i testkontexten.
    @MockitoBean private JwtDecoder jwtDecoder;

    @MockitoBean private NotificationClient notificationClient;
    @Autowired private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        TestDataSeeder.TestIds testIds = testDataSeeder.reset();  //Tömmer databasen först och fyller sen med fräsch testdata

        bookingId = testIds.bookingId();
        restaurantId = testIds.restaurantId();

        // Förbereder mockade JWT för de roller som testerna använder.
        // jwtDecoder används för att bestämma vilken JWT mocken ska returnera till Spring Security
        // när testet skickar "test-token"
        RestAssuredSpecs.mockJwt(jwtDecoder, "GUEST");
        RestAssuredSpecs.mockJwt(jwtDecoder, "STAFF");
        RestAssuredSpecs.mockJwt(jwtDecoder, "ADMIN");

        // Skapar requestSpecificationer för de olika rollerna
        requestSpecGuest = RestAssuredSpecs.request(port, "GUEST");
        requestSpecStaff = RestAssuredSpecs.request(port, "STAFF");
        requestSpecAdmin = RestAssuredSpecs.request(port, "ADMIN");
    }

    String bookingJson() {
        return """
        {
          "restaurantId": %d,
          "guestName": "Alice Andersson",
          "guestEmail": "alice@example.com",
          "arrivalTime": "2026-10-30T19:00:00",
          "partySize": 4
        }
        """.formatted(restaurantId);
    }

    @Test
    void getBooking_returns200() {
        given()
            .spec(requestSpecGuest)
        .when()
            .get("/bookings/{id}", bookingId)
        .then()
            .spec(RestAssuredSpecs.ok());
    }

    @Test
    void createBooking_returns201() {
        given()
            .spec(requestSpecGuest)
            .body(bookingJson())
        .when()
            .post("/bookings")
        .then()
            .spec(RestAssuredSpecs.created());
    }

    @Test
    void getUnknownBooking_returns404() {
        given()
            .spec(requestSpecGuest)
        .when()
            .get("/bookings/999")
        .then()
            .spec(RestAssuredSpecs.notFound());
    }

    // Mitt API anropar en extern aviseringstjänst när en bokning bekräftas. Den tjänsten finns inte i verkligheten.
    @Test
    void confirmBooking_notificationSentOnceAndConfirmationSentIsTrue() {

        // Arrange - Låtsas att den externa aviseringstjänsten lyckas.
        when(notificationClient.sendConfirmation(bookingId))
                .thenReturn(true);

        // Act - Skicka ett riktigt HTTP-anrop till vårt API.
        given()
            .spec(requestSpecStaff)
        .when()
            .post("bookings/{id}/confirm", bookingId)
        .then()
            .spec(RestAssuredSpecs.ok());

        // Assert - controllera att aviseringstjänsten anropades 1 gång och att isConfirmationSent uppdateras till true
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();
        verify(notificationClient, times(1))
                .sendConfirmation(bookingId);
        assertThat(booking.isConfirmationSent()).isTrue();
    }

    @Test
    void seatBookingInStatusRequested_returns409() {
        given()
            .spec(requestSpecStaff)
        .when()
            .post("bookings/{id}/seat", bookingId)
        .then()
            .log().status()
            .spec(RestAssuredSpecs.illegalTransition());
    }

    /*
    *1.
     Nedan är field injection dvs Spring sätter fältets värde efter att testklassens objekt skapats. Vanligt i testklasser.
     Spring injectar ett riktigt object, en Bean (injectade objektet måste vara annoterad som @Component)
     Konstruktor injection används för vanliga klasser och innebär att fältets värde sätts samtidigt som objektet skapas.
     @Autwored gör samma sak som TestDataSeeder testDataSeeder = new TestDataSeeder();
     Fältet kan inte vara final eftersom det sätts EFTER att objektet skapats
     */
}
