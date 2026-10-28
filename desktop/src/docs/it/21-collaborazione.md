# Capitolo 21: Database Condiviso e Sincronizzazione 🌐

Finora abbiamo immaginato LibrePM come un laboratorio tutto tuo, dentro il tuo computer. Ma cosa succede se vuoi lavorare con i colleghi, ognuno dal proprio PC, sugli **stessi progetti**?

In questo capitolo scopriamo le due modalità di lavoro di LibrePM e cosa cambia quando si usa un **database condiviso**.

---

## 🏠 Modalità Locale (Predefinita)

Di base LibrePM salva tutto in un database **SQLite** nella cartella dati del tuo computer (il percorso lo trovi nel Menu Profilo → **Apri cartella dati**).
*   Funziona senza internet e senza server.
*   Più profili sullo stesso computer condividono lo stesso database: ognuno entra con la propria password.
*   Gli altri computer non vedono i tuoi dati: per spostarli usa l'export del database o il backup cifrato (Capitolo 13) oppure l'export/import JSON (Capitolo 12).

---

## 🏢 Modalità Condivisa (MariaDB)

Un **amministratore** può collegare LibrePM a un database **MariaDB** condiviso. Così più installazioni desktop leggono e scrivono gli stessi dati: progetti, task, note, messaggi, file allegati, notifiche, membri del team.

### Chi la configura?
La configurazione **non si fa dall'app**. In **Impostazioni** → **Database Esterno** i controlli (tipo di database, host, pulsante di connessione) sono volutamente **disattivati** e servono solo a mostrare le opzioni (Capitolo 13): le credenziali del database non devono essere salvate nell'interfaccia. Il messaggio di quella card che dice che stai usando il database SQLite locale compare sempre, anche quando sei collegato a MariaDB.

L'amministratore, invece:
1. Prepara un database MariaDB vuoto e lo inizializza **una sola volta** con il comando di provisioning di LibrePM (se vuole, importando un database locale esistente).
2. Su ogni computer avvia LibrePM con le variabili d'ambiente del profilo MariaDB, ad esempio `SPRING_PROFILES_ACTIVE=mariadb` più host, nome database, utente e password (`LIBREPM_DB_*`).
3. Si occupa di sicurezza di rete (TLS, firewall), backup e ripristino del database.

> **🛠️ Per gli amministratori:** la procedura completa, le variabili e i limiti sono descritti nel `README.md` del progetto (sezione *External MariaDB*) e in `deploy/README.md`.

---

## 👥 Cosa Cambia per Te

Quando il tuo LibrePM è collegato al database condiviso:

### Profili
La schermata **"Seleziona un profilo"** (Capitolo 1) mostra i profili di **tutte le persone** registrate nel database condiviso. Ognuno entra con la propria password. Evita l'accesso automatico sui computer usati da più persone.

### Progetti e ruoli
Vedi i progetti di cui sei **membro**. Il proprietario li condivide aggiungendo i colleghi dalla pagina **Team** (Capitolo 6), con uno di questi ruoli:
*   **OWNER** e **ADMIN**: possono fare tutto nel progetto.
*   **EDITOR**: può leggere e modificare.
*   **VIEWER**: può solo leggere.

### Note condivise
Le note collegate a un **progetto** o a un **task** (Capitolo 5) sono visibili a tutti i membri del progetto:
*   Tutti i membri (OWNER, EDITOR, VIEWER…) possono **leggere** le note degli altri.
*   Tutti i membri, **VIEWER compresi**, possono anche **scrivere** note su task e progetti: a differenza dei Messaggi, le note non seguono il ruolo di sola lettura.
*   Solo **l'autore** può modificare o eliminare la propria nota; per gli altri è in sola lettura.
*   Il riquadro **Note Collegate** di un task mostra solo le note dei progetti di cui sei membro, e solo l'autore di una nota può collegarla ad altri elementi.
*   Le note di un task o di un progetto compaiono in ordine cronologico, anche quando due colleghi scrivono nello stesso istante.
*   Quando qualcuno crea una nota, gli altri membri ricevono una notifica **"Nuovo commento"** nella campanella 🔔.
*   Nella pagina Note, gli ambiti **Tutti / In arrivo / Inviate** ti aiutano a separare le note ricevute da quelle scritte da te.

### File e allegati condivisi
Gli allegati dei **task** (Capitolo 3) e i **File del progetto** della Dashboard sono salvati **nel database**, non solo sul disco di chi li carica. Per questo un collega su un altro PC li vede nell'elenco e li apre con un clic.
*   Tutti i membri del progetto (VIEWER compresi) possono **vedere e scaricare** i file.
*   **Caricare** file richiede il permesso di modifica (OWNER, ADMIN, EDITOR); i VIEWER non possono caricare.
*   Solo **chi ha caricato** un file può eliminarlo, e l'eliminazione vale per tutti.
*   Chi non è membro del progetto non vede i file e non può scaricarli né caricarne.
*   Quando qualcuno aggiunge un file, gli altri membri ricevono una notifica **"Nuovo file"** nella campanella 🔔: il pulsante **Apri i file** porta al task (nel Planner) o alla Dashboard del progetto.
*   Massimo **50 MB** per file. Sul tuo PC LibrePM tiene una copia dei file già aperti nella cartella dati (`assets`), così le aperture successive sono immediate.
*   I file caricati prima di questa versione erano solo sul PC di chi li aveva caricati: vengono copiati nel database automaticamente al successivo avvio di LibrePM **su quel PC**. Finché non succede, gli altri vedono il file in elenco ma il download risponde che il contenuto non è ancora disponibile.

> **📌 Nota:** anche in modalità locale i file vengono salvati nel database SQLite: se la cartella `assets` va persa, LibrePM li ricrea alla prima apertura.

### Messaggi
La pagina **Messaggi** (Capitolo 19) mostra la stessa conversazione di progetto su tutti i computer.

### Quanto è "in tempo reale"?
LibrePM non usa una connessione in diretta: le pagine si aggiornano leggendo il database.
*   I **Messaggi** si aggiornano da soli ogni 10 secondi.
*   Il contatore della **campanella** si aggiorna ogni 30 secondi.
*   Le altre pagine mostrano le modifiche dei colleghi quando le riapri o usi il loro pulsante di aggiornamento.

---

## 🔄 Conflitti di Sincronizzazione

La pagina **Conflitti Sincronizzazione** (titolo **"Revisione Conflitti"**) serve a risolvere a mano i casi in cui la stessa informazione esiste in due versioni diverse, una **locale** e una **remota**, che LibrePM non riesce a unire da solo.

*   Se tutto è in ordine vedrai *"Nessun conflitto in sospeso"*: è la situazione normale.
*   Se ci sono conflitti, un badge giallo accanto al titolo ne mostra il numero. A sinistra c'è l'elenco (tipo di elemento, tipo di conflitto e identificativo).
*   Clicca un conflitto per vedere, affiancate, la **"Versione Locale"** e la **"Versione Remota"** (in formato dati).
*   Scegli:
    *   **"Mantieni Locale"**: tieni la versione locale.
    *   **"Accetta Remoto"**: adotta la versione remota.
*   Un avviso conferma la risoluzione e il conflitto sparisce dall'elenco.

> **📌 Nota:** con il database condiviso tutti scrivono direttamente sugli stessi dati, quindi di norma questa pagina resta vuota.

---

### 🎉 Fine del Percorso
Hai completato il manuale di LibrePM! Ora conosci ogni angolo del tuo laboratorio digitale, da solo o in squadra. Buon lavoro! 🚀
