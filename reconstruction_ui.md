# LibrePM — ricostruzione UI e qualità operativa

Data: 2026-10-16. Stato: modifiche e verifiche locali eseguite; certificazione integrale aperta.

## Obiettivo e criteri di accettazione

Portare il desktop a un'identità coerente con il logo, con superfici sobrie, gerarchie leggibili, tema chiaro/scuro, navigazione completa e recupero dagli errori. Rendere verificabile ogni dichiarazione di qualità. Nessuna certificazione generale è implicita in una build riuscita.

## Piano di esecuzione

1. Rilevare architettura, pagine, API, database e baseline dei test.
2. Correggere indisponibilità Gantt e navigazione su installazioni nuove; isolare errori di pagina.
3. Applicare design system condiviso: palette dal logo, tipografia di sistema offline, bordi, spaziatura, focus tastiera, movimento ridotto, responsive.
4. Completare messaggistica persistente di progetto e copertura permessi; definire distribuzione MariaDB con connessione remota al backend, senza credenziali DB nel renderer.
5. Verificare build distribuita con browser reale, flussi CRUD, navigazione e messaggi; eseguire suite Java, migrazione MariaDB reale e packaging.
6. Aggiornare PRD, piano, changelog e rapporto con comandi, risultati, limiti e gate di rilascio.

## Direzione di prodotto

La struttura esistente React/Electron e Spring Boot resta la base. Le pagine non devono sparire per un profilo operativo: il profilo orienta il lavoro senza nascondere le funzioni. Errori locali devono mostrare recupero, senza schermata bianca globale. Stati vuoti e richieste in errore devono essere distinguibili.

Il backend è il solo punto di accesso a MariaDB. Multiutenza remota richiede identità autenticata, autorizzazione sul progetto, TLS, backup e test di isolamento. Il profilo locale senza autenticazione non costituisce distribuzione sicura su Internet.

## Riferimenti tecnici

- WCAG 2.2: https://www.w3.org/TR/WCAG22/ — obiettivo AA, da verificare con audit dedicato; non dichiarazione di conformità.
- MariaDB Connector/J TLS: https://mariadb.com/docs/connectors/mariadb-connector-j/using-tls-ssl-with-mariadb-java-connector — verifica certificato e hostname con `sslMode=verify-full`.

## Evidenze

Risultati finali e copertura documentati in `QUALITY_REPORT.md`. Installazioni Windows/macOS e rete remota richiedono esecuzione sui rispettivi ambienti: i test Linux non certificano quei sistemi.

## Difetti emersi e correzioni

- Gantt/Risorse nascosti dai moduli del profilo personale: navigazione resa completa.
- DOM Gantt spostato dalla libreria fuori dal controllo React: host dedicato e cleanup.
- Etichette SVG e popup Gantt interpretavano markup utente: escaping prima della libreria e nel popup.
- Preferenza backend condivisa sovrascriveva scelta locale di autologin: precedenza client.
- Soft-delete di Task, Note, Project con `@Version` ma SQL con un solo placeholder: aggiunto confronto e incremento versione.
- DELETE progetto privo di mapping REST: endpoint aggiunto con autorizzazione owner esistente.
- Handler generale trasformava permessi e errori HTTP in 500: mantenuti 400/403/405 e stati espliciti.
- Percorso dati configurato ignorato dal main Java: rispettati environment e system property.
- Identità OAuth2 scavalcata da header o URL: principal prioritario e rifiuto impersonazione; guardia progetto per richieste autenticate.
- Pipeline costruiva backend con `-x test`: rimossa esclusione e aggiunto gate browser prima degli installatori.
- Logo SVG da oltre 8 MB nel renderer: usato asset PNG esistente da circa 540 kB.

## Esito esecuzione

23 test Java e 9 E2E superati, 25 pagine raggiungibili, asset packaging coerenti. Report dettagliato: `QUALITY_REPORT.md`; riepilogo strutturato: `docs/quality/2026-10-16-results.json`. Restano 31 avvisi lint, test delle operazioni non coperte e validazione su altri sistemi/IdP reale. Non dichiarata certificazione completa.
