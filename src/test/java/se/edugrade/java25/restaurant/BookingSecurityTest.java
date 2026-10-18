package se.edugrade.java25.restaurant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.edugrade.java25.restaurant.assembler.BookingModelAssembler;
import se.edugrade.java25.restaurant.controller.BookingController;
import se.edugrade.java25.restaurant.security.SecurityConfig;
import se.edugrade.java25.restaurant.service.BookingService;
import se.edugrade.java25.restaurant.service.IdempotencyGuard;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class) // registrerar klassen i Spring-kontexten, så att @Autowired fungerar.
public class BookingSecurityTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BookingService bookingService;
    @MockitoBean private JwtDecoder jwtDecoder;
    @MockitoBean private IdempotencyGuard idempotency;
    @MockitoBean private BookingModelAssembler assembler;

    @Test
    void guestCanReadBookings() throws Exception {
        when(bookingService.findAll(PageRequest.of(0, 10)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        mockMvc.perform(get("/bookings")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void guestCannotConfirmBooking() throws Exception {
        mockMvc.perform(post("/bookings/1/confirm")
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_GUEST")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void staffCanConfirmBooking() throws Exception {
        mockMvc.perform(post("/bookings/1/confirm")
                        .with(jwt().authorities(      // jwt() placerar en färdig JWT-autentisering i testrequestens säkerhetskontext
                                new SimpleGrantedAuthority("ROLE_STAFF")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserCannotReadBookings() throws Exception {
        mockMvc.perform(post("/bookings")
                                                                    // ingen jwt för token ska inte skickas
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
