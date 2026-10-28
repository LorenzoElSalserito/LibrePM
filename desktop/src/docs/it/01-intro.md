# Benvenuto su LibrePM! 👋

Ciao! Immagina LibrePM come il tuo **laboratorio digitale**. È un posto speciale dove puoi mettere in ordine le tue idee, i tuoi lavori e i tuoi progetti, senza paura di perdere nulla.

Non serve internet per usarlo: di base LibrePM vive nel tuo computer (è "Local-First"), proprio come i tuoi file personali. È veloce, sicuro e sempre pronto.

> **💡 Lavori in team?** Un amministratore può collegare più computer allo stesso database condiviso (MariaDB): così tu e i tuoi colleghi vedete gli stessi progetti, note e messaggi. Trovi tutto nel Capitolo 21.

---

## 🏗️ Passo 1: Chi sei tu? (Il Profilo)

Ogni persona che usa LibrePM ha un **Profilo**, protetto da una password.
Pensa al profilo come al tuo camice da lavoro: serve a LibrePM per sapere chi sta spostando le cose nel laboratorio.

### 🌍 Scegli la lingua subito
In alto a destra della schermata di avvio ci sono due pulsanti, **IT** e **EN**. Cliccane uno per passare all'italiano o all'inglese ancora prima di entrare.

### Creare il tuo profilo
Al primo avvio non ci sono profili, quindi LibrePM ti mostra direttamente il modulo **"Crea il tuo profilo"**. (Se esistono già altri profili, clicca **"Crea nuovo profilo"** sotto l'elenco.)

1. **Username** *(obbligatorio)*: un identificativo univoco di almeno 3 caratteri (es. "mario.rossi").
2. **Nome Visualizzato** *(obbligatorio)*: come vuoi essere chiamato (es. "Mario Rossi").
3. **Email** *(facoltativa)*: se la inserisci deve essere un indirizzo valido.
4. **Password** *(obbligatoria)*: almeno 8 caratteri, con almeno una lettera maiuscola, una minuscola, un numero e un carattere speciale (es. `!@#`). Il pulsante con l'occhio 👁️ mostra o nasconde quello che scrivi.
5. Se vuoi, spunta **"Accedi automaticamente al prossimo avvio"**.
6. Clicca **"Crea profilo e inizia"**.

Se lo username è già usato, LibrePM te lo segnala sotto il campo.

#### Scegli il tuo workspace (facoltativo)
Subito dopo la creazione, se sono disponibili più profili di workspace, compare la schermata **"Scegli il tuo workspace"**: clicca il profilo che descrive meglio il tuo modo di lavorare, oppure **"Salta per ora"**. Potrai cambiarlo in seguito da **Impostazioni** → **Profilo Workspace** (Capitolo 13).

🎉 **Fatto!** Ora sei dentro.

### Entrare con un profilo esistente
Se ci sono già dei profili, vedrai la schermata **"Seleziona un profilo"**:
1. Clicca sulla card del tuo profilo (l'ultimo usato ha il badge **"Ultimo usato"**).
2. Scrivi la tua **Password** e clicca **"Accedi"** (o premi Invio). **"Annulla"** chiude il riquadro.

### Password dimenticata?
1. Seleziona il tuo profilo e clicca **"Password dimenticata?"**.
2. In **"Conferma username"** riscrivi lo username del profilo (deve corrispondere).
3. Inserisci **"Nuova password"** e **"Conferma nuova password"** (stesse regole della creazione).
4. Clicca **"Ripristina password"**: la password viene cambiata e entri direttamente.

### Accesso automatico
Sotto l'elenco dei profili c'è la casella **"Accedi automaticamente con l'ultimo profilo usato"**. Se è attiva quando accedi, al prossimo avvio LibrePM entra da solo con quel profilo **senza chiedere la password**: usala solo su un computer personale. L'accesso automatico si disattiva quando usi **Cambia profilo** o **Esci**.

### Eliminare un profilo
Ogni card ha un'**icona cestino** (**"Elimina profilo"**). Dopo la conferma il profilo viene eliminato: l'azione non si può annullare.

> **💡 Curiosità:** Puoi creare più profili sullo stesso computer! È utile se vuoi tenere separato il lavoro ("Mario Lavoro") dagli hobby ("Mario Hobby").

---

## 🧭 Passo 2: La Mappa del Laboratorio (L'Interfaccia)

Guardiamo insieme lo schermo. È diviso in quattro zone, come un banco di lavoro ben organizzato.

### 1. La Barra Laterale (A Sinistra)
Questa è la tua cassetta degli attrezzi. Da qui puoi andare ovunque (le voci sono in quest'ordine):
*   **Dashboard**: Il cruscotto con i riassunti del tuo lavoro (Capitolo 5).
*   **Messaggi**: Le conversazioni condivise del progetto (Capitolo 19).
*   **In arrivo**: Cattura veloce di task da smistare poi nei progetti (Capitolo 3).
*   **Charter**: Dashboard esecutiva con charter di progetto, OKR, rischi, deliverable e baseline (Capitolo 8).
*   **Progetti**: Dove sono conservate tutte le tue "scatole" di lavoro (Capitolo 2).
*   **Galleria**: Template preconfigurati per avviare nuovi progetti rapidamente (Capitolo 10).
*   **Team**: I membri dei tuoi progetti e i loro ruoli (Capitolo 6).
*   **Risorse**: Carichi di lavoro e allocazione delle persone (Capitolo 9).
*   **Contatti**: La tua rete di altri utenti LibrePM (Capitolo 6).
*   **Calendario**: Una vista calendario dei tuoi task ed eventi (Capitolo 7).
*   **Planner**: Una lista ordinata, perfetta per vedere tutto insieme (Capitolo 3).
*   **Gantt**: Un diagramma temporale con dipendenze e milestone (Capitolo 4).
*   **Kanban**: Cartellini da spostare tra colonne (Capitolo 3).
*   **Note**: Il tuo quaderno Markdown, condiviso con i membri del progetto (Capitolo 5).
*   **Registrazione Tempo**: Timer e inserimento manuale delle ore lavorate (Capitolo 20).
*   **Finanza**: Budget, finanziamenti e costi (Capitolo 14).
*   **Bandi**: Bandi di finanziamento e relativi obblighi (Capitolo 15).
*   **Stakeholder**: Registro e mappa degli stakeholder (Capitolo 15).
*   **Controllo Modifiche**: Richieste di modifica, branch e decisioni (Capitolo 16).
*   **Portfolio**: Programmi che raggruppano più progetti (Capitolo 17).
*   **Guide**: Questa documentazione — sempre a portata di mano.
*   **Analisi**: Grafici e statistiche sulla tua produttività (Capitolo 11).
*   **Integrazioni**: Feed calendario, import/export dei dati (Capitolo 12).
*   **Conflitti Sincronizzazione**: Revisione di eventuali conflitti di sincronizzazione (Capitolo 21).
*   **Impostazioni**: Aspetto, sicurezza, backup e preferenze (Capitolo 13).

Tutte le voci sono sempre visibili, qualunque profilo di workspace tu abbia scelto. La barra laterale può essere **compressa** cliccando il pulsante con la freccia in basso: restano solo le icone (passaci sopra con il mouse per leggerne il nome).

> **💡 Bentornato:** LibrePM ricorda l'ultima pagina che hai aperto e la ripropone al prossimo accesso.

### 2. La Barra Superiore (In Alto)
A sinistra trovi il titolo della pagina dove ti trovi. Sul lato destro:

*   **Azioni Pagina**: Pulsanti che cambiano in base alla pagina (es. "+ Nuovo Progetto", ricerche, filtri).
*   **🔔 Campanella Notifiche**: Un badge rosso mostra quante notifiche non lette hai (il conteggio si aggiorna ogni 30 secondi). Clicca la campanella per vedere le notifiche **non lette**:
    *   **Nuovo commento** (icona blu): un membro del progetto ha scritto una nuova nota su un progetto o task che condividi con te.
    *   **Richieste in Arrivo** (icona gialla): qualcuno vuole connettersi con te nei Contatti.
    *   **Successo** (icona verde): una tua richiesta di connessione è stata accettata.
    *   **Notifica** (icona grigia): tutti gli altri avvisi, ad esempio quando qualcuno ti assegna un task.
    *   Il pulsante **"Segna letta"** su ogni notifica e **"Segna tutte lette"** in alto le tolgono dall'elenco.
*   **Pannello ▸ / Pannello ◂**: Mostra o nasconde il pannello informativo a destra.
*   **👤 Menu Profilo**: Le tue iniziali in un cerchio. Cliccaci sopra per aprire un menu con:
    *   Il tuo nome e username in alto.
    *   **Impostazioni**: Apre la pagina Impostazioni.
    *   **Info LibrePM**: Un popup con versione, sito web, copyright, licenza e il pulsante **"Donazioni"**.
    *   **Apri cartella dati**: Mostra in un avviso il percorso della cartella in cui LibrePM salva i tuoi dati.
    *   **Esporta dati (JSON)**: Esporta rapidamente in un file JSON tutti i progetti del profilo, come **"Esporta JSON"** in Integrazioni (Capitolo 12). Per un backup completo del database usa le Impostazioni (Capitolo 13).
    *   **Cambia profilo**: Torna alla schermata di selezione dei profili (con conferma).
    *   **Esci**: Chiude la sessione (con conferma) e disattiva l'accesso automatico.

### 3. L'Area di Lavoro (Al Centro)
Qui è dove succede la magia! Se sei nei Progetti, vedrai le tue scatole. Se sei nel Planner, vedrai le tue liste.

### 4. Il Pannello Destro (Lato Destro)
Sul lato destro dello schermo c'è un **pannello informativo** che si adatta alla pagina in cui ti trovi. Può mostrare dettagli sull'elemento selezionato, statistiche e suggerimenti. Quando la pagina non lo usa, mostra la versione di LibrePM, il **Progetto attivo** e l'utente con cui sei entrato.

Puoi mostrarlo o nasconderlo in qualsiasi momento con il pulsante **"Pannello"** nella barra superiore, oppure chiuderlo con **Esc**.

---

## ⌨️ Passo 3: La Command Palette (Scorciatoia da Professionista)

LibrePM ha una **Command Palette** — una barra di ricerca rapida per muoverti e trovare le cose all'istante.

### Come aprirla:
Premi **Ctrl+K** (o **Cmd+K** su Mac). Premi di nuovo **Ctrl+K** o **Esc** per chiuderla.

### Cosa puoi fare:
*   **A campo vuoto** vedi i comandi rapidi: **Vai a Dashboard**, **Vai a Progetti**, **Vai a Planner**, **Vai a Kanban**, **Vai a Calendario**, **Vai a Note**, **Vai a Impostazioni** e **Nuovo Progetto** (apre subito la finestra di creazione progetto).
*   **Scrivendo** filtri i comandi "Vai a…" per Dashboard, Planner, Kanban e Note; da 2 caratteri in su LibrePM cerca anche nel **testo dei tuoi task e delle tue note**. Scegli un risultato per aprire il task nel Planner o la nota nella pagina Note.
*   Usa le **frecce** per navigare la lista e **Invio** per selezionare.

---

## ⌨️ Scorciatoie da Tastiera

| Scorciatoia | Cosa fa |
|-------------|---------|
| **Ctrl+K** | Apre/chiude la Command Palette |
| **Ctrl+N** | Va al **Planner** |
| **Ctrl+Shift+N** | Va alle **Note** |
| **Ctrl+L** | Blocca la sessione: torna subito alla selezione profili (senza conferma) e disattiva l'accesso automatico |
| **Esc** | Chiude il pannello destro, la Command Palette o la finestra aperta |

Su Mac usa **Cmd** al posto di **Ctrl**.

---

## 🎨 Passo 4: Chiaro e Scuro (Temi)

LibrePM supporta due temi visivi:
*   **Chiaro**: Pulito e luminoso, ideale per lavorare di giorno.
*   **Scuro**: Riposante per gli occhi, perfetto per le sessioni notturne (è il tema predefinito).

Puoi passare da un tema all'altro in **Impostazioni** → **Aspetto** → **Tema**. L'anteprima è immediata; ricordati di salvare (Capitolo 13).

---

## 🌍 Passo 5: La Tua Lingua

LibrePM è disponibile in **Italiano** e **Inglese**. Puoi scegliere la lingua dai pulsanti **IT/EN** della schermata di avvio oppure da **Impostazioni** → **Aspetto** → **Lingua**.

> **📖 Nota:** Nella pagina **Guide** puoi scegliere la lingua del manuale con un selettore dedicato, indipendente dalla lingua dell'interfaccia.

---

## 🚀 E adesso?
Ora che hai il tuo camice (Profilo) e sai dove sono gli attrezzi (Interfaccia), sei pronto per iniziare a costruire. Nel prossimo capitolo impareremo a creare la tua prima "scatola": il **Progetto**.
