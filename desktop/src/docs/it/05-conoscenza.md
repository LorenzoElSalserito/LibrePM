# Capitolo 5: Idee, Numeri e Sicurezza 🧠

Abbiamo parlato di scatole (Progetti), cose da fare (Task) e tempi (Gantt).
Ma dove metti le tue **idee**, i tuoi appunti e i documenti importanti?

Benvenuto nella sezione **Conoscenza**!

---

## 📝 Le Note: Il Tuo Quaderno Digitale

Le **Note** sono pagine bianche dove puoi scrivere tutto quello che vuoi.
Esempio: "Lista della spesa per la festa", "Idee per il nuovo logo", "Verbale riunione".

### Come creare una Nota
1. Vai nella pagina **"Note"** (Notes).
2. Clicca su **"+ Nuova Nota"** (in alto a destra).
3. Si apre un dialogo che chiede:
    *   **Titolo**: Scrivi un nome (es. "Idee Logo").
    *   **Associa a**: Scegli se collegare la nota al **Progetto Corrente** o a un **Task Specifico**.
    *   Se scegli "Task Specifico", appare un selettore a tendina per scegliere il task.
4. Clicca **"Crea Nota"**.

### 🔗 Collegare le Note
Le note non vivono nel vuoto. Sono sempre collegate a un progetto o a un task specifico.
Esempio: La nota "Idee Logo" serve per il progetto "Sito Web".

Quando aprirai quel progetto o quel task, vedrai subito la nota collegata!

### 📂 Ambiti delle Note
La pagina Note ha una barra laterale a sinistra con **pulsanti ambito** per filtrare le tue note:
*   **All (Tutto)**: Vedi tutte le note a cui hai accesso.
*   **Inbox (Ricevute)**: Vedi le note inviate da altri membri del team.
*   **Sent (Inviate)**: Vedi le note che hai creato e inviato.

### 🔍 Filtri per Contesto
Sotto i pulsanti ambito, puoi filtrare ulteriormente:
*   **All contexts (Tutti i contesti)**: Vedi tutto.
*   **Project (Progetto)**: Vedi solo le note a livello di progetto.
*   **Task**: Vedi solo le note a livello di task (appare un selettore a tendina per scegliere un task specifico).

### ✍️ L'Editor
L'area principale mostra la nota selezionata con un potente editor Markdown. Hai **tre modalità editor** (pulsanti toggle in alto a destra):
*   **✏️ Edit (Modifica)**: Scrivi e modifica il sorgente Markdown.
*   **📐 Live (Diviso)**: Vedi l'editor a sinistra e un'anteprima live a destra, affiancati.
*   **👁️ Preview (Anteprima)**: Vedi solo il risultato renderizzato (vista di sola lettura).

Il pulsante **"Salva"** diventa **verde** quando ci sono modifiche non salvate, e mostra "Salvato" (grigio) quando tutto è aggiornato.

> **🔒 Solo Lettura**: Se una nota è stata creata da qualcun altro, puoi leggerla ma non modificarla. Un badge "Solo Lettura" appare accanto al titolo.

### 🗑️ Eliminare le Note
Puoi eliminare le note di cui sei proprietario cliccando l'**icona cestino** sulla card della nota nella barra laterale. Verrà chiesta conferma.

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

Prova a scrivere nella nota e guarda l'anteprima (Preview) per vedere come viene!

---

## 📊 La Dashboard: Il Cruscotto

La **Dashboard** è la prima cosa che vedi quando apri LibrePM.
È il riassunto di tutto il tuo lavoro.

Qui trovi:
1.  **Progetti Attivi**: Quanti ne hai aperti.
2.  **Task Completati**: Quanti ne hai finiti. Bravo!
3.  **Prossime Scadenze**: I task che scadono presto. Non dimenticarli!
4.  **Attività Recente**: Cosa hai fatto ultimamente (creato task, modificato note...).

È utile per capire "Come sta andando?" con un colpo d'occhio.

> **💡 Suggerimento Pro:** Per una dashboard più avanzata con OKR, baseline, rischi, deliverable e charter di progetto, vai nella pagina **Charter** (Capitolo 8).

---

## 💾 Salvare e Esportare (Backup)

LibrePM salva tutto automaticamente sul tuo computer. Non devi preoccuparti.
Ma se vuoi una copia di sicurezza o vuoi spostare i dati su un altro computer?

### 1. Esportare il Database (Il Cuore)
Vai in **Impostazioni** (Settings) -> **Backup e Sincronizzazione**.
Clicca su **"Esporta Database (.db)"**.
Salverà un file `.db` con TUTTO dentro (progetti, task, note...). Conservalo bene!

### 2. Esportare in JSON (Leggibile)
Vuoi vedere i dati in un formato che anche altri programmi possono leggere?
Clicca sulla tua foto profilo in alto a destra -> **"Esporta Database"** -> **"Esporta JSON"**.
Creerà un file di testo leggibile con tutti i tuoi dati.

### 3. Esportare CSV (Per Progetto)
Ti serve un foglio di calcolo? Vai nella pagina **Integrazioni** e clicca **"Esporta CSV (Progetto)"** per esportare i task del progetto corrente come file CSV.

### 4. Importare
Se cambi computer, puoi **"Importare Database"** (sempre dalle Impostazioni) per rimettere tutto a posto.

Puoi anche importare da un backup JSON tramite la pagina **Integrazioni** (**"Importa JSON"**). Nota: questo sovrascriverà i dati correnti.

### 5. Feed Calendario (iCal)
LibrePM genera un **URL feed iCal** che puoi aggiungere a Google Calendar, Outlook o Apple Calendar per vedere le scadenze dei tuoi task. Trovi il link in **Impostazioni** o nella pagina **Integrazioni**. Vedi il Capitolo 12 per i dettagli.

---

### 🎉 Congratulazioni!
Ora conosci tutti i segreti essenziali di LibrePM!
Ma c'è molto di più da esplorare — continua con i prossimi capitoli per scoprire Team e Collaborazione, Calendario, Dashboard Esecutiva, Risorse, Template, Analytics, Integrazioni e Impostazioni.
