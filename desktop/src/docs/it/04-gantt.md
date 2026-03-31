# Capitolo 4: Il Tempo e la Pianificazione (Gantt) 🗓️

Se i Task sono "Cosa fare", il **Gantt** è "Quando farlo e in che ordine".
È un calendario visivo che ti mostra tutto il progetto su una linea del tempo.

---

## 📅 Cos'è un Gantt?

Immagina un grafico con le date in alto (Gennaio, Febbraio...) e i tuoi task a sinistra.
Ogni task è una barra colorata che va da una data di inizio a una data di fine.

Più lunga è la barra, più tempo ci vuole.

---

## 🔗 Le Dipendenze: "Prima questo, poi quello"

In un progetto complesso, non puoi fare tutto insieme.
Esempio: "Prima devo comprare la vernice (Task A), poi posso dipingere il muro (Task B)".
Non puoi dipingere senza vernice!

LibrePM ti permette di dire: **"Il Task B dipende dal Task A"**.
Questo crea una freccia che collega la fine di A all'inizio di B.

LibrePM supporta quattro tipi di dipendenza:
*   **FS (Finish-to-Start)**: Il più comune. B inizia dopo che A finisce.
*   **SS (Start-to-Start)**: B inizia quando A inizia.
*   **FF (Finish-to-Finish)**: B finisce quando A finisce.
*   **SF (Start-to-Finish)**: B finisce quando A inizia.

### Come crearle?
1. Nel Gantt, vedi un pallino alla fine della barra del Task A.
2. Clicca e trascina quel pallino fino all'inizio della barra del Task B.
3. Rilascia.

🎉 **Fatto!** Ora c'è una freccia. Se sposti il Task A in avanti (ritardo!), il Task B si sposterà automaticamente per rispettare la regola.

> **📌 Nota:** La creazione dipendenze via drag è una funzionalità pianificata. Attualmente, le dipendenze vengono gestite tramite backend e sono completamente visualizzate nel Gantt con frecce di collegamento.

---

## 💎 Le Milestone: "Pietre Miliari"

Le **Milestone** (Pietre Miliari) sono momenti speciali nel progetto.
Non sono cose da fare (durano zero minuti), ma sono **traguardi importanti**.
Esempio: "Consegna del progetto", "Approvazione cliente", "Lancio sito web".

Nel Gantt, le Milestone non sono barre, ma **rombi (♦️)** colorati di giallo.
Servono per vedere subito le date cruciali.

### Come crearle?
Quando crei un task, nel campo "Tipo" (Type), scegli **"Milestone"**.
Automaticamente la sua durata diventerà 0 e apparirà come un rombo nel Gantt.

> **📌 Nota:** La creazione milestone tramite un campo "Tipo" dedicato è un miglioramento pianificato. Attualmente, le milestone vengono identificate da criteri backend e visualizzate come rombi nel Gantt.

---

## 🖱️ Muoversi nel Tempo

Il Gantt è interattivo!

### Modalità Vista
In alto a destra, troverai tre **pulsanti vista** per cambiare la scala temporale:
*   **Day (Giorno)**: Vedi ogni singolo giorno — ideale per la pianificazione a breve termine.
*   **Week (Settimana)**: Vedi il progetto per settimane — la vista bilanciata predefinita.
*   **Month (Mese)**: Vedi il quadro generale — ideale per progetti a lungo termine.

La modalità selezionata è evidenziata in blu.

### Funzionalità Interattive
*   **Click per Modificare**: Clicca su una barra task per aprire l'Editor Task e aggiornare dettagli, date o effort.
*   **Progresso**: La percentuale di completamento viene calcolata automaticamente dalla checklist o dal rapporto tra effort effettivo e stimato.

### Pulsanti Barra Strumenti
*   **🔄 Aggiorna**: Ricarica tutti i dati Gantt dal server.
*   **+ Aggiungi Task**: Crea un nuovo task direttamente dalla vista Gantt (apre l'Editor Task).

---

## 📊 La Legenda

Quando il tuo progetto ha task sul percorso critico, una **legenda** appare sotto la barra strumenti:
*   🔴 **Percorso Critico**: Rosso — task che non possono essere ritardati senza ritardare tutto il progetto.
*   ⬜ **Task Riepilogativo**: Grigio — task padre che raggruppano altri task.
*   🟡 **Milestone**: Giallo con bordo — checkpoint a durata zero.

---

## 📐 Codici WBS

Ogni task nel Gantt può mostrare un **codice WBS** (Work Breakdown Structure) — un sistema di numerazione gerarchica (es. "1.1", "1.2.3") che aiuta a identificare la posizione di ogni task nella struttura del progetto.

---

## 🚨 Il Percorso Critico (Critical Path)

Alcuni task sono più importanti di altri per finire in tempo.
Se ritardi "Comprare le sedie", forse non succede nulla.
Ma se ritardi "Costruire il tetto", ritardi tutta la casa!

I task che non possono essere ritardati senza ritardare tutto il progetto formano il **Percorso Critico**.
LibrePM li colora di **Rosso** nel Gantt. Tienili d'occhio!

Il percorso critico viene calcolato automaticamente dal **Planning Engine** di LibrePM usando l'algoritmo CPM (Critical Path Method), tenendo conto di tutte le dipendenze tra i task.

---

### 👉 Prossimo Passo
Ora sai pianificare come un professionista. Ma dove metti tutte le informazioni, le idee e i documenti? Nel prossimo capitolo parleremo delle **Note**.
