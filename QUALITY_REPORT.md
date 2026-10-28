# LibrePM — rapporto qualità del 2026-10-16

## Esito e significato delle verifiche

Le verifiche elencate sotto sono superate sulla revisione locale modificata. Non costituiscono certificazione integrale del prodotto, conformità normativa, audit WCAG o garanzia di assenza di bug. Il rilascio generale resta subordinato ai gate aperti.

Ambiente: Linux, Java 21, Node 20, Chromium, Electron 32, backend Spring Boot reale con database SQLite isolato; test MariaDB eseguiti con server reale gestito da MariaDB4j. Nessuna API simulata nei test browser. Il test Electron usa renderer compilato, protocollo `file://` e preload applicativo reale; il suo main è una fixture con IPC di porta/focus. Non verifica l'installatore o l'intero main di produzione.

| Verifica | Risultato | Evidenza |
| --- | --- | --- |
| `./gradlew test bootJar --console=plain` | 23 test, 0 errori, 0 fallimenti, 0 saltati | `build/reports/tests/test/index.html`, XML JUnit |
| `npm run build` in `desktop` | Superata | Asset Vite, percorsi relativi e chunk locali |
| `CHROMIUM_PATH=/usr/bin/chromium xvfb-run -a npm run test:e2e` | 9 test, 0 fallimenti, 0 saltati | `desktop/test-results/e2e-results.json`, screenshot |
| `npm test` in `desktop` | 6 superati, 1 saltato | Test infrastruttura release e configurazione |
| `node scripts/prepare-self-contained.cjs` | Superata | JRE e backend aggiornati negli asset di packaging |
| `npm run verify:packaging` con `USE_SYSTEM_FPM=true` | Superata | Verifica coerenza asset e versione, fpm 1.18.0 isolata in `/tmp/librepm-qa-gems` |
| `git diff --check` | Superata | Nessun errore whitespace |
| `npm run lint` | Gate aperto: 0 errori, 31 avvisi; exit non zero per `--max-warnings 0` | Hook dependencies e React Fast Refresh nel codice esistente |

Il test Debian che richiede fakeroot è saltato dalla condizione ambientale della suite: non è certificata la finalizzazione del pacchetto `.deb` in questa sessione. La verifica packaging senza fpm di sistema inizialmente falliva correttamente su RPM 4.20; con toolchain isolata appropriata è superata. Nessun installatore è stato pubblicato o installato su altre macchine.

## Matrice di copertura

| Area | Prova superata | Limite |
| --- | --- | --- |
| Navigazione | Tutte le 25 pagine, nessun crash renderer o risposta API >=400 durante visita | Smoke di apertura, non ogni azione CRUD |
| Gantt | Barre, Day/Week/Month, rivisita, singolo SVG; Electron `file://` con preload reale | Non prova tutti gli algoritmi di scheduling o tutti gli OS |
| Sicurezza Gantt | Markup nei titoli mostrato come testo, senza nodi SVG iniettati | Non audit XSS dell'intera app |
| Task | Creazione da Planner UI, aggiornamento API visibile in Kanban, DELETE e GET 404 | Allegati, dipendenze, timer e tutte le varianti non coperti integralmente |
| Note e progetti | Soft-delete e lookup successivo verificati tramite REST | CRUD UI completo non coperto |
| Team | Aggiunta membro, ruoli, duplicati, ghost, rimozione e protezione owner | Non collaborazione distribuita sotto carico |
| Messaggi | UI → API → persistenza → ricaricamento; testo HTML innocuo; ruoli e input invalidi | Non WebSocket, allegati o ricevute |
| MariaDB | Migrazione schema/dati, rifiuto target popolato, upgrade vendor, repository messaggi e import amministrativo prima di Flyway/JPA | Non test di rete TLS/IdP reale o ripristino backup |
| Identità | Sessione OAuth2 di test non può impersonare URL/header di altro utente | Provider reale, onboarding remoto e audit di ogni risorsa ancora necessari |
| Diagnostica/report | Download ZIP da Impostazioni, report restituisce PDF con firma `%PDF` | Non analisi del contenuto completo o privacy audit del bundle |
| OKR | Campo risultato modificabile e valore 42 persistito dopo blur | Non ogni operazione OKR |
| UI | Controlli visibili a 1024×768, temi chiaro/scuro, screenshot | Non audit completo di contrasto/reflow o screen reader |
| Password | Suite BCrypt e gestione utenti superata | Non penetration test |

Pagine visitate: Dashboard, Messaggi, Inbox, Charter, Progetti, Galleria, Team, Risorse, Contatti, Calendario, Planner, Gantt, Kanban, Note, Time Tracking, Finance, Grants, Stakeholder, Change Control, Portfolio, Guide, Analytics, Integrazioni, Sync Conflicts, Impostazioni.

## Bug corretti

1. Profilo personale nascondeva Gantt e Risorse; Impostazioni escluse dalla navigazione principale.
2. Frappe Gantt spostava SVG gestito da React, lasciando contenitori a ogni ricreazione: host dedicato e cleanup.
3. Etichette Gantt e popup accettavano HTML/SVG utente: escaping prima della libreria e nel popup.
4. Risposte Gantt/messaggi di richieste precedenti potevano sovrascrivere il progetto attuale: controllo richiesta e progetto.
5. Errore pagina poteva interrompere l'intera UI: boundary per pagina con recupero.
6. Autologin preferiva ultimo utente globale a quello scelto sul client: priorità locale.
7. Soft-delete Task/Note/Project non prevedeva parametro versione Hibernate: SQL corretto, incremento versione e controllo righe.
8. Mancava mapping REST DELETE progetto, pur essendo presente nel client: aggiunto riusando autorizzazione owner.
9. Eccezioni permessi/HTTP/richiesta trasformate in 500: stati 400/403/405 e ResponseStatusException preservati.
10. Percorso dati configurato ignorato dal main Java: rispettati environment e system property.
11. Header utente prevaleva sulla sessione OAuth2: principal prioritario e guardia URL/header/progetto.
12. Import MariaDB all'avvio avveniva dopo inizializzazione JPA/Flyway: spostato nella strategia preliminare Flyway e verificato su server reale.
13. Backlink note task usava `shell` inesistente: callback corretta e collegata al Gantt.
14. Client diagnostica e PDF usava `currentUserId` inesistente: gestione comune API e download blob con sessione/CSRF.
15. Input risultato OKR aveva `value` e `onChange` vuoto: editing e salvataggio verificati.
16. CI saltava i test backend: esclusione rimossa, E2E richiesti prima del packaging.
17. Asset logo SVG da oltre 8 MB sostituito nel renderer con PNG esistente da circa 540 kB.
18. Configurazione lint non distingueva runtime Node e variabili Vite: riconosciuti correttamente; rimossi riferimenti e definizioni inutilizzati.

Bundle principale Vite: circa 1.124 kB prima della suddivisione delle pagine, circa 517 kB dopo. Misura dell'asset principale, non di tutto il trasferimento iniziale. Editor Markdown conserva chunk di circa 1.047 kB e warning Sass provengono da frappe-gantt.

## Gate ancora aperti

- 31 avvisi lint: correggere dipendenze hook e organizzazione export dei context, verificando assenza di cicli di ricaricamento. Non abbassata soglia lint per nasconderli.
- Copertura E2E di ogni operazione nei moduli Finance, Grants, Stakeholder, Portfolio, calendario, integrazioni, sync, allegati e timer. Apertura pagina non dimostra correttezza di tutte le funzioni.
- Installazione/aggiornamento reale su Windows e macOS e su seconda macchina Linux; finalizzazione `.deb` e test main Electron completo.
- Test con IdP reale, onboarding remoto, revisione autorizzazioni di tutte le risorse, TLS/CORS/CSRF, backup e ripristino, concorrenza e carico.
- Audit WCAG 2.2 AA completo, zoom/reflow e screen reader.
- Verifica ciclo di supporto e aggiornamento dipendenze desktop/backend prima di dichiarazioni “all'avanguardia”.

## Riproduzione

```bash
./gradlew test bootJar --console=plain
# In un terminale dedicato, senza usare il database personale:
LIBREPM_DATA_PATH=/tmp/librepm-qa-reproduction java -jar build/libs/LibrePM-0.1.7.jar --server.port=8080 --server.address=127.0.0.1
# In un altro terminale:
cd desktop
npm ci
npm run build
npx playwright install --with-deps chromium
xvfb-run -a npm run test:e2e
npm test
npm run lint
```

Per Chromium di sistema impostare `CHROMIUM_PATH=/usr/bin/chromium`. Non ricostruire il jar mentre lo stesso file è aperto dal processo Java di test; usare una copia per le esecuzioni parallele. I test creano utenti/progetti: usare esclusivamente dati isolati. La CI conserva report backend e prove browser come artefatti.
