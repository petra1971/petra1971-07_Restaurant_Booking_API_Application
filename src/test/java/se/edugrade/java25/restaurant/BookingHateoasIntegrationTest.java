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
import se.edugrade.java25.restaurant.support.TestDataSeeder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // För att TestDataSeeder säkert ska användas och inte SampleData
@Import(TestDataSeeder.class) // registrerar klassen i Spring-kontexten, så att @Autowired fungerar.
public class BookingHateoasIntegrationTest {

    @LocalServerPort
    private int port;

    private RequestSpecification requestSpecGuest, requestSpecStaff;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setup() {
        testDataSeeder.reset();

        // Förbereder mockade JWT för de roller som testerna använder.
        RestAssuredSpecs.mockJwt(jwtDecoder, "GUEST");
        RestAssuredSpecs.mockJwt(jwtDecoder, "STAFF");

        // Skapar requestSpecificationer för de olika rollerna
        requestSpecGuest = RestAssuredSpecs.request(port, "GUEST");
        requestSpecStaff = RestAssuredSpecs.request(port, "STAFF");
    }

    @Test
    void getBooking_returnHateoasLinks() {

        given()
            .spec(requestSpecGuest)
        .when()
            .get("/bookings/1")
        .then()
            .log().body()
            .statusCode(200)
            .body("_links.self.href", notNullValue())
            .body("_links.collection.href", notNullValue())
            .body("_links['bookings:confirm'].href", notNullValue())
            .body("_links['bookings:cancel'].href", notNullValue());
    }

    @Test
    void getBooking_returnCorrectHateoasLinks() {
        given()
            .spec(requestSpecGuest)
            .accept("application/hal+json")
        .when()
            .get("/bookings/1")
        .then()
            .spec(RestAssuredSpecs.ok())

            // Kontrollera att self-länken pekar på bokningen.
            .body("_links.self.href",
                    equalTo("http://localhost:" + port + "/bookings/1"))

            // Kontrollera att collection-länken pekar på listan.
            .body("_links.collection.href",
                    equalTo("http://localhost:" + port + "/bookings?page=0&size=5"))

            // Kontrollera att confirm-länken finns.
            .body("_links['bookings:confirm'].href",
                    equalTo("http://localhost:" + port + "/bookings/1/confirm"))

            // Kontrollera att confirm-länken finns.
            .body("_links['bookings:cancel'].href",
                    equalTo("http://localhost:" + port + "/bookings/1/cancel"));
    }

    @Test
    void confirmBooking_returnsCorrectHateoasLinks() {

        given()
            .spec(requestSpecStaff)
            .accept("application/hal+json")
        .when()
            .post("/bookings/1/confirm")
        .then()
            .spec(RestAssuredSpecs.ok())

            // Kontrollera att self-länken pekar på bokningen.
            .statusCode(200)
            .body("status", equalTo("CONFIRMED"))
            .body("_links.self.href", notNullValue())
            .body("_links.collection.href", notNullValue())
            .body("_links['bookings:seat'].href", notNullValue())
            .body("_links['bookings:cancel'].href", notNullValue())
            .body("_links['bookings:confirm'].href", nullValue());
    }
}
