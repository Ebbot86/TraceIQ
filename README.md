# TraceIQ – Android 1.4.0
Svensk prototyp för publik IP-information. Native Android (Java, min Android 8). Ingen platsbehörighet, konto eller permanent sökhistorik. Kräver internet.

## Bygg en test-APK
Öppna Actions → Build TraceIQ APK. Byggflödet körs vid push till main eller manuellt via Run workflow.
Öppna den lyckade körningen, ladda ned artefakten TraceIQ-1.4.0-debug-APK och packa upp app-debug.apk. Installera endast om du litar på källan.

Alternativ: Android Studio med SDK 35, JDK 17 och Gradle 8.9. Debugsignering är enbart för tester; permanent release-signering och uppdateringar är inte konfigurerade. En ny GitHub-run kan få en annan debugnyckel och därmed kräva avinstallation av tidigare APK.

## Datakällor
- https://ipwhois.io/documentation (ipwho.is, IP-geografi och nätverksdata)
- https://stat.ripe.net/docs/data-api/ripestat-data-api (routingprefix)
- https://developers.google.com/speed/public-dns/docs/doh/json (PTR)

Appen skickar IP-sökningar direkt till externa leverantörer, som även kan se klientens publika IP. Inga egna loggar eller lagring. Geografi är en grov uppskattning. VPN/hotklassificering, WHOIS, AI, PDF och permanent historik är inte implementerade. Gratisplaners villkor och begränsningar måste verifieras före kommersiell distribution.

## IP-karta
OpenStreetMap visas direkt i analysen med koordinater avrundade till en decimal. Kartan visar en uppskattad punkt, inte en exakt adress eller en verifierad felradie. Vanlig karta finns i appen; satellitvy öppnas i Google Maps. Kartleverantörer får avrundade koordinater och klientens IP. Ingen GPS-behörighet. Kartans funktion och satellitöppning behöver provas på fysisk Android.

## Dashboard 1.4.0
Fem flikar: Hem, Spåra, Verktyg, Historik och Mer. Snabbval öppnar befintlig IP-analys. Historik är max 20 lyckade sökningar i minnet och kan rensas; den sparas inte på disk. Menyn Mer länkar till GitHub Releases. VPN, hotinformation, ping och andra kommande verktyg är markerade som planerade, inte färdiga. UI och navigation behöver testas på fysisk Android.

## Cyber dashboard
Offline vector illustration inspired by the approved mockup; decorative pins do not represent actual IP locations. Search, tools, live lookup results and the geographic map share one scrollable mobile dashboard. Planned tools show explicit information rather than fabricated results. On-device visual verification remains required.

## Security Intelligence
Tor Project exit list (IPv4, cached in memory for one hour); ipapi.is detection fields and AbuseIPDB 90-day reports with optional session-only personal API keys. Unknown or absent fields are never converted to false or zero. Sources: https://ipapi.is/developers.html and https://docs.abuseipdb.com/ and https://check.torproject.org/torbulkexitlist . No reports are submitted. Provider scores are not an overall safety verdict.

## Persistent signing
Optional release signing is prepared. Repository owner must configure GitHub Actions secrets TRACEIQ_KEYSTORE_B64 (base64 of private release keystore), TRACEIQ_STORE_PASSWORD, TRACEIQ_KEY_ALIAS and TRACEIQ_KEY_PASSWORD. Keep an offline backup. Without all four secrets the build is either debug (all absent) or fails (partial configuration); it never embeds the secrets in the APK. Switching from debug to release requires a one-time uninstall. In-app automatic installation remains unimplemented. Release signing path requires a real key to verify.
