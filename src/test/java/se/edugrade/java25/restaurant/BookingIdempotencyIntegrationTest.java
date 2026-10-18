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

    @BeforeEach
    void setUp() {
        testDataSeeder.reset();
        idempotencyRecordRepository.deleteAll();
    }

    @Test
    void sameIdempotencyKeyShouldReturnSameResponse() throws Exception {

        String requestBody = """
            {
                "restaurantId": 1,
                "guestName": "Anna",
                "guestEmail": "anna@example.com",
                "arrivalTime": "2026-12-20T18:00:00",
                "partySize": 2
            }
            """;

        // Första POST-anropet
        MvcResult first = mockMvc.perform(post("/bookings")
                        .with(jwt().authorities(      // jwt() placerar en färdig JWT-autentisering i testrequestens säkerhetskontext
                                new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "test-key-123")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        // Andra POST-anropet med samma nyckel och body
        MvcResult second = mockMvc.perform(post("/bookings")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "test-key-123")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        // Samma svar ska returneras
        assertEquals(
                first.getResponse().getContentAsString(),
                second.getResponse().getContentAsString()
        );

    }
}