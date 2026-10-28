# Capitolo 3: Task, Liste e Cartellini (Kanban) 📝

Se il Progetto è il contenitore, i **Task** sono il contenuto operativo. LibrePM offre strumenti avanzati per gestirli, andando oltre la semplice lista della spesa.

Un task in LibrePM è un'entità ricca di informazioni:
*   **Stato**: A che punto siamo? (TODO, IN_PROGRESS, REVIEW, DONE, BLOCKED, CANCELLED)
*   **Priorità**: Quanto è urgente? (Visibile a colpo d'occhio).
*   **Tempo**: Scadenze, pianificazione e tracciamento del tempo reale (Focus Timer).
*   **Sotto-attività**: Checklist dettagliate.
*   **Contesto**: Note, allegati e discussioni.

LibrePM ti offre tre punti di accesso principali ai task:
1.  **In arrivo (Inbox)**: Per catturare al volo un'idea senza ancora decidere a quale progetto appartiene.
2.  **Planner (Lista)**: Per una visione d'insieme strutturata e ordinabile.
3.  **Kanban (Lavagna)**: Per un flusso di lavoro visivo e dinamico.

Planner, Kanban, Gantt e Calendario lavorano sempre sul **progetto corrente**, che puoi cambiare dal selettore in alto in ciascuna pagina.

---

## 📥 In arrivo: Cattura Veloce

La pagina **"In arrivo"** (Inbox) nella barra laterale è la tua vaschetta delle idee: *"Cattura veloce task — spostali in un progetto quando sei pronto."*

### Aggiungere un task
1. Scrivi il titolo nel campo **"Aggiungi task veloce..."** in alto.
2. Premi Invio o clicca **"Aggiungi"**.

Il task finisce nella tua Inbox personale, senza bisogno di scegliere un progetto. Un badge in alto a destra mostra quanti task ci sono in Inbox.

### Cosa vedi per ogni task
*   Titolo ed eventuale descrizione.
*   Badge colorati di **stato** e **priorità**.
*   La **scadenza**, se presente.

### Spostare un task in un progetto
1. Clicca **"Sposta"** accanto al task.
2. Scegli il progetto dal menu **"Seleziona progetto"**.
3. Conferma con il pulsante verde ✓ (oppure annulla con ✕).

Il task lascia l'Inbox e compare nel progetto scelto (Planner, Kanban, ecc.).

Se l'Inbox è vuota vedrai: *"La tua inbox è vuota. Aggiungi un task per iniziare."*

---

## 📝 Creazione Avanzata di un Task

Dal Planner e dalla Kanban il pulsante **"+ Attività"** in alto apre l'editor completo (nel Gantt c'è anche **"Aggiungi Task"**; nel Calendario basta cliccare un giorno).

### L'Editor dei Task
Non è una semplice finestra di inserimento, ma una centrale di comando. Per un task nuovo il titolo è **"Nuovo Task"**; per un task esistente è **"Modifica Task"** e compaiono **tre schede (tab)**: **Dettagli**, **Note & Discussione** e **Cronologia**.

#### Tab 1: Dettagli
1.  **Titolo** (Obbligatorio): Cosa devi fare? (es. "Scrivere email"). Massimo 500 caratteri.
2.  **Descrizione Breve**: Una breve spiegazione del task.
3.  **Stato**: Seleziona da un menu a tendina con gli stati del flusso di lavoro caricati dal sistema (TODO, IN_PROGRESS, REVIEW, DONE, BLOCKED, CANCELLED). Il predefinito per nuovi task è TODO.
4.  **Priorità**: Quanto è urgente? Seleziona dalle priorità di sistema — ognuna ha il suo colore per l'identificazione rapida:
    *   ⚪ **LOW**: Può aspettare (grigio).
    *   🔵 **MEDIUM**: Normale (blu, predefinita).
    *   🟠 **HIGH**: Importante (arancione).
    *   🔴 **CRITICAL**: Fallo SUBITO! (rosso).
5.  **Scadenza**: Entro quando? Scegli una data con il selettore. Puoi cancellarla se il task non ha scadenza.
6.  **Proprietario**: Chi è responsabile? Seleziona tra i membri del team del progetto.
7.  **Planning & Gantt** (riquadro dedicato):
    *   **Tipo**: Scegli il tipo di task: Task, Milestone, Riunione, Chiamata, Appuntamento, Scadenza, Promemoria, Blocco Task, Task Riepilogativo, Task Deliverable, Task di Revisione, Task Finanziario. Selezionando **Milestone** la fine coincide con l'inizio e l'effort diventa "0 min".
    *   **Inizio Pianificato**: Quando dovrebbe iniziare? Seleziona data e ora.
    *   **Fine Pianificata**: Quando dovrebbe terminare? Se è vuota quando scegli l'inizio, viene impostata automaticamente a inizio + 1 giorno. Non può essere precedente all'inizio.
    *   **Effort Stimato**: Il tempo necessario, in minuti. Un'icona calcolatrice (🔢) appare quando l'effort è **calcolato automaticamente dalle date**: LibrePM conta i giorni lavorativi (Lun–Ven) tra inizio e fine (minimo 1), moltiplicati per 8 ore (480 min/giorno). Puoi sempre sovrascriverlo manualmente. Da 480 minuti in su un'etichetta mostra l'equivalente in giorni (es. "≈ 5.0 giorni").
8.  **Pianificazione Avanzata** (sezione richiudibile, clicca sul titolo per aprirla): **Inizio Effettivo**, **Fine Effettiva**, **Ore Stimate**, **Ore Effettive**, e i campi **Deliverable** e **Stakeholder** in cui inserire l'ID di un deliverable o di uno stakeholder collegato.
9.  **Contesto Finanziario** (sezione richiudibile): il campo **Contesto di Finanziamento** per annotare da dove arrivano i fondi (es. "Bando XYZ, voce di budget 42").
10. **Checklist**: Suddividi un task complesso in passi più piccoli.
    *   *Esempio*: Task "Preparare Release" → Checklist: "Aggiornare versione", "Scrivere changelog", "Upload build".
    *   Scrivi un elemento e premi Invio (o clicca il pulsante **+**) per aggiungerlo.
    *   Spunta le voci man mano. Un indicatore percentuale mostra il progresso (es. "67% completed").
    *   Clicca il pulsante **✕** per rimuovere un elemento.
11. **Tag**: Aggiungi etichette per categorizzare i tuoi task.
    *   Scrivi il nome di un tag e premi Invio (o clicca **+**) per aggiungerlo.
    *   I tag appaiono come badge. Clicca la **✕** su un tag per rimuoverlo.
    *   Utile per raggruppare task per argomento (es. "frontend", "urgente", "design").
12. **Note Markdown (Personali)**: Un editor di testo per appunti formattati con stili, link e codice. Attiva lo switch **Anteprima** per vedere come viene renderizzato.
13. **Allegati**: Trascina file (Drag & Drop) direttamente nella zona di caricamento o clicca per sfogliare.
    *   **Task nuovo**: i file vengono caricati quando salvi il task.
    *   **Task esistente**: il riquadro **Allegati** mostra i file già presenti con dimensione, autore e data. Clicca **Carica file** (o trascina i file nel riquadro) per caricarli subito, senza dover salvare il task.
    *   Clicca l'icona di **download** per aprire o salvare un file. Funziona anche con i file caricati da un collega su un altro PC: il contenuto viaggia insieme al database (Capitolo 21).
    *   Solo **chi ha caricato** un file vede il cestino: dopo la conferma **"Eliminare per tutti?"** il file sparisce per tutti i membri del progetto.
    *   Massimo **50 MB** per file. Formati accettati: immagini (png, jpg, jpeg, webp, gif), pdf, testo (txt, md, json, csv), documenti Office e OpenDocument (doc, docx, xls, xlsx, ppt, pptx, odt, ods, odp, rtf) e zip.
    *   Gli allegati del progetto (non legati a un task) si gestiscono dal riquadro **File del progetto** della Dashboard.

#### Tab 2: Note & Discussione (solo per task esistenti)
Un'**interfaccia chat** integrata nel task per comunicare con il team. Ogni messaggio è una **nota collegata al task**, quindi la ritrovi anche nella pagina Note (Capitolo 5):
*   In alto, il riquadro **Note Collegate** elenca le note che fanno riferimento a questo task; cliccandone una la apri nella pagina Note.
*   I tuoi messaggi appaiono a destra (blu), quelli degli altri membri a sinistra (grigio) con le loro iniziali.
*   Ogni messaggio mostra autore, data/ora e contenuto.
*   Scrivi nel campo **"Scrivi una nota..."** e premi Invio o il pulsante di invio.
*   Sui **tuoi** messaggi trovi la matita (apre la nota nella pagina Note per modificarla) e il cestino (elimina, con conferma). I messaggi degli altri sono in sola lettura.
*   Gli altri membri del progetto ricevono una notifica della nuova nota.

> **💡 Nota:** Questa discussione è legata al singolo task. Per una chat generale del progetto usa la pagina **Messaggi** (Capitolo 19).

#### Tab 3: Cronologia (solo per task esistenti)
Mostra la storia dei **cambi di stato** del task: per ogni passaggio vedi stato precedente → nuovo stato e data/ora. Se non ci sono cambi vedrai *"Nessuna cronologia"*.

### Salvare il Task
*   Clicca **"Crea Task"** (per nuovo) o **"Aggiorna"** (per esistente). Il pulsante è presente nella scheda Dettagli.
*   Il pulsante **"Chiudi"** chiude l'editor scartando le modifiche non salvate.
*   **Richiedi Revisione** (solo per task esistenti, scheda Dettagli): scegli un membro del progetto dal menu; riceverà una notifica di richiesta revisione.

---

## 📋 Il Planner: Ordine e Precisione

Vai nella pagina **"Planner"** (oppure, dal Dettaglio Progetto, clicca **"Vai a Planner"**).

La vista **Planner** è ideale per pianificare e organizzare.

### Controlli in Alto
*   **Selettore Progetto**: Un menu a tendina per passare tra i tuoi progetti. Scegliendo la voce **"Seleziona progetto..."** vieni portato alla pagina Progetti.
*   **Ricerca** (**"Cerca..."**): Filtra i task cercando il testo nel titolo o nella descrizione.
*   **+ Attività**: Il pulsante blu per creare un nuovo task (disabilitato se nessun progetto è selezionato).

### La Lista dei Task
Qui vedi i tuoi task uno sotto l'altro, ognuno con:

*   **Maniglia di Trascinamento** (⋮⋮): Afferra la riga per **riordinare i task** trascinandoli su o giù. LibrePM salva il nuovo ordine automaticamente.
*   **Checkbox**: Clicca il quadratino per alternare un task tra DONE e TODO. I task completati appaiono barrati e grigi.
*   **Titolo**: Cliccaci sopra per aprire l'Editor Task completo.
*   **Indicatori Visivi**:
    *   🔴 **Badge Scaduto**: Se la scadenza è passata e il task non è DONE, appare un badge rosso "IN RITARDO" e la riga ha il bordo rosso.
    *   🎨 **Badge Priorità**: Un badge colorato con il nome della priorità.
    *   📅 **Scadenza**: La data di scadenza con un'icona calendario.
    *   👤 **Proprietario** e ✅ **Assegnatario**: Chi è responsabile del task.
    *   ☑️ **Progresso Checklist**: Quanti sotto-elementi sono completati (es. "3/5").

### Azioni Rapide
*   **Pulsante Note**: Un pulsante su ogni task che ti porta direttamente alla pagina Note filtrata su quel task, senza aprire l'editor completo.
*   **Menu Tre Puntini** (⋮):
    *   **Segna come completato** / **Riapri**: Cambia lo stato del task.
    *   **Elimina Task**: Rimuovi il task (con conferma).

### Stato Vuoto
Se non ci sono task, vedrai un messaggio: *"Nessun task trovato. Clicca su '+ Attività' per crearne uno."*

---

## 📌 La Kanban Board: Il Flusso di Lavoro

Vai nella pagina **"Kanban"**. Qui tutto è più visivo!

### 1. Colonne Dinamiche
Le colonne vengono caricate dagli stati di sistema: **Da Fare**, **In Corso**, **Revisione**, **Fatto**, **Bloccato** e **Cancellato**. Ogni intestazione di colonna mostra:
*   L'**icona stato** e il **nome** su sfondo colorato.
*   Un **badge conteggio** che mostra quanti task ci sono in quella colonna.

### 2. Drag & Drop Intelligente
*   **Prendi e Sposta**: Clicca una card, tieni premuto e trascinala in un'altra colonna.
*   Rilascia il mouse. Lo stato del task cambia **automaticamente e istantaneamente** (un messaggio conferma "Task spostato in ...").
*   La card trascinata si inclina leggermente con un effetto ombra.
*   Un bordo blu tratteggiato evidenzia la colonna di destinazione.

🎉 **Magia!** Non devi aprire nessuna finestra per cambiare stato.

### 3. La "Card" Potenziata
Ogni cartellino sulla lavagna è ricco di funzioni accessibili senza aprirlo:
*   **Bordo Colorato**: Il lato sinistro della card ha il colore della **Priorità** (rosso CRITICAL, arancione HIGH, blu MEDIUM, grigio LOW).
*   **Titolo**: Il nome del task, in evidenza.
*   **Descrizione**: Un'anteprima troncata (2 righe) della descrizione del task.
*   **Badge Priorità**: Un'etichetta colorata con il nome della priorità.
*   **Badge Checklist**: Vedi a colpo d'occhio il progresso dei sotto-task (es. `2/4`).
*   **Scadenza**: Giorno e mese di scadenza. Se scaduta, diventa **rossa** con icona di avviso.
*   **Focus Timer Integrato** ⏱️:
    *   Su ogni card c'è un pulsante **Play (▶)** ("Avvia timer"). Cliccalo per avviare una sessione di focus su quel task.
    *   **Automazione**: Quando avvii il timer, se il task non è già in "In Corso", LibrePM lo sposta automaticamente in **"In Corso"**!
    *   Il bordo destro diventa **verde** per indicare che il timer è attivo. Puoi avere timer attivi su più card contemporaneamente.
    *   Clicca **Stop (■)** ("Ferma timer") per terminare la sessione.
    *   Le sessioni registrate alimentano la Focus Heatmap degli Analytics (Capitolo 11). Per registrare ore su un task con timer o inserimento manuale c'è anche la pagina **Registrazione Tempo** (Capitolo 20).
*   **Menu Rapido** (⋮): I tre puntini in alto a destra offrono **Modifica** (apre l'editor) ed **Elimina** (con conferma).

### 4. Filtri e Controlli
*   **Selettore Progetto**: Cambia il progetto mostrato sulla lavagna.
*   **"Cerca task..."**: Filtra la lavagna istantaneamente per titolo.
*   **+ Attività**: Crea un nuovo task.
*   **Aggiorna** (🔄): Ricarica task e timer attivi dal server.

---

## ✅ Checklist: I Piccoli Pezzi

A volte un task è grande (es. "Preparare la valigia").
Invece di creare 10 piccoli task ("Magliette", "Pantaloni", "Scarpe"...), puoi usare una **Checklist** dentro il task grande!

1. Apri un task (cliccaci sopra nel Planner, o usa **Modifica** sulla card Kanban).
2. Scorri fino a "Checklist".
3. Scrivi un elemento (es. "Spazzolino") e premi Invio.
4. Scrivine un altro (es. "Caricabatterie").
5. Clicca **"Aggiorna"** per salvare.

Ora, man mano che metti le cose in valigia, spunta le voci nella checklist. Quando hai finito tutto, segna il task grande come "Fatto"!

---

## ⚡ Produttività al Massimo (Tips)

1.  **Svuota la mente**: Durante il giorno butta ogni idea in **In arrivo**; spostala in un progetto quando hai tempo.
2.  **Inizia la giornata**: Apri la Kanban, guarda la colonna "In Corso".
3.  **Focus**: Clicca "Play" sul task più importante. Il timer parte, lo stato si aggiorna.
4.  **Esecuzione**: Lavora. Se hai bisogno di info, apri il task e controlla gli Allegati o la Checklist.
5.  **Completamento**: Quando hai finito, trascina la card in "Fatto". Ferma il timer se non l'hai già fatto.
6.  **Review**: A fine giornata, guarda la colonna "Fatto" per gratificarti del lavoro svolto!

---

### 👉 Prossimo Passo
Ora che domini i Task, è tempo di guardare al tempo in modo diverso. Nel prossimo capitolo esploreremo il **Gantt** e come gestire le dipendenze temporali.
