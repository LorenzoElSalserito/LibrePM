# MariaDB e multiutenza

Architettura: client React/Electron → backend HTTPS → MariaDB privata. Le credenziali database restano nel processo Java. Non esporre la porta 3306 ai client desktop.

## Provisioning esplicito

1. Creare database MariaDB vuoto e utenza amministrativa temporanea. Configurare CA/certificato e credenziali nelle variabili di `mariadb.env.example`. L'esempio non è una configurazione pronta per la produzione.
2. Eseguire `./gradlew bootJar`. Caricare le variabili tramite secret manager o servizio di sistema.
3. Eseguire `java -jar build/libs/LibrePM-0.1.7.jar --provision-mariadb`. Il comando costruisce uno schema SQLite temporaneo dalle migrazioni versionate e lo converte con indici, vincoli e dati iniziali. Non avvia HTTP. Rifiuta qualsiasi database destinazione non vuoto.
4. Per importare dati esistenti, impostare `LIBREPM_REMOTE_MIGRATION_SOURCE` sul backup SQLite aggiornato alla stessa versione, prima del comando. Il database sorgente non viene sostituito. Eseguire prima backup coerente con applicazione arrestata.
5. Sostituire utenza amministrativa con utenza applicativa a privilegi minimi; avviare normalmente il jar con `SPRING_PROFILES_ACTIVE=mariadb`. Configurare provider OAuth2 e proxy HTTPS. Impostare CORS su origine frontend esatta tramite `LIBREPM_CORS_ALLOWED_ORIGINS`.
6. Compilare renderer con `VITE_API_BASE_URL=https://librepm.example/api npm run build`. Backend remoto HTTP è rifiutato, tranne loopback per test. Il client include cookie sessione e token CSRF quando disponibile.

`LIBREPM_DB_URL` permette parametri TLS completi. Il valore di default verifica certificato e hostname. Per MariaDB di test esclusivamente locale è possibile usare `sslMode=disable` esplicitamente.

## Limiti e gate di rilascio

- Migrazione iniziale verificata contro MariaDB reale dalla suite Java. Aggiornamenti incrementali usano `db/mariadb` e cronologia separata `flyway_mariadb_schema_history`; V1 aggiunge messaggi anche a database precedenti. Le modifiche future richiedono migrazioni in entrambe le famiglie. Non eseguire nuovamente provisioning.
- Messaggi: feed di 50 elementi, storico paginato, polling ogni 10 secondi, testo semplice di massimo 4000 caratteri. EDITOR/ADMIN/OWNER possono scrivere; VIEWER legge; estranei rifiutati. Nessuna promessa di WebSocket, consegna garantita, allegati o letture confermate.
- Sessione OAuth2 completa, onboarding remoto e associazione sicura identità devono essere verificati con IdP reale prima di distribuzione multiutente pubblica. Selezione profili desktop non equivale ad autenticazione remota.
- Il profilo MariaDB mantiene bind loopback di default. Abilitare accesso remoto solo dopo test identità, permessi, CSRF, CORS, backup/ripristino e TLS.
- Backup MariaDB e archivio allegati devono essere coordinati; verificare ripristino su ambiente separato.
