# Capitolo 6: Team e Collaborazione 👥

LibrePM non è solo per il lavoro in solitaria. Puoi costruire un team, collegare colleghi che usano LibrePM, creare membri "virtuali" per chi non ha un account e decidere cosa può fare ciascuno in ogni progetto.

> **💡 Lavorare davvero insieme:** gli altri utenti LibrePM che puoi trovare e aggiungere sono quelli presenti nello **stesso database**: altri profili creati su questo computer oppure, se l'amministratore ha configurato un database MariaDB condiviso, i colleghi che usano lo stesso server. Vedi il Capitolo 21.

---

## 👥 La Pagina Team

Clicca **"Team"** nella barra laterale (il titolo della pagina è **"Gestione Team"**). Qui gestisci chi ha accesso ai tuoi progetti e con quale ruolo.

### La Tabella del Team
La vista principale è una tabella che mostra i membri di tutti i tuoi progetti attivi (una riga per ogni coppia membro/progetto):

| Colonna | Cosa mostra |
|---------|-------------|
| **Membro** | Avatar (cerchio con iniziali), nome visualizzato e @username |
| **Tipo Account** | Badge **"LibrePM"** (utente reale) o **"Locale"** (utente virtuale) |
| **Progetto** | A quale progetto appartiene questa membership |
| **Ruolo** | **"Proprietario"** (badge) per l'owner; per gli altri un menu a tendina con **Amministratore**, **Editore**, **Visualizzatore** |
| **Ruolo di Sistema** | Un menu a tendina opzionale (**"Nessuno"** o un ruolo di sistema) — non disponibile per l'owner |
| **Azioni** | ✏️ **"Modifica Membro"** (solo per utenti locali) e 🗑️ **"Rimuovi dal progetto"** (non disponibile per l'owner) |

### Filtrare per Progetto
Usa il **selettore progetto** in alto a sinistra per mostrare solo i membri di un progetto specifico, o scegli **"Tutti i progetti"** per vederli tutti.

---

## 🔐 Ruoli e Permessi

Ogni membro di un progetto ha un **ruolo di progetto**:

| Ruolo | Cosa può fare |
|-------|---------------|
| **Proprietario** (OWNER) | Tutto. È chi ha creato il progetto; il suo ruolo non può essere cambiato e non può essere rimosso. È l'unico che può **aggiungere** o **rimuovere** membri. |
| **Amministratore** (ADMIN) | Tutti i permessi sui contenuti del progetto e può **cambiare i ruoli** degli altri membri. |
| **Editore** (EDITOR) | Può leggere e modificare i contenuti del progetto (task, note, messaggi...). È il ruolo assegnato di default ai nuovi membri. |
| **Visualizzatore** (VIEWER) | Accesso in sola lettura: vede i contenuti ma non può modificarli né scrivere. |

### Cambiare il Ruolo
Nella colonna **Ruolo**, scegli il nuovo valore dal menu a tendina. Il cambiamento è immediato (appare il messaggio "Ruolo aggiornato"). Solo il **Proprietario** o un **Amministratore** del progetto possono cambiare i ruoli: per gli altri l'operazione viene rifiutata con un errore.

### Ruolo di Sistema (opzionale)
La colonna **Ruolo di Sistema** permette di associare al membro un profilo di permessi predefinito (OWNER, ADMIN, EDITOR, VIEWER). Se lo imposti, per i membri che non sono Proprietario o Amministratore i permessi vengono presi da quel profilo invece che dal ruolo di progetto. Lascia **"Nessuno"** per usare semplicemente il ruolo di progetto.

---

## ➕ Aggiungere Membri al Team

Clicca il pulsante **"+ Nuovo Membro"** in alto a destra. Si apre il dialogo **"Aggiungi Membro al Team"**:

### 1. Seleziona un Progetto
Per prima cosa, scegli in **"Progetto \*"** a quale progetto vuoi aggiungere il membro (obbligatorio).

### 2. Scegli il Tipo di Membro
Un interruttore ti permette di scegliere tra due modalità:

#### 🔗 Collega Utente LibrePM (Amico)
Usa questa modalità per aggiungere qualcuno che esiste già:
*   Il menu **"Seleziona Contatto \*"** mostra due gruppi: **"Contatti"** (i tuoi contatti LibrePM connessi) e **"Membri Virtuali"** (gli utenti locali che hai già creato).
*   Chi è già membro del progetto selezionato appare disabilitato.
*   Clicca **"Aggiungi al Team"**. Il membro viene aggiunto con ruolo **Editore**; puoi cambiarlo subito dopo dalla tabella.

> **⚠️ Nota:** Un utente LibrePM reale deve prima essere tra i tuoi **Contatti** (vedi sotto). Se non hai ancora connessioni, il dialogo te lo segnala.

#### 👤 Crea Utente Locale (Virtuale)
Usa questa modalità per un **membro segnaposto** — qualcuno che non usa LibrePM ma deve apparire nel progetto:
*   Inserisci un **"Nome Visualizzato \*"** (es. "Mario Rossi (Esterno)").
*   Inserisci uno **"Username Identificativo \*"** (es. "mario.rossi.ext") — deve essere univoco nel sistema.
*   Clicca **"Crea Locale"**. L'utente viene creato e aggiunto al progetto come **Editore**.

Gli utenti locali non hanno login. Sono utili per:
*   Stakeholder o fornitori esterni che non usano l'app.
*   Assegnare task a persone non ancora su LibrePM.
*   Pianificare e allocare risorse prima che il team sia completamente configurato.

> **🔒 Permessi:** solo il **Proprietario** del progetto può aggiungere membri.

### Capire i Tipi
Il **pannello destro** della pagina ("Gestione Membri") spiega la differenza:
*   **LibrePM — Utente Reale**: un tuo contatto LibrePM con il proprio account.
*   **Locale — Utente Virtuale**: un segnaposto senza login, usato per assegnare task a risorse esterne.

---

## ✏️ Modificare un Utente Locale

Per gli utenti locali, il pulsante ✏️ **"Modifica Membro"** apre un dialogo dove puoi:
*   Cambiare **"Nome Visualizzato \*"** e **"Username Identificativo \*"**.
*   Spuntare o togliere i **Progetti** di cui fa parte (i nuovi progetti lo aggiungono come Editore, quelli tolti lo rimuovono).

Clicca **"Salva Modifiche"** per confermare.

---

## 🗑️ Rimuovere Membri
Per rimuovere un membro da un progetto:
1. Trovalo nella tabella.
2. Clicca l'**icona cestino** nella colonna Azioni.
3. Conferma la rimozione.

> **⚠️ Nota:** Solo il **Proprietario** può rimuovere membri, e il Proprietario stesso non può essere rimosso.

---

## 🤝 Contributori Esterni

Sotto la tabella dei membri c'è la sezione **"Contributori Esterni"**: un registro di persone esterne (consulenti, revisori, partner) che collaborano al lavoro senza essere membri del progetto.

1. Clicca **"+ Aggiungi Esterno"**.
2. Compila **"Nome Visualizzato \*"**, **Email**, **Organizzazione**.
3. Opzionalmente scegli un **Ruolo di Sistema**.
4. Scegli l'**Ambito**: **Progetto**, **Deliverable** o **Revisione**, e l'**Entità Collegata** (per l'ambito Progetto un progetto o **"Tutti"**; per gli altri l'ID dell'elemento).
5. Clicca **"Aggiungi Esterno"**.

La tabella mostra Nome, Email, Organizzazione, Ruolo di Sistema e Ambito. Il pulsante **"Revoca Accesso"** rimuove il contributore dal registro (con conferma).

> **📌 Nota:** i contributori esterni sono un registro informativo: non ricevono un account né un accesso all'app.

---

## 🌐 Contatti: La Tua Rete Professionale

Vai nella pagina **"Contatti"** nella barra laterale. Qui gestisci la tua rete di utenti LibrePM e i tuoi membri virtuali.

### Colonna Sinistra: La Tua Rete

#### Richieste in Arrivo
Se qualcuno ti ha inviato una richiesta di connessione, vedrai la card **"Richieste in Arrivo"** con:
*   Il nome e lo username della persona.
*   Pulsante **"Accetta"**: aggiungila alla tua rete.
*   Pulsante **"Rifiuta"**: rifiuta la richiesta.

Le nuove richieste e le connessioni accettate arrivano anche come notifiche nella campanella 🔔 (Capitolo 1).

#### I tuoi Contatti
Una lista dei contatti connessi, ognuno con avatar, nome visualizzato e username, e un pulsante **"Rimuovi"** (icona persona rossa) per disconnetterti.

#### Membri Virtuali
La card **"Membri Virtuali"** elenca gli utenti locali che hai creato (badge **"Virtuale"**):
*   ✏️ **Modifica**: cambia nome visualizzato e username.
*   🗑️ **Rimuovi**: elimina il membro virtuale (con conferma).
*   In fondo alla card puoi crearne uno nuovo inserendo **Nome Visualizzato** e **Username** e cliccando **"Crea"**. Se hai progetti attivi, si apre subito il dialogo **"Assegna ai Progetti"**: spunta i progetti e clicca **"Assegna"** (verrà aggiunto come Editore), oppure **"Salta"**.

### Colonna Destra: Cerca e Connetti

#### Cerca Contatti
*   Digita almeno 2 caratteri di **username, nome o email** nel campo **"Username o email..."**.
*   Clicca **"Cerca..."** (o premi Invio).
*   Nei risultati (escluso te stesso, i contatti già connessi e le richieste in sospeso) clicca **"Connetti"** per inviare una richiesta.

#### Richieste Inviate
Sotto i risultati vedi le **"Richieste Inviate"** in sospeso ("A: nome") con il badge **"In attesa"**.

---

### 👉 Prossimo Passo
Il tuo team può anche scambiarsi messaggi in ogni progetto: vedi il Capitolo 19 (**Messaggi di Progetto**). Ora vediamo come visualizzare gli impegni nel tempo. Prossimo capitolo: il **Calendario**.
