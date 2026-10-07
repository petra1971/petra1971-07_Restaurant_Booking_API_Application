# Authorization Server och Keycloak

Om du ersätter din egen **Authorization Server** med **Keycloak**, är
det framför allt klasserna i `authserver`-paketet som inte längre
behövs.

## 1. Klasser som ersätts av Keycloak

Hela detta paket:

``` text
authserver/
├── AccessToken
├── AuthorizationCode
├── AuthorizationCodeStore
├── AuthorizationServerController
├── ClientRegistration
├── ClientRegistry
├── Pkce
├── TokenResponses
└── TokenStore
```

kan i princip tas bort.

| Klass                           | Vad den gör                                        | Med Keycloak |
| ------------------------------- | -------------------------------------------------- | ------------ |
| `AccessToken`                   | Representerar/skapar access token                  | Keycloak     |
| `AuthorizationCode`             | Hanterar Authorization Code i OAuth2-flödet        | Keycloak     |
| `AuthorizationCodeStore`        | Lagrar authorization codes                         | Keycloak     |
| `AuthorizationServerController` | Implementerar din egen Authorization Server-logik  | Keycloak     |
| `ClientRegistration`            | Registrerar OAuth-klienter                         | Keycloak     |
| `ClientRegistry`                | Lagrar/hämtar klientregistreringar                 | Keycloak     |
| `Pkce`                          | Hanterar PKCE (`code_verifier` / `code_challenge`) | Keycloak     |
| `TokenResponses`                | Bygger token-svar                                  | Keycloak     |
| `TokenStore`                    | Lagrar tokens                                      | Keycloak     |


### Slutsats

> **Hela `authserver`-paketet representerar i praktiken funktionalitet
> som Keycloak tar över.**

Du behöver alltså inte bygga och underhålla din egen Authorization
Server längre.

------------------------------------------------------------------------

## 2. Vad händer med `BearerTokenAuthenticationFilter`?

Den här klassen är annorlunda:

``` text
config/
└── BearerTokenAuthenticationFilter
```

Den är **inte en del av Authorization Server**.

Den hör istället till **Resource Server-delen**, alltså ditt Restaurant
API.

Skillnaden är viktig:

``` text
Authorization Server
        │
        │ skapar tokens
        ▼
    Keycloak
        │
        │ access token (JWT)
        ▼
Resource Server
        │
        │ validerar token
        ▼
Restaurant API
```

------------------------------------------------------------------------

## 3. Med Keycloak ser arkitekturen ut så här

``` text
                    ┌──────────────────┐
                    │     Keycloak     │
                    │                  │
                    │ Authorization    │
                    │ Server           │
                    └────────┬─────────┘
                             │
                       access token
                           (JWT)
                             │
                             ▼
┌──────────────┐       ┌─────────────────────┐
│   Frontend   │──────▶│   Restaurant API    │
└──────────────┘  JWT  │   Resource Server   │
                       │                     │
                       │ Spring Security     │
                       │ JWT validation      │
                       │                     │
                       │ Roles / Scopes      │
                       └─────────────────────┘
```

------------------------------------------------------------------------

## 4. Vad Keycloak ansvarar för

När Keycloak används som Authorization Server ansvarar Keycloak bland
annat för:

-   Authorization Code
-   PKCE
-   Client registration
-   Login
-   Authorization
-   Access tokens
-   Refresh tokens
-   Token storage
-   Authorization endpoint
-   Token endpoint

Det är alltså saker som du tidigare behövde implementera själv.

------------------------------------------------------------------------

## 5. Vad Restaurant API fortfarande ansvarar för

Ditt API är fortfarande en **Resource Server**.

Det ska bland annat:

-   ta emot Bearer tokens
-   validera JWT
-   kontrollera roller
-   kontrollera scopes
-   skydda endpoints
-   returnera `401 Unauthorized` när token saknas/är ogiltig
-   returnera `403 Forbidden` när användaren saknar rättighet

Exempel:

``` java
.requestMatchers(HttpMethod.GET, "/bookings/**")
    .hasRole("GUEST")

.requestMatchers(HttpMethod.POST, "/bookings")
    .hasRole("GUEST")

.requestMatchers(HttpMethod.POST, "/bookings/{id}/seat")
    .hasRole("STAFF")
```

Keycloak skapar alltså token med information om användaren och dess
roller. Spring Security i ditt API använder sedan informationen från
JWT:n när den avgör om requesten får gå igenom.

------------------------------------------------------------------------

## 6. Behöver jag `BearerTokenAuthenticationFilter` själv?

Om du använder Spring Securitys Resource Server-stöd:

``` java
.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))
```

behöver du normalt **inte skriva ett eget
`BearerTokenAuthenticationFilter`**.

Spring Security har redan filter och funktionalitet för att:

1.  hitta Bearer token i:

``` http
Authorization: Bearer eyJ...
```

2.  läsa JWT:n

3.  validera JWT:n

4.  skapa en authenticated principal

5.  göra den tillgänglig för Spring Security

6.  kontrollera dina `hasRole()` / `hasAuthority()`-regler

Därför kan även din egen:

``` text
BearerTokenAuthenticationFilter
```

vara överflödig om den bara gör detta.

------------------------------------------------------------------------

## 7. Vad blir kvar i projektet?

Ungefär:

``` text
src/main/java/.../

├── authserver/                    ❌ TA BORT
│   ├── AccessToken
│   ├── AuthorizationCode
│   ├── AuthorizationCodeStore
│   ├── AuthorizationServerController
│   ├── ClientRegistration
│   ├── ClientRegistry
│   ├── Pkce
│   ├── TokenResponses
│   └── TokenStore
│
├── config/
│   ├── ApiVersioningConfig         ✅ BEHÅLL
│   └── BearerTokenAuthenticationFilter
│                                    ❌ sannolikt TA BORT
│
├── security/                       ✅ BEHÅLL/LÄGG HÄR
│   └── SecurityConfig
│
├── controller/
├── service/
├── repository/
├── model/
└── dto/
```

------------------------------------------------------------------------

## 8. Den viktigaste skillnaden

Tänk så här:

### Före Keycloak

``` text
Ditt projekt
│
├── Authorization Server
│   ├── skapar tokens
│   ├── hanterar PKCE
│   ├── hanterar authorization codes
│   ├── registrerar clients
│   └── lagrar tokens
│
└── Resource Server
    └── Restaurant API
```

### Efter Keycloak

``` text
Keycloak
│
└── Authorization Server
    ├── skapar tokens
    ├── hanterar PKCE
    ├── hanterar authorization codes
    ├── registrerar clients
    └── hanterar användare/roller


Ditt projekt
│
└── Resource Server
    └── Restaurant API
        ├── validerar JWT
        ├── kontrollerar roller/scopes
        └── skyddar endpoints
```

## Kort sammanfattning

**`authserver` = din gamla Authorization Server → Keycloak ersätter
den.**

**`BearerTokenAuthenticationFilter` = Resource Server-funktionalitet →
Spring Security kan redan göra detta åt dig.**

**`ApiVersioningConfig` = har inget med OAuth/Keycloak att göra → behåll
den.**

**`SecurityConfig` = behövs fortfarande → här konfigurerar du hur ditt
Restaurant API litar på och använder JWT från Keycloak.**
