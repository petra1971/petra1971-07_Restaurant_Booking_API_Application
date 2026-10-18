package se.edugrade.java25.restaurant;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/*
Del	Ansvar
request()	                                    Skapar HTTP-request med Bearer-token
withoutToken()	                                Skapar request utan token för 401-test
ok(), created(), unauthorized(), forbidden()	Kontrollerar HTTP-status
mockJwt()	                                    Lär Mockito vad JwtDecoder ska returnera
tokenFor()	                                    Ser till att samma test-token används i request och mock
Viktigt: mockJwt() skapar inte en riktig signerad JWT. Den konfigurerar Mockito att
returnera ett färdigt Jwt-objekt. Det är samma princip som i ditt befintliga test.
**/

public final class RestAssuredSpecs {

    private RestAssuredSpecs() {
        // Hjälpklass som inte ska instansieras.
    }

    // ============================================
    // REQUEST SPECIFICATION
    // ============================================

    public static RequestSpecification request(int port, String role) {

        return new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + tokenFor(role))
                .build();
    }

    // Request utan JWT – används för att testa 401.
    public static RequestSpecification withoutToken(int port) {

        return new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .setAccept(ContentType.JSON)
                .build();
    }

    // ============================================
    // RESPONSE SPECIFICATION
    // ============================================

    public static ResponseSpecification ok() {

        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }

    public static ResponseSpecification created() {

        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .build();
    }

    public static ResponseSpecification unauthorized() {

        return new ResponseSpecBuilder()
                .expectStatusCode(401)
                .build();
    }

    public static ResponseSpecification forbidden() {

        return new ResponseSpecBuilder()
                .expectStatusCode(403)
                .build();
    }

    public static ResponseSpecification illegalTransition() {

        return new ResponseSpecBuilder()
                .expectStatusCode(409)
                .build();
    }

    public static ResponseSpecification notFound() {

        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .build();
    }

    // ============================================
    // SPRING SECURITY – TEST JWT
    // ============================================

    public static void mockJwt(JwtDecoder jwtDecoder, String role) {

        String token = tokenFor(role);

        // Skapa ett Jwt-objekt med samma rollstruktur
        // som Keycloak använder.
        Jwt jwt = Jwt
                .withTokenValue(token)
                .header("alg", "RS256")
                .subject("test-user")
                .claim("realm_access",
                        Map.of("roles", List.of(role)))
                .build();

        // När Spring Security avkodar test-token
        // returnerar Mockito vårt Jwt-objekt.
        when(jwtDecoder.decode(token))
                .thenReturn(jwt);
    }

    private static String tokenFor(String role) {
        return "test-token-" + role;
    }
}
