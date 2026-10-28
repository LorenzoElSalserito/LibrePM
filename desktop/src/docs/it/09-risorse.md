# Capitolo 9: Risorse e Carichi di Lavoro 📊

La pagina **Risorse** (il suo titolo è **"Risorse & Carico"**) ti dà una vista panoramica di come il tempo del tuo team è distribuito in un progetto. Ti aiuta a individuare membri sovraccarichi, trovare capacità disponibile e bilanciare i carichi di lavoro.

---

## 🗓️ La Griglia delle Risorse

La vista principale è una **tabella a griglia** con:
*   **Righe**: una riga per ogni membro del progetto, con avatar (cerchio con iniziali) e nome, fissa a sinistra. Se ci sono task senza assegnatario nel periodo, compare anche una riga **"Non assegnato"**.
*   **Colonne**: una colonna per giorno dell'intervallo selezionato, con l'abbreviazione del giorno della settimana e il numero del giorno.
*   **Celle**: le ore di lavoro stimate per quel membro in quel giorno (es. "2.5h"). Le celle senza carico restano vuote.
*   **Ultima colonna (Est. / Act.)**: per ogni membro, il totale delle ore **stimate** e, sotto, delle ore **effettive** dei task del periodo. Il valore effettivo è 🔴 rosso se supera la stima, 🟢 verde altrimenti.

### Da Dove Vengono le Ore
Il carico si basa sull'**effort stimato** dei task assegnati al membro (Capitolo 3):
*   Se il task ha **inizio e fine pianificati**, l'effort viene distribuito in parti uguali su tutti i giorni dell'intervallo.
*   Se ha solo una **scadenza**, tutto l'effort viene conteggiato nel giorno della scadenza.
*   I task senza date, senza effort stimato o archiviati non vengono conteggiati.

### Codifica Colori
Le celle sono colorate per mostrare istantaneamente il livello di carico:
*   🟢 **Ottimale (≤ 4h)**: carico sano.
*   🟡 **Pieno (4h - 8h)**: a capacità.
*   🔴 **Overload (> 8h)**: sovraccarico! Serve intervenire.

Una **legenda** sotto la tabella ricorda i colori. Passando il mouse su una cella appare un tooltip con le "ore stimate".

---

## 🎛️ Controlli

*   **Selettore Progetto** (nella barra superiore e nella barra strumenti della pagina): scegli di quale progetto visualizzare le risorse. Vengono elencati i progetti non archiviati; all'apertura è selezionato il primo.
*   🔄 **Aggiorna** (barra superiore): ricarica i dati.
*   **Data Inizio**: scegli il primo giorno dell'intervallo da visualizzare.
*   **Durata Intervallo**: scegli quanti giorni mostrare:
    *   **7 giorni**: focus sulla settimana.
    *   **14 giorni**: vista sprint di due settimane.
    *   **21 giorni**: prospettiva di tre settimane.
    *   **30 giorni**: panoramica di un mese.

---

## 📋 Dettagli Task per Cella

Clicca su qualsiasi cella della griglia per vedere quali task pesano su quel membro in quel giorno.

Si apre una finestra **"Attività: nome membro"** che mostra:
*   Il giorno selezionato (**"Giorno:"**).
*   La lista dei task, ognuno con:
    *   Titolo del task.
    *   Effort stimato in minuti (o **"Non impostato"**).
    *   Pulsante **"Rimuovi assegnazione"** (icona persona con X): toglie l'assegnatario dal task.
*   Se non ci sono task: "Nessun task assegnato in questa data."

---

## 👤 Gestione Membro

Clicca sul **nome di un membro** nella colonna sinistra per aprire la finestra **"Gestione Membro: nome"**:
*   **Dettagli**: l'ID del membro.
*   Pulsante **"Rimuovi dal Progetto"**: rimuove il membro dal progetto (con conferma). Come nella pagina Team, solo il **Proprietario** del progetto può farlo.

> **💡 Suggerimento:** per aggiungere membri al progetto o cambiarne il ruolo usa la pagina **Team** (Capitolo 6).

---

### 👉 Prossimo Passo
Ora che puoi gestire i carichi di lavoro, scopriamo come avviare progetti da template. Prossimo capitolo: **Template e Gallery**.
