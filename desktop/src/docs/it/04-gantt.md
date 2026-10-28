# Capitolo 4: Il Tempo e la Pianificazione (Gantt) 🗓️

Se i Task sono "Cosa fare", il **Gantt** è "Quando farlo e in che ordine".
È un calendario visivo che ti mostra tutto il progetto su una linea del tempo.

---

## 📅 Cos'è un Gantt?

Immagina un grafico con le date in alto (Gennaio, Febbraio...) e i tuoi task a sinistra.
Ogni task è una barra colorata che va da una data di inizio a una data di fine.

Più lunga è la barra, più tempo ci vuole.

Vai nella pagina **"Gantt"** nella barra laterale: il diagramma mostra i task del **progetto corrente**, che puoi cambiare con il selettore in alto.

### Da dove arrivano le date?
Le barre usano l'**Inizio Pianificato** e la **Fine Pianificata** del task (sezione "Planning & Gantt" dell'Editor Task, Capitolo 3). Se un task non le ha, LibrePM le stima per poterlo comunque disegnare:
*   **Inizio**: dalla scadenza meno la durata stimata (effort), oppure il giorno prima della scadenza, oppure la data di creazione.
*   **Fine**: dalla scadenza, oppure inizio + effort, oppure inizio + 1 giorno.

Queste stime servono solo alla visualizzazione: per una pianificazione precisa, imposta le date nell'editor.

---

## 🔗 Le Dipendenze: "Prima questo, poi quello"

In un progetto complesso, non puoi fare tutto insieme.
Esempio: "Prima devo comprare la vernice (Task A), poi posso dipingere il muro (Task B)".
Non puoi dipingere senza vernice!

Una dipendenza dice: **"Il Task B dipende dal Task A"**.
Nel Gantt appare come una **freccia** che collega A a B.

Il motore di pianificazione supporta quattro tipi di dipendenza:
*   **FS (Finish-to-Start)**: Il più comune. B inizia dopo che A finisce.
*   **SS (Start-to-Start)**: B inizia quando A inizia.
*   **FF (Finish-to-Finish)**: B finisce quando A finisce.
*   **SF (Start-to-Finish)**: B finisce quando A inizia.

### Da dove arrivano?
Nella versione attuale **non c'è un comando nell'interfaccia per disegnare o modificare le dipendenze** (non si possono trascinare frecce tra le barre). Le dipendenze vengono create automaticamente quando avvii un progetto da un **template della Galleria** (Capitolo 10) che le prevede.

### Cosa succede se un task cambia?
Quando salvi un task modificando inizio pianificato, fine pianificata o effort, il **Planning Engine** ricalcola il piano del progetto:
*   I task che dipendono da quello modificato vengono **spostati in avanti** se necessario, per rispettare la dipendenza.
*   Per i task con un effort stimato, la fine pianificata viene ricalcolata a partire dall'inizio e dall'effort, contando solo il tempo lavorativo.
*   Le milestone restano a durata zero (fine = inizio).

---

## 💎 Le Milestone: "Pietre Miliari"

Le **Milestone** (Pietre Miliari) sono momenti speciali nel progetto.
Non sono cose da fare (durano zero minuti), ma sono **traguardi importanti**.
Esempio: "Consegna del progetto", "Approvazione cliente", "Lancio sito web".

Nel Gantt le Milestone sono evidenziate in **arancione** (nella legenda le riconosci dal rombo ♦️).
Servono per vedere subito le date cruciali.

### Come crearle?
Nell'Editor Task, nel campo **"Tipo"**, scegli **"Milestone"**.
Automaticamente la fine pianificata coincide con l'inizio e l'effort diventa "0 min".

---

## 🖱️ Muoversi nel Tempo

### Modalità Vista
In alto a destra, troverai tre **pulsanti vista** per cambiare la scala temporale:
*   **Giorno**: Vedi ogni singolo giorno — ideale per la pianificazione a breve termine.
*   **Settimana**: Vedi il progetto per settimane — la vista predefinita.
*   **Mese**: Vedi il quadro generale — ideale per progetti a lungo termine.

La modalità selezionata appare in grassetto.

Il pulsante flottante **"Oggi"** nell'angolo del diagramma riporta la vista sulla data odierna (il Gantt si centra su oggi anche all'apertura).

### Funzionalità Interattive
*   **Click per Modificare**: Clicca su una barra per vedere un riepilogo (titolo, eventuale badge CRITICAL, tipo, inizio, fine, avanzamento, stato) e aprire l'Editor Task per aggiornare dettagli, date o effort.
*   **Niente trascinamento**: Le barre non si spostano né si allungano col mouse; per cambiare le date usa l'Editor Task.
*   **Avanzamento**: La percentuale di completamento è calcolata automaticamente: dalla checklist se il task ne ha una, altrimenti dal rapporto tra effort effettivo e stimato. I task in uno stato completato valgono sempre 100%.

### Pulsanti Barra Strumenti
*   **Selettore progetto** e **+ Attività** nell'intestazione della pagina.
*   **🔄 Aggiorna**: Ricarica tutti i dati Gantt dal server.
*   **+ Aggiungi Task**: Crea un nuovo task direttamente dalla vista Gantt (apre l'Editor Task).

Se il progetto non ha task, vedrai un messaggio che ti invita ad aggiungere task con date di inizio e fine.

---

## 📊 La Legenda

Quando il progetto ha dei task, accanto al titolo "| Timeline" appare una **legenda** (su finestre abbastanza larghe):
*   🔴 **Critica**: task sul percorso critico.
*   🔶 **Milestone**: checkpoint a durata zero.
*   🔵 **Attività**: task standard.

I **Task Riepilogativi** (tipo "Task Riepilogativo") sono disegnati in **grigio scuro**.

---

## 📐 Codici WBS

Quando un task fa parte di una struttura WBS (Work Breakdown Structure), il suo **codice WBS** (es. "1.1", "1.2.3") appare davanti al titolo nella barra e nel riepilogo. Aiuta a identificare la posizione di ogni task nella struttura del progetto.

> **📌 Nota:** Nella versione attuale l'interfaccia non ha un editor della struttura WBS: i codici compaiono solo per i task a cui è già stato assegnato un nodo WBS. Se nessun task ne ha uno, le barre mostrano semplicemente il titolo.

---

## 🚨 Il Percorso Critico (Critical Path)

Alcuni task sono più importanti di altri per finire in tempo.
Se ritardi "Comprare le sedie", forse non succede nulla.
Ma se ritardi "Costruire il tetto", ritardi tutta la casa!

I task che non possono essere ritardati senza ritardare tutto il progetto formano il **Percorso Critico**.
LibrePM li colora di **Rosso** nel Gantt e mostra il badge **CRITICAL** nel riepilogo. Tienili d'occhio!

Il percorso critico viene calcolato automaticamente dal **Planning Engine** di LibrePM usando l'algoritmo CPM (Critical Path Method), a partire dalle date pianificate e dalle dipendenze tra i task.

---

### 👉 Prossimo Passo
Ora sai pianificare come un professionista. Ma dove metti tutte le informazioni, le idee e i documenti? Nel prossimo capitolo parleremo delle **Note**.
