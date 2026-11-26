# TraceIQ – Android 1.0.0
Svensk prototyp för publik IP-information. Native Android (Java, min Android 8). Ingen platsbehörighet, konto eller sökhistorik. Kräver internet.

## Bygg en test-APK
Öppna Actions → Build TraceIQ APK. Byggflödet körs vid push till main eller manuellt via Run workflow.
Öppna den lyckade körningen, ladda ned artefakten TraceIQ-1.0.0-debug-APK och packa upp app-debug.apk. Installera endast om du litar på källan.

Alternativ: Android Studio med SDK 35, JDK 17 och Gradle 8.9. Debugsignering är enbart för tester; permanent release-signering och uppdateringar är inte konfigurerade. En ny GitHub-run kan få en annan debugnyckel och därmed kräva avinstallation av tidigare APK.

## Datakällor
- https://ipwhois.io/documentation (ipwho.is, IP-geografi och nätverksdata)
- https://stat.ripe.net/docs/data-api/ripestat-data-api (routingprefix)
- https://developers.google.com/speed/public-dns/docs/doh/json (PTR)

Appen skickar IP-sökningar direkt till externa leverantörer, som även kan se klientens publika IP. Inga egna loggar eller lagring. Geografi är en grov uppskattning. VPN/hotklassificering, karta, WHOIS, AI, PDF och historik är inte implementerade. Gratisplaners villkor och begränsningar måste verifieras före kommersiell distribution.
