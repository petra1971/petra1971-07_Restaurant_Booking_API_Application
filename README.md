# Inlämningsuppgif

### Att köra projektet lokalt eller på Railway

Lokalt använder man en keycloak autentiserings-server i docker. 
För att starta en KeyCLoak-server gör man klokast att be AI om hjälp. Jag bifogar även en instruktion som KAN vara till
hjälp.

Lokalt har jag ett separat projekt för detta i C:\git\07_webservices_KeyCloak_Oauth2_OIDC 
och där finns även docker-compose med admin-konto/password för att logga in i keycloak lokalt

För att starta keycloak-server:
1. starta docker
2. I tearminal cd C:\git\07_webservices_KeyCloak_Oauth2_OIDC 
3. starta containern: docker compose up -d

## Kör applikationen lokalt

```bash
mvn spring-boot:run
```

I ett annat fönster:

```bash
curl localhost:8080/health
```

Du ska få tillbaka `{"status":"UP","service":"restaurant-api"}`. Om du får det fungerar din miljö, och du kan börja med del 1.

Kör testerna med:

```bash
mvn test
```

Nu kan man logga in i keycloak med admin-kontot (just nu admin/admin)
Adressen är: localhost:8181

När man kör lokalt ska host i filen "requests.http" vara @host = http://localhost:8080

När man kör med både RestaurantAPI-servern och Keycloak-servern på Railway
Ska host i vara #@host = https://petra1971-restaurant-frontend.up.railway.app

I Insomnia: 
authorization url http://localhost:8180/realms/restaurant-api/protocol/openid-connect/auth
token url: http://localhost:8180/realms/restaurant-api/protocol/openid-connect/token
redirect url: https://app.insomnia.rest/oauth/redirect

User och passwords när man går mot restaurant-api:t:
guest/guest
staff/staff
admin/admin
Roller: GUEST, STAFF, ADMIN

guest har access att boka och hämta bokningar
staff kan hämta bokningar och ändra status på bokningar
admin kan skapa restaurang
alla kan hämta restauranger

I Insomnia:
authorization url: https://keycloak-production-75ba.up.railway.app/realms/restaurant-api/protocol/openid-connect/auth
token url: https://keycloak-production-75ba.up.railway.app/realms/restaurant-api/protocol/openid-connect/token
redirect url: https://app.insomnia.rest/oauth/redirect

User och passwords när man går mot restaurant-api:t:
guest/guest - OBS Kontrollera! Går ändra i Keycloak inloggad som admin (admin/admin)
staff/staff - se ovan
admin/admin - se ovan
Roller: GUEST, STAFF, ADMIN

Projektet är nu uppsatt så att frontend kan köras lokalt eller i railway
Backend och keycloak samt dess db körs i Railway.

Om jag byter till lokal backend måste min lokala Keycloak-konfiguration (i docker-container), 
JWT-validering och CORS-inställningar fungera tillsammans med den.
I .env.development ändrar jag då till VITE_API_URL=http://localhost:8080. 
Då skickas API-anropen till min lokala Spring Boot-applikation.
I frontend måste configs för backend-url:er också ses över,

## Lars introduktion till uppgiften

1. **[requirements.md](requirements.md)** är uppgiften. Den innehåller fyra delar, kraven för G och VG samt datum.
2. **[skriftliga_fragor.md](skriftliga_fragor.md)** innehåller de fem frågorna i del 4.

Deadline är onsdag den 14 oktober 2026 kl. 08.00. Muntlig redovisning sker onsdagen den 14 oktober 2026.

## Tekniker

```text
pom.xml                          Spring Boot 4.1.0, Java 25

```

`HealthController` är inte en del av uppgiften. Behåll den för en snabb kontroll efter en deploy, eller radera den när du har egna endpoints.

## Kom igång

```bash
mvn spring-boot:run
```

I ett annat fönster:

```bash
curl localhost:8080/health
```

Du ska få tillbaka `{"status":"UP","service":"restaurant-api"}`. Om du får det fungerar din miljö, och du kan börja med del 1.

Kör testerna med:

```bash
mvn test
```

## requests.http

I `requests.http` finns anropen mot API:et. Öppna filen i IntelliJ och klicka på den gröna pilen till vänster om ett anrop, eller installera tillägget REST Client i VS Code.

