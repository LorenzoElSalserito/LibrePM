# Capitolo 5: Idee, Numeri e Sicurezza 🧠

Abbiamo parlato di scatole (Progetti), cose da fare (Task) e tempi (Gantt).
Ma dove metti le tue **idee**, i tuoi appunti e i documenti importanti?

Benvenuto nella sezione **Conoscenza**!

---

## 📝 Le Note: Il Tuo Quaderno Digitale

Le **Note** sono pagine dove puoi scrivere tutto quello che vuoi, in Markdown.
Esempio: "Idee per il nuovo logo", "Verbale riunione", "Motivazione della scelta del fornitore".

Vai nella pagina **"Note"** nella barra laterale: il titolo della pagina è **"Note & Feed"**.

> **📌 Serve un progetto:** la pagina Note lavora sempre nel contesto del **progetto corrente**. Se non hai ancora un progetto, vedrai il messaggio *"Seleziona o crea un progetto per gestire le note."*

### Come creare una Nota
1. Clicca su **"Nuova Nota"** in alto a destra.
2. Si apre un dialogo che chiede:
    *   **Titolo** (obbligatorio): Scrivi un nome (es. "Idee Logo").
    *   **Tipo Nota**: Che genere di nota è? Puoi scegliere tra *Promemoria* (predefinito), *Razionale*, *Nota Riunione*, *Nota Rischio*, *Nota Bando*, *Nota Sponsor*, *Nota Audit*, *Decisione* e *Brief*.
    *   **Associa a**: Scegli se collegare la nota al **Progetto Corrente** o a un **Task Specifico**.
    *   Se scegli "Task Specifico", appare un selettore a tendina con i task del progetto.
3. Clicca **"Crea Nota"**.

> **💡 Suggerimento:** Se nella barra laterale hai già filtrato per un task, il dialogo propone già quel task come destinazione.

Puoi creare note anche da altri punti dell'app: dalla scheda note dell'**Editor Task** (Capitolo 3) e dal riquadro **"Note del Progetto"** della Dashboard (vedi sotto).

### 🔗 Collegare le Note
Le note non vivono nel vuoto: sono sempre collegate a un **progetto** o a un **task**.
Esempio: La nota "Idee Logo" serve per il progetto "Sito Web".

### 👥 Note condivise con il team
Le note sono condivise con tutti i **membri del progetto** (Capitolo 6):
*   Ogni membro (OWNER, EDITOR o VIEWER) può **leggere** le note del progetto e dei suoi task.
*   Solo l'**autore** di una nota può modificarla o eliminarla.
*   Quando qualcuno crea una nota, gli altri membri del progetto ricevono una **notifica** nella campanella 🔔.

> **📌 Note ≠ Messaggi:** le note sono documenti collegati a un progetto o a un task. Per una conversazione libera con il team usa la pagina **Messaggi** (Capitolo 19).

### 📂 Ambiti delle Note (Feed)
La pagina Note ha una barra laterale a sinistra con tre **pulsanti ambito**:
*   **Tutti**: Tutte le note dei progetti di cui sei membro.
*   **In arrivo**: Le note scritte dagli **altri** membri nei tuoi progetti.
*   **Inviate**: Le note che hai scritto **tu**.

> **📌 Nota:** il feed comprende le note di **tutti** i progetti di cui fai parte, non solo del progetto corrente. Cambiare ambito azzera il filtro per contesto.

### 🔍 Filtri
Sotto i pulsanti ambito puoi filtrare ulteriormente:
*   **Tutti i contesti**: Il feed dell'ambito scelto.
*   **Progetto**: Solo le note collegate direttamente al progetto corrente.
*   **Attività**: Solo le note di un task (appare un selettore **"Seleziona Task..."** per sceglierlo).
*   **Tutti i Tipi**: Un menu per mostrare solo un tipo di nota (es. solo le *Decisioni*).
*   **Solo Evidenze** (icona scudo): Mostra solo le note segnate come evidenza.

In alto a destra trovi anche il campo **"Cerca note..."**, che filtra le note per titolo e contenuto.

### 🗂️ La Lista delle Note
Ogni nota nella lista mostra titolo, un'anteprima del contenuto e la data di modifica. Inoltre:
*   Il **bordo sinistro verde** indica le tue note, quello **grigio** le note degli altri (per queste viene mostrato anche il nome dell'autore).
*   Un'icona scudo indica una nota **Evidenza**, un lucchetto una nota **Congelata**.

### ✍️ L'Editor
Selezionando una nota, l'area principale mostra un editor Markdown. Hai **tre modalità** (pulsanti in alto a destra):
*   **✏️ Editore**: Scrivi e modifica il sorgente Markdown.
*   **📐 Editor + Anteprima**: Editor e anteprima live affiancati.
*   **👁️ Anteprima**: Solo il risultato renderizzato.

Accanto al titolo puoi cambiare il **tipo** della nota. Il pulsante **"Salva"** diventa **verde** quando ci sono modifiche non salvate e mostra **"Salvato"** quando tutto è aggiornato.

> **🔒 Sola Lettura**: Se una nota è stata scritta da qualcun altro, puoi leggerla ma non modificarla. Un badge **"Sola Lettura"** appare accanto al titolo e l'editor mostra solo l'anteprima.

### 🛡️ Evidenze, Congelamento e Revisioni
Sulle tue note hai a disposizione alcuni strumenti di governance:
*   **Segna come Evidenza** (icona scudo): Marca la nota come prova documentale (es. un verbale da conservare). Ricliccando scegli **"Rimuovi Evidenza"**.
*   **Congela Nota** (icona lucchetto): Blocca la nota. Una nota **Congelata** non può essere modificata finché non la sblocchi con **"Scongela Nota"**.
*   **Revisioni** (icona orologio, disponibile per tutti): Apre il pannello con la cronologia. Ogni volta che salvi una modifica al contenuto, LibrePM registra una revisione numerata. Clicca su una revisione per ricaricarla nell'editor, poi clicca **"Salva"** per ripristinarla.

### 🗑️ Eliminare le Note
Puoi eliminare le note di cui sei autore cliccando l'**icona cestino** sulla nota nella lista. Verrà chiesta conferma.

---

## ✍️ Scrivere con Stile (Markdown)

LibrePM usa un modo speciale di scrivere chiamato **Markdown**.
Non preoccuparti, è facilissimo!
Invece di cliccare bottoni per fare grassetto o liste, usi dei simboli sulla tastiera.

Ecco i trucchi principali:

*   **Titoli**: Metti un `#` davanti al testo.
    *   `# Titolo Grande` -> **Titolo Grande**
    *   `## Titolo Medio` -> **Titolo Medio**
*   **Grassetto**: Metti due asterischi `**` intorno alla parola.
    *   `**Importante**` -> **Importante**
*   **Corsivo**: Metti un asterisco `*` intorno.
    *   `*Corsivo*` -> *Corsivo*
*   **Liste Puntate**: Metti un trattino `-` e uno spazio.
    *   `- Pane`
    *   `- Latte`
*   **Liste Numerate**: Metti un numero e un punto.
    *   `1. Primo`
    *   `2. Secondo`
*   **Link**: Metti il testo tra parentesi quadre `[]` e l'indirizzo tra tonde `()`.
    *   `[Google](https://google.com)`

Prova a scrivere nella nota e passa in **Anteprima** per vedere come viene!

---

## 📊 La Dashboard: Il Cruscotto

La **Dashboard** è il riassunto di tutto il tuo lavoro. Alla prima apertura LibrePM parte da qui; in seguito riapre l'ultima pagina che stavi usando.

Dall'alto verso il basso trovi:
1.  **Quattro contatori**: **Progetti Attivi**, **Task Completati**, **Task Totali** e **In Ritardo**.
2.  **Heatmap del Focus** e **grafico delle stime** (stimato vs effettivo): gli stessi widget della pagina Analytics (Capitolo 11).
3.  **Varianza Effort**: Ore stimate contro ore effettive per i task del progetto selezionato.
4.  **Approvazioni in Attesa**: Le richieste di approvazione indirizzate a te, con chi le ha richieste (**"Richiesto da"**) e i pulsanti **Approva** / **Rifiuta**.
5.  **Note del Progetto**: Scegli un progetto dal menu per vedere le sue note, crearne una al volo (pulsante **+**) o aprirle nella pagina Note (**"Vedi tutti"**).
6.  **File del Progetto**: Gli allegati del progetto selezionato nel menu delle Note del Progetto. Con **Carica file** (o trascinando i file nel riquadro) li condividi con tutti i membri; l'icona di download apre anche i file caricati dai colleghi su altri PC; solo chi ha caricato un file può eliminarlo (Capitolo 21). Massimo 50 MB per file.
7.  **Salute Progetto**: L'elenco dei progetti attivi, ordinati per salute, con il badge **In orario**, **Attenzione** o **In Ritardo** e il numero di task scaduti.
8.  **Task in Ritardo (Top 10)**: I dieci task scaduti più vecchi. **"Vedi tutti"** ti porta al Planner.
9.  **Progetti Completati / Archiviati**: I progetti archiviati, con il pulsante **Riapri**.

Il pulsante **Aggiorna** (🔄) in alto ricarica tutti i dati. Se non hai ancora nessun progetto, un popup **"Nessun Progetto"** ti invita a crearne uno con **"Vai a Progetti"**.

> **💡 Suggerimento Pro:** Per una dashboard più avanzata con OKR, baseline, rischi, deliverable e charter di progetto, vai nella pagina **Charter** (Capitolo 8).

---

## 💾 Salvare e Esportare (Backup)

LibrePM salva tutto automaticamente nel database: non devi premere "salva" per i tuoi progetti.
Se vuoi una copia di sicurezza, gli strumenti si trovano in due posti:

*   **Impostazioni → Backup e Sincronizzazione**: export/import del file di database (.db) e **backup cifrato** con ripristino (Capitolo 13).
*   **Integrazioni → Importa / Esporta Dati**: export/import JSON dei progetti ed export/import CSV dei task di un progetto (Capitolo 12).

C'è anche la voce **"Esporta dati (JSON)"** nel menu del profilo (in alto a destra).

> **💡 Consiglio:** per una copia di sicurezza completa usa l'export del database (.db) o il backup cifrato; l'export JSON è comodo per spostare progetti tra profili o computer. Su **database condiviso** (MariaDB) il backup completo è compito dell'amministratore del server (Capitolo 21), ma l'export/import JSON resta disponibile.

Per il **feed calendario (iCal)** vedi il Capitolo 13.

---

### 🎉 Congratulazioni!
Ora conosci i segreti essenziali di LibrePM!
Ma c'è molto di più da esplorare — continua con i prossimi capitoli per scoprire Team e Collaborazione, Calendario, Dashboard Esecutiva, Risorse, Template, Analytics, Integrazioni, Impostazioni e i moduli avanzati.
