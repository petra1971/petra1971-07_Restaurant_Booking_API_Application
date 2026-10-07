## KeycloakJwtAuthenticationConverter tar dessa:
* GUEST
* STAFF

och omvandlar dem till Spring Security authorities:

* ROLE_GUEST
* ROLE_STAFF

Det är därför du kan skriva:
* .hasRole("GUEST")

Spring Security letar då efter:
* ROLE_GUEST

Flödet blir

``` text
Keycloak
    │
    │ JWT
    │
    ▼
JwtDecoder
    │
    │ "Är JWT:n giltig?"
    ▼
KeycloakJwtAuthenticationConverter
    │
    │ "Vilka roller finns i JWT:n?"
    ▼
ROLE_GUEST
ROLE_STAFF
ROLE_ADMIN
    │
    ▼
SecurityConfig
    │
    ├── hasRole("GUEST")
    ├── hasRole("STAFF")
    └── hasRole("ADMIN")
´´´