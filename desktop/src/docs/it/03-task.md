# Capitolo 3: Task, Liste e Cartellini (Kanban) 📝

Se il Progetto è il contenitore, i **Task** sono il contenuto operativo. LibrePM offre strumenti avanzati per gestirli, andando oltre la semplice lista della spesa.

Un task in LibrePM è un'entità ricca di informazioni:
*   **Stato**: A che punto siamo? (Da fare, In corso, Fatto, Bloccato...)
*   **Priorità**: Quanto è urgente? (Visibile a colpo d'occhio).
*   **Tempo**: Scadenze e tracciamento del tempo reale (Focus Timer).
*   **Sotto-attività**: Checklist dettagliate.
*   **Contesto**: Note, allegati e discussioni.

LibrePM ti offre due viste principali:
1.  **Planner (Lista)**: Per una visione d'insieme strutturata e ordinabile.
2.  **Kanban (Lavagna)**: Per un flusso di lavoro visivo e dinamico.

---

## 📝 Creazione Avanzata di un Task

Dovunque tu sia (Planner o Kanban), il pulsante **"+ Task"** apre l'editor completo.

### L'Editor dei Task (TaskEditorModal)
Non è una semplice finestra di inserimento, ma una centrale di comando con **due schede (tab)**:

#### Tab 1: Dettagli
1.  **Titolo** (Obbligatorio): Cosa devi fare? (es. "Scrivere email"). Massimo 500 caratteri.
2.  **Descrizione Breve**: Una breve spiegazione del task.
3.  **Stato**: Seleziona da un dropdown di stati del flusso di lavoro caricati dal sistema (es. TODO, IN_PROGRESS, DONE, REVIEW, BLOCKED). Il predefinito per nuovi task è "TODO".
4.  **Priorità**: Quanto è urgente? Seleziona dalle priorità di sistema — ognuna ha il suo colore per l'identificazione rapida:
    *   🔵 **Low (Bassa)**: Può aspettare.
    *   🟢 **Medium (Media)**: Normale (predefinita).
    *   🟠 **High (Alta)**: Importante.
    *   🔴 **Critical (Critica)**: Fallo SUBITO!
5.  **Scadenza (Deadline)**: Entro quando? Scegli una data con il selettore. Puoi cancellarla se il task non ha scadenza.
6.  **Responsabile (Owner)**: Chi è responsabile? Seleziona tra i membri del team del progetto.
7.  **Checklist**: Suddividi un task complesso in passi più piccoli.
    *   *Esempio*: Task "Preparare Release" → Checklist: "Aggiornare versione", "Scrivere changelog", "Upload build".
    *   Scrivi un elemento e premi Invio (o clicca il pulsante **+**) per aggiungerlo.
    *   Puoi aggiungere voci al volo e spuntarle man mano. Un indicatore percentuale mostra il progresso (es. "67% completato").
    *   Clicca il pulsante **✕** per rimuovere un elemento.
8.  **Tag**: Aggiungi etichette per categorizzare i tuoi task.
    *   Scrivi il nome di un tag e premi Invio (o clicca **+**) per aggiungerlo.
    *   I tag appaiono come badge colorati. Clicca la **✕** su un tag per rimuoverlo.
    *   Utile per raggruppare task per argomento (es. "frontend", "urgente", "design").
9.  **Pianificazione & Gantt** (sezione dedicata):
    *   **Tipo**: Scegli il tipo di task (Task, Milestone, Meeting, Call, ecc.). Selezionando "Milestone" la durata diventa automaticamente zero.
    *   **Inizio Pianificato**: Quando dovrebbe iniziare? Seleziona data e ora.
    *   **Fine Pianificata**: Quando dovrebbe terminare? Se lasciata vuota, viene impostata automaticamente a inizio + 1 giorno.
    *   **Effort Stimato**: Il tempo necessario, in minuti. Un'icona calcolatrice (🔢) appare quando l'effort è **calcolato automaticamente dalle date**: LibrePM conta i giorni lavorativi (Lun–Ven) tra inizio e fine, moltiplicati per 8 ore (480 min/giorno). Puoi sempre sovrascriverlo manualmente. Un'etichetta mostra l'equivalente in giorni (es. "≈ 5.0 giorni").
10.  **Markdown Notes (Personali)**: Un editor di testo ricco per prendere appunti personali, formattati con stili, link e codice. Attiva lo switch **Preview** per vedere come viene renderizzato.
11. **Allegati (Assets)**: Trascina file (Drag & Drop) direttamente nella zona di caricamento o clicca per sfogliare. Documenti, immagini o log sono sempre a portata di mano.
    *   Massimo 10MB per file.
    *   Gli allegati esistenti possono essere scaricati o rimossi.

#### Tab 2: Note & Discussione (solo in modifica di un task esistente)
Un'**interfaccia chat** integrata nel task per comunicare con il team:
*   Vedi tutte le note dei membri del team in una vista conversazione.
*   I tuoi messaggi appaiono a destra (blu), quelli degli altri a sinistra (grigio).
*   Ogni messaggio mostra autore, timestamp e contenuto.
*   Scrivi un messaggio e clicca il pulsante **Invia** (o premi Invio).
*   Puoi **modificare** o **eliminare** i tuoi messaggi.

### Salvare il Task
*   Clicca **"Crea Task"** (per nuovo) o **"Aggiorna"** (per esistente).
*   Il pulsante **"Chiudi"** scarta le modifiche non salvate.

---

## 📋 Il Planner: Ordine e Precisione

Vai nella pagina **"Planner"** (o apri il tuo progetto e clicca "Planner").

La vista **Planner** è ideale per pianificare e organizzare.

### Controlli in Alto
*   **Selettore Progetto**: Un dropdown in alto per passare tra i tuoi progetti.
*   **Ricerca**: Un campo di testo per filtrare i task per titolo istantaneamente.
*   **+ Task**: Il pulsante blu per creare un nuovo task (disabilitato se nessun progetto è selezionato).

### La Lista dei Task
Qui vedi i tuoi task uno sotto l'altro, ognuno con:

*   **Maniglia di Trascinamento** (≡): Afferra questa icona a sinistra per **riordinare i task** trascinandoli su o giù. LibrePM salva il nuovo ordine automaticamente.
*   **Checkbox**: Clicca il quadratino per alternare un task tra Fatto e Da Fare. I task completati appaiono barrati e grigi.
*   **Titolo**: Cliccaci sopra per aprire l'Editor Task completo.
*   **Indicatori Visivi**:
    *   🔴 **Badge Scaduto**: Se la data è passata, appare un badge rosso "OVERDUE".
    *   🎨 **Badge Priorità**: Un badge colorato che mostra il livello di priorità.
    *   📊 **Progresso Checklist**: Mostra quanti sotto-elementi sono completati (es. "3/5").
    *   📅 **Scadenza**: La data di scadenza con un'icona calendario.
    *   👤 **Responsabile**: Chi è il responsabile.
    *   ✅ **Assegnato**: A chi è assegnato.

### Azioni Rapide
*   **Pulsante Note**: Un pulsante dedicato su ogni task che ti porta direttamente alla pagina Note per quel task, senza aprire l'editor completo.
*   **Menu Tre Puntini** (⋮): Clicca per azioni rapide:
    *   **Segna come completato** / **Riapri**: Cambia lo stato del task.
    *   **Elimina**: Rimuovi il task (con conferma).

### Stato Vuoto
Se non ci sono task, vedrai un messaggio: *"Nessun task trovato. Clicca su '+ Task' per crearne uno."*

---

## 📌 La Kanban Board: Il Flusso di Lavoro

Vai nella pagina **"Kanban"**. Qui tutto è più visivo!

La **Kanban** di LibrePM è molto più di una serie di colonne. È uno strumento interattivo progettato per il *flow*.

### 1. Colonne Dinamiche
Le colonne riflettono il tuo flusso di lavoro reale. Vengono caricate dinamicamente dal sistema e possono includere stati come `To Do`, `In Progress`, `Review`, `Done`, `Blocked`. Ogni intestazione di colonna mostra:
*   L'**icona stato** e il **nome** su sfondo colorato.
*   Un **badge conteggio** che mostra quanti task ci sono in quella colonna.

### 2. Drag & Drop Intelligente
*   **Prendi e Sposta**: Clicca una card, tieni premuto e trascinala in un'altra colonna.
*   Rilascia il mouse. Lo stato del task cambia **automaticamente e istantaneamente**.
*   Le card si adattano visivamente mentre le trascini (leggera rotazione ed effetto ombra).
*   Un bordo blu tratteggiato evidenzia la colonna di destinazione.

🎉 **Magia!** Non devi aprire nessuna finestra per cambiare stato.

### 3. La "Card" Potenziata
Ogni cartellino sulla lavagna è ricco di funzioni accessibili senza aprirlo:
*   **Bordo Colorato**: Il lato sinistro della card indica la **Priorità** (es. Rosso per Critica, Giallo per Alta, Verde per Media, Grigio per Bassa).
*   **Titolo**: Il nome del task, in evidenza.
*   **Descrizione**: Un'anteprima troncata (2 righe) della descrizione del task.
*   **Badge Priorità**: Un'etichetta colorata con il nome della priorità.
*   **Badge Checklist**: Vedi a colpo d'occhio il progresso dei sotto-task (es. `2/4`).
*   **Scadenza**: La data di scadenza. Se scaduta, diventa **rossa** con icona di avviso.
*   **Focus Timer Integrato** ⏱️:
    *   Su ogni card c'è un pulsante **Play (▶)**. Cliccalo per avviare il timer su quel task.
    *   **Automazione**: Quando avvii il timer, se il task era in "To Do", LibrePM lo sposta automaticamente in **"In Progress"**!
    *   Il bordo destro diventa **verde** per indicare che è il task attivo.
    *   Clicca **Stop (■)** per terminare la sessione timer.
*   **Menu Rapido** (⋮): I tre puntini in alto a destra permettono **Modifica** ed **Eliminazione** al volo.

### 4. Filtri in Tempo Reale
Usa la **barra di ricerca** nell'intestazione in alto per filtrare la lavagna istantaneamente per titolo. Utile quando hai decine di task e ne cerchi uno specifico.

Il pulsante **Aggiorna** (🔄) ricarica tutti i task dal server.

---

## ✅ Checklist: I Piccoli Pezzi

A volte un task è grande (es. "Preparare la valigia").
Invece di creare 10 piccoli task ("Magliette", "Pantaloni", "Scarpe"...), puoi usare una **Checklist** dentro il task grande!

1. Apri un task (cliccaci sopra).
2. Scorri fino a "Checklist".
3. Scrivi un elemento (es. "Spazzolino") e premi Invio.
4. Scrivine un altro (es. "Caricabatterie").

Ora, man mano che metti le cose in valigia, spunta le voci nella checklist. Quando hai finito tutto, segna il task grande come "Fatto"!

---

## ⚡ Produttività al Massimo (Tips)

1.  **Inizia la giornata**: Apri la Kanban, guarda la colonna "In Progress".
2.  **Focus**: Clicca "Play" sul task più importante. Il timer parte, lo stato si aggiorna.
3.  **Esecuzione**: Lavora. Se hai bisogno di info, apri il task e controlla gli Allegati o la Checklist.
4.  **Completamento**: Quando hai finito, trascina la card in "Done". Ferma il timer se non l'hai già fatto.
5.  **Review**: A fine giornata, guarda la colonna "Done" per gratificarti del lavoro svolto!

---

### 👉 Prossimo Passo
Ora che domini i Task, è tempo di guardare al tempo in modo diverso. Nel prossimo capitolo esploreremo il **Gantt** e come gestire le dipendenze temporali.
