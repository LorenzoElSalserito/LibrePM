# Capitolo 13: Impostazioni e Personalizzazione ⚙️

La pagina **Impostazioni** ti permette di personalizzare LibrePM, proteggere il tuo profilo, gestire i backup, configurare il feed calendario e le notifiche, e segnalare bug. La raggiungi dalla barra laterale oppure dal menu del profilo (in alto a destra) → **Impostazioni**.

---

## 💾 Salvare le Preferenze

In alto a destra della pagina trovi due pulsanti:
*   **"Salva Modifiche"**: diventa **verde** quando ci sono modifiche non salvate (tema, lingua, backup automatico) ed è disattivato quando tutto è salvato.
*   **"Ripristina Predefiniti"**: riporta le impostazioni ai valori originali (con conferma).

Le altre sezioni (profilo workspace, password, notifiche, export) agiscono **subito**, senza passare da "Salva Modifiche".

---

## 🎨 Aspetto

### Tema
Scegli tra due temi visivi:
*   **Chiaro**: Interfaccia pulita e luminosa.
*   **Scuro**: Interfaccia più scura, più riposante per gli occhi in condizioni di poca luce.

Il cambiamento si vede **subito** come anteprima; clicca **"Salva Modifiche"** per renderlo permanente.

### Lingua
Passa tra **Italiano** e **Inglese**. L'interfaccia si aggiorna istantaneamente; salva per mantenere la scelta al prossimo avvio.

> **💡 Nota:** la lingua di questa **Guida** si sceglie a parte, con il selettore in alto nella pagina Guide.

---

## 🧩 Profilo Workspace

Una serie di card con i profili disponibili (es. **Personale**, **Operazioni Team**, **PM / PMO Light**, **No-Profit e Donazioni**, **Bandi e Finanziamenti**, **Agenzia / Consulenza**, **Consorzio R&S**). Ogni card mostra una descrizione e i moduli associati; quello attivo ha il bordo blu e un segno di spunta. Clicca una card per attivarla: la scelta viene salvata subito.

> **📌 Stato attuale:** il profilo scelto viene memorizzato, ma in questa versione **non nasconde** voci della barra laterale: tutte le pagine restano raggiungibili qualunque profilo tu scelga. Puoi scegliere il profilo anche durante la creazione del profilo utente (Capitolo 1).

---

## 🔐 Sicurezza

### Cambia Password
Compila **Password Attuale**, **Nuova Password** e **Conferma Password** (ogni campo ha l'icona 👁️ per mostrare/nascondere il testo), poi clicca **"Cambia Password"**.
*   La nuova password deve avere almeno **4 caratteri** e i due campi devono coincidere.
*   Se il tuo profilo non ha ancora una password, lascia vuota "Password Attuale".

### Rimuovi Password
Il pulsante **"Rimuovi Password"** toglie la password dal profilo; richiede la **Password Attuale**. Senza password chiunque usi il computer potrà aprire il tuo profilo.

> **💡 Password dimenticata?** Si recupera dalla schermata di accesso, confermando il proprio username (Capitolo 1).

### Identità Collegate
I pulsanti **"Collega Google"**, **"Collega Microsoft"** e **"Collega Keycloak"** sono visibili ma **disattivati**: *"Disponibile in una versione futura"*.

---

## 📅 Calendario Esterno (iCal)

LibrePM può esporre un **feed iCal** con i task **assegnati a te** (non archiviati) che hanno una scadenza o un inizio pianificato.

*   **Campo URL**: il link personale del feed (di sola lettura) con il pulsante **Copia**.
*   **"Rigenera Token"** (bordo rosso): crea un nuovo token dopo una conferma e mostra il nuovo URL. Il link precedente smette di funzionare.

> **📌 Primo utilizzo:** se il campo URL è vuoto, clicca **"Rigenera Token"** per generare il tuo link.

> **⚠️ Raggiungibilità:** il link punta al backend in esecuzione sul tuo computer (`localhost`). Funziona con le app calendario installate sullo stesso computer mentre LibrePM è aperto; servizi online come Google Calendar non possono raggiungerlo.

La pagina Integrazioni ha una card simile (Capitolo 12), ma il link funzionante è quello di questa pagina.

---

## 🗄️ Database Esterno

Questa card mostra le modalità di database previste, ma i controlli sono **intenzionalmente disattivati**:
*   Selettore **Tipo Database**: *SQLite (Locale)* e *MariaDB (External, administrator configuration)*.
*   Campo **Host** e pulsante **"Connetti a DB Esterno"**.

Il collegamento a un database **MariaDB** condiviso non si fa da qui: lo configura l'amministratore tramite variabili d'ambiente del backend. Come funziona la collaborazione su database condiviso è spiegato nel **Capitolo 21**.

> **📌 Nota:** il messaggio *"Attualmente stai usando il database locale SQLite."* è fisso e compare anche quando l'app è collegata a MariaDB.

---

## 💾 Backup e Sincronizzazione

### Backup Automatico
Un interruttore che memorizza la tua preferenza (attivo di default). Viene salvato con **"Salva Modifiche"**.

> **📌 Stato attuale:** in questa versione non c'è un processo che esegua backup programmati in base a questo interruttore: non considerarlo una copia di sicurezza.

### File del database (.db)
*   **"Esporta Database (.db)"** (bordo blu): salva una copia completa e coerente del database locale (tutti i profili) in un file `LibrePM-db-….db` scelto con la finestra di sistema.
*   **"Importa Database (.db)"** (bordo rosso): ripristina un file `.db` esportato in precedenza, **sostituendo tutti i dati correnti**. Dopo la conferma LibrePM:
    1.  controlla che il file sia davvero un database LibrePM integro e non creato da una versione più recente (altrimenti lo rifiuta senza toccare nulla);
    2.  salva una **copia di sicurezza** del database attuale in `config/backup/librepm-before-import-….db` dentro la cartella dati (la trovi con **"Apri cartella dati"** nel menu del profilo);
    3.  riavvia il backend, applica il file e ricarica l'app.

Durante il ripristino compare un indicatore di avanzamento; non chiudere l'app finché non si ricarica.

> **📌 Database condiviso (MariaDB):** se l'amministratore ha configurato un database condiviso, i pulsanti del file `.db` e del backup cifrato sono **disattivati** con un avviso: con un server condiviso il backup è compito dell'amministratore (Capitolo 21). Puoi comunque usare l'export/import **JSON** in Integrazioni (Capitolo 12).

### Backup cifrato
Inserisci una **Passphrase** (almeno 4 caratteri) e clicca **"Esporta Cifrato"**: LibrePM salva un file `LibrePM-encrypted-….enc.json` contenente il database cifrato con **AES-256-GCM**. Conserva la passphrase in un luogo sicuro: senza di essa il file non può essere decifrato.

Per ripristinarlo inserisci **la stessa passphrase** e clicca **"Importa backup cifrato"**, poi scegli il file. Il ripristino funziona come l'import del `.db` (verifica, copia di sicurezza, riavvio). Con una passphrase sbagliata compare un errore e i dati attuali restano invariati.

---

## 🔔 Preferenze Notifiche

Una tabella con un tipo di evento per riga (es. *Nuova Nota*, *Task Assegnato*, *Menzione*, *Richiesta di Connessione*, *Scadenza in Avvicinamento*, *Soglia Budget*, *Approvazione Richiesta*) e due colonne di canale: **In-App** (la campanella) e **Desktop** (notifica di sistema). Spunta o togli la spunta: ogni modifica viene salvata subito.

Tre pulsanti applicano un preset a tutta la tabella:
*   **Tutti**: tutti gli eventi attivi.
*   **Essenziali**: task assegnati, menzioni, scadenze in avvicinamento e richieste di approvazione.
*   **Solo Critiche**: solo scadenze in avvicinamento e soglie di budget.

---

## 🩺 Diagnostica

Il pulsante **"Genera Pacchetto Diagnostico"** scarica un file ZIP utile per la risoluzione dei problemi. Il pacchetto **non** contiene dati personali né contenuti di progetto.

---

## 🐛 Segnalazione Bug

Trovato un bug? Aiuta a migliorare LibrePM!
*   Clicca **"Segnala un Bug"** (pulsante giallo con icona busta).
*   Nell'app desktop si apre il tuo client email con un messaggio pre-compilato indirizzato allo sviluppatore.
*   Descrivi il problema e invia l'email.

---

## ℹ️ Informazioni

Una card che mostra:
*   **Versione**: numero di versione corrente di LibrePM.
*   **Copyright**: © Lorenzo DM 2026.
*   **Licenza**: AGPLv3.
*   **Sito Web**: link a https://www.lorenzodm.it
*   **Supporto**: un invito a sostenere lo sviluppo, con il pulsante **"Donazioni"**.
*   **Piattaforma**: Desktop (Electron) o Web.

Le stesse informazioni sono disponibili dal menu del profilo → **Info LibrePM**.

---

## 🛡️ I Tuoi Dati

In fondo alla pagina, la card **"I Tuoi Dati"** offre **"Esporta i Miei Dati"** e **"Richiedi Cancellazione Dati"**. Sono descritti nel **Capitolo 18** (Privacy e Conformità).

---

### 🎉 Ce l'hai fatta!
Ora conosci le funzionalità principali di LibrePM. Nei capitoli successivi trovi i moduli avanzati (finanza, bandi, controllo modifiche, portfolio, conformità), i **Messaggi** di progetto (Capitolo 19), il **Time Tracking** (Capitolo 20) e il lavoro su **database condiviso** (Capitolo 21). Buon divertimento nel tuo laboratorio digitale! 🚀
