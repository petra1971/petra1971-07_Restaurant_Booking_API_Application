1. SLå ihop Idempotency-klasserna
2. Lägg till användning av ETag i controller-klasserna.
3. Titta på Comment-controllern och se om något ska tas med.
4. Lägg till Hateoas
5. API:er för statusförändringar
6. CORS (lägg på rätt ställe, före filtret) och CSRF
6. Token server för säkerhet
7. Lägg till update för if-match
8. Lägg till sort and filter i Bookings

9.Ett okänt id ger 404 som ProblemDetail. En otillåten tillståndsövergång ger 409.
10. OpenAPI via springdoc. Swagger UI ska svara.
11. test kap 7-9
- RestAssured mot en slumpmässig port. Kap 7. Minst tre fall: 
  * skapa en bokning (201), 
  * hämta den (200) och 
  * hämta en som inte finns (404).
  - Ditt API anropar en extern aviseringstjänst när en bokning bekräftas. Den tjänsten finns inte i verkligheten. 
        Definiera ett interface för den och mocka den i testerna. Du väljer mellan Mockito och WireMock och motiverar valet i del 4. Kap 8.
  - Minst ett test som visar att en otillåten tillståndsövergång ger 409.
- Tester av minst två säkrade endpoints med SecurityMockMvcRequestPostProcessors.jwt(). En tillåten och en nekad. Kap 16
- Flytta HATEOAS-testerna till en MockMvc-integrationstestklass

Alla dessa kan kallas integrationstester, eftersom du testar hur flera delar av ditt Restaurant API fungerar tillsammans via HTTP.
Men du kan dela in dem efter vad de verifierar:
Testområde	Benämning	Vad testas?
HATEOAS-länkar	HATEOAS-tester	Att rätt länkar finns utifrån bokningens status
ETag och 304	HTTP-cachetester eller Conditional Request Tests	Att ETag och If-None-Match fungerar
Idempotency	Idempotens-tester	Att upprepade POST-anrop med samma nyckel inte skapar dubbletter
Spring Security/JWT	Säkerhets- och auktoriseringstester	Att autentisering och roller ger rätt 401/403/200
Förslag på klassnamn
Om du vill dela upp din nuvarande BookingApiIntegrationTest i separata testklasser:
src/test/java/
└── se/edugrade/java25/restaurant/
├── BookingHateoasIntegrationTest.java
├── BookingETagIntegrationTest.java
├── BookingIdempotencyIntegrationTest.java
├── BookingSecurityIntegrationTest.java
└── RestAssuredSpecs.java

12. Tjänsten ska vara driftsatt och åtkomlig via internet vid redovisningen.
13. Bygg frontend - Det finns två skäl att ändå göra det. CORS blir konkret först när en riktig webbsida
    på en annan origin anropar ditt API. Dessutom får du något annat än Swagger UI att visa under redovisningen.
    
14. Fem frågor, totalt ungefär två sidor. De finns i en separat fil:   skriftliga_fragor.md
     Frågorna täcker de kursmål som handlar om att känna till något snarare än att bygga det: SOAP, WSDL, Spring Cloud, 
     driftsättningsalternativ och end-to-end-perspektivet.   
    Två av frågorna bygger på kapitel som behandlas under vecka 40 och 41. Du kan alltså inte bli klar med del 4 
    förrän i slutet av kursen, och det är avsiktligt.

15. Lägg till put-metod för att få med IfMatch()