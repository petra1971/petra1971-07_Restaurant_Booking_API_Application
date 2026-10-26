package se.edugrade.java25.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.http.MediaType;
import se.edugrade.java25.restaurant.repository.IdempotencyRecordRepository;
import se.edugrade.java25.restaurant.support.TestDataSeeder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Test med MockMvc o JUnit 5 - Här händer:
- @SpringBootTest – startar Spring-applikationens kontext.
- @AutoConfigureMockMvc – konfigurerar MockMvc UTAN att starta en riktig HTTP-server.
- @MockitoBean JwtDecoder – Spring får en mockad JwtDecoder att använda vid konfigureringen vid uppstart

jwt() är ett sätt att simulera JWT-inloggning i MockMvc-tester. Man slipper då Keycloak och Insomnia,
och kan enkelt kontrollera att rätt roller får 200 och fel roller får 403.
*/
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // För att TestDataSeeder säkert ska användas och inte SampleData
@Import(TestDataSeeder.class) // registrerar klassen i Spring-kontexten, så att @Autowired fungerar.
class BookingIdempotencyIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private IdempotencyRecordRepository idempotencyRecordRepository;
    @Autowired private TestDataSeeder testDataSeeder;
    @MockitoBean private JwtDecoder jwtDecoder;

    private Long bookingId;
    private Long restaurantId;

    @BeforeEach
    void setUp() {
        TestDataSeeder.TestIds testIds = testDataSeeder.reset();  //Tömmer databasen först och fyller sen med fräsch testdata
        restaurantId = testIds.restaurantId();

        idempotencyRecordRepository.deleteAll();
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
    void sameIdempotencyKeyShouldReturnSameResponse() throws Exception {

        String key = UUID.randomUUID().toString();

        // Första POST-anropet
        MvcResult first = mockMvc.perform(post("/bookings")
                        .with(jwt().authorities(      // jwt() placerar en färdig JWT-autentisering i testrequestens säkerhetskontext
                                new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/hal+json")
                        .header("Idempotency-Key", key)
                        .content(bookingJson()))
                .andExpect(status().isCreated())
                .andReturn();

        // Andra POST-anropet med samma nyckel och body
        MvcResult second = mockMvc.perform(post("/bookings")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/hal+json")
                        .header("Idempotency-Key", key)
                        .content(bookingJson()))
                .andExpect(status().isCreated())
                .andReturn();

        // Samma svar ska returneras
        assertEquals(
                first.getResponse().getContentAsString(),
                second.getResponse().getContentAsString()
        );

    }
}