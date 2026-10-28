# Capitolo 8: Dashboard Esecutiva e Charter 📊

La pagina **Charter** (il suo titolo è **"Dashboard Esecutiva"**) è la vista gestionale più completa di LibrePM. Riunisce governance di progetto, obiettivi, rischi, deliverable e monitoraggio delle performance in un unico posto, sempre riferiti al **progetto corrente**.

---

## 🎛️ Scegliere il Progetto

Nella barra superiore trovi:
*   Un **selettore progetto**: scegli il progetto da analizzare. La voce **"Seleziona progetto..."** ti porta alla pagina Progetti.
*   Un pulsante 🔄 **Aggiorna** per ricaricare i dati.

Se nessun progetto è selezionato, la pagina mostra solo il messaggio **"Seleziona un progetto"**.

In cima alla pagina vedi il **nome del progetto**, la dicitura "Dashboard Esecutiva & Charter", il **badge di stato** e un pulsante **"Aggiorna"**.

---

## 📊 La Riga di Statistiche

Quattro card riassuntive danno una panoramica istantanea:

| Card | Cosa mostra |
|------|-------------|
| **Completamento** | Percentuale dei task completati nel progetto |
| **In Ritardo** | Numero di task oltre la loro scadenza |
| **Deliverable** | Deliverable completati (progresso al 100%) sul totale (es. "3/7") |
| **Rischi Alti** | Numero di rischi con **impatto** Alta o Critica |

### Il Badge di Stato
Accanto al nome del progetto appare lo stato generale:
*   🟢 **In Linea**: tutto procede secondo il piano.
*   🟡 **A Rischio**: l'ultima baseline mostra un piccolo scostamento.
*   🔴 **In Ritardo**: l'ultima baseline mostra uno scostamento importante.
*   ⚫ **Bloccato**: almeno un rischio ha impatto **Critica**.

Come viene calcolato: se esiste un rischio con impatto Critica lo stato è sempre **Bloccato**; altrimenti deriva dal confronto con la baseline più recente (vedi "Baseline e Varianza"). Senza baseline il progetto risulta **In Linea**.

---

## 📋 Charter del Progetto

Il Charter è il documento fondante del tuo progetto.

### Card "Charter di Progetto"
*   **Sponsor**: chi sponsorizza/finanzia il progetto (campo testo).
*   **Project Manager**: chi guida il progetto (campo testo).
*   **Obiettivi**: cosa mira a raggiungere il progetto (area di testo).
*   Clicca **"Modifica"** nell'intestazione della card per modificare i campi, poi **"Salva"** per salvarli.

### Sezioni Markdown
Sotto la card trovi due sezioni con editor Markdown:
*   **Obiettivi / Problem Statement**: quale problema risolve questo progetto?
*   **Business Case**: perché vale la pena fare questo progetto?

Ogni sezione ha un pulsante **"Modifica"** / **"Salva"**. In modifica appare l'editor Markdown; dopo il salvataggio viene mostrato il contenuto formattato. Se una sezione è vuota leggi "Nessun contenuto. Clicca Modifica per aggiungere."

---

## 🎯 OKR (Objectives and Key Results)

Gli OKR ti aiutano a tracciare obiettivi strategici e risultati misurabili.

### Creare un OKR
1. Clicca il pulsante **"+"** nell'intestazione della sezione **OKR**.
2. Scrivi l'obiettivo nel campo **"Obiettivo..."** (es. "Aumentare il coinvolgimento degli utenti").
3. Clicca **"Aggiungi"** (o premi Invio).

### Aggiungere Key Results
Ogni OKR può avere più Key Result:
1. Clicca l'icona **⊕** ("Aggiungi Key Result") accanto all'OKR.
2. Compila:
    *   **Nome Key Result** (es. "Raggiungere 1000 utenti attivi al giorno").
    *   **Obiettivo**: il valore target (es. 1000).
    *   **Unità** (es. "utenti", "%", "€").
3. Clicca **"Aggiungi"**.

### Monitorare il Progresso
*   Clicca su un OKR per **espandere** i suoi Key Result.
*   Ogni Key Result mostra un campo con il **valore corrente** (modificalo: viene salvato quando esci dal campo), il target con la sua unità ("/ 1000 utenti") e una piccola barra di avanzamento.
*   Il **progresso complessivo** dell'OKR è la media dei suoi Key Result. La sua barra è 🟢 verde (≥100%), 🔵 blu (≥50%) o 🟡 gialla (<50%).

### Eliminare
*   Clicca l'**icona cestino** accanto a un OKR per eliminarlo (con conferma).
*   Clicca l'icona **ⓧ** accanto a un Key Result per rimuoverlo (senza conferma).

---

## ⚠️ Registro dei Rischi

Traccia e gestisci i rischi di progetto con valutazione di probabilità e impatto. Il badge rosso nell'intestazione mostra il numero totale dei rischi.

### Aggiungere un Rischio
1. Clicca il pulsante **"+"** nell'intestazione della sezione **Rischi**.
2. Compila:
    *   **"Descrizione rischio..."** (es. "Lo sviluppatore chiave potrebbe lasciare a metà progetto").
    *   **Probabilità**: Bassa, Media, Alta o Critica (predefinita: Media).
    *   **Impatto**: Bassa, Media, Alta o Critica (predefinita: Media).
    *   **"Strategia di mitigazione..."** (es. "Formare il team trasversalmente, documentare tutto").
3. Clicca **"Aggiungi"**.

### Visualizzare i Rischi
*   Ogni rischio mostra la descrizione e due badge: **P:** (probabilità) e **I:** (impatto).
*   Clicca su un rischio per **espandere** e leggere la **Mitigazione**.
*   Clicca l'**icona cestino** per eliminarlo (con conferma).

> **💡 Ricorda:** un rischio con impatto **Critica** porta lo stato del progetto a **Bloccato**, e quelli con impatto Alta o Critica vengono contati in **Rischi Alti**.

---

## 📦 Deliverable Chiave

Traccia cosa il tuo progetto deve produrre, con avanzamento e stato di rischio.

### Aggiungere un Deliverable
1. Clicca il pulsante **"+"** nell'intestazione della sezione **Deliverable Chiave**.
2. Compila:
    *   **"Nome deliverable..."** (es. "Mockup Interfaccia Utente").
    *   La **data di scadenza** (opzionale).
3. Clicca **"Aggiungi"**. Il deliverable parte da 0% con stato OK.

### Monitorare i Deliverable
Ogni deliverable mostra:
*   **Nome** (barrato quando il progresso raggiunge il 100%) e la data di scadenza.
*   **Menu stato rischio**: OK, A Rischio o Bloccato.
*   **Barra di progresso** e un campo numerico (0–100) per aggiornare la percentuale.
*   **Pulsante ✕**: elimina il deliverable (con conferma).

---

## 📈 Baseline e Varianza

Le Baseline sono una "fotografia" del piano di progetto in un momento preciso: ti permettono di misurare quanto la realtà si sta allontanando dal piano.

### Creare una Baseline
1. Clicca il pulsante **"+"** nell'intestazione della sezione **Baseline**.
2. Inserisci un nome nel campo **"Nome baseline..."** (es. "Piano Sprint 1", "Timeline Originale").
3. Clicca **"Crea"**.

LibrePM salva per ogni task le date di **inizio e fine pianificate** e l'**effort stimato**.

### Visualizzare la Varianza
In cima alla sezione, il riquadro **"Ultima Varianza"** confronta il piano attuale con la baseline più recente:
*   **Nome della baseline** e **badge di stato** (In Linea, A Rischio, In Ritardo).
*   **Varianza Tempi**: di quanti giorni la fine pianificata del progetto si è spostata rispetto alla baseline. 🔴 Rosso (positivo) = ritardo, 🟢 verde (negativo) = anticipo.
*   **Varianza Effort**: differenza in ore tra l'effort stimato attuale e quello della baseline.

Lo stato è **In Linea** se tempi ed effort non sono aumentati, **A Rischio** se lo slittamento è di al massimo 3 giorni oppure l'effort è cresciuto al massimo del 10%, altrimenti **In Ritardo**.

### La Lista delle Baseline
*   Ogni baseline mostra nome, data e numero di task fotografati.
*   Clicca su una baseline per vedere la sua varianza (**Tempi** e **Effort**); clicca di nuovo per chiuderla.
*   Clicca l'**icona cestino** per eliminarla (con conferma).

---

## 👥 Card Team

Una card rapida che mostra:
*   Lo **Sponsor** e il **Project Manager** (dal Charter).
*   Se ci sono membri sovraccarichi da oggi alla fine del mese, un badge **"N sovraccaricati"**.
*   Se Sponsor e Project Manager non sono impostati: "Vedi pagina Team per elenco completo".
*   Pulsante **"Gestisci"**: apre la pagina Team.

---

## 🔗 Link Rapidi

In fondo alla colonna destra:
*   **"Vai al Diagramma di Gantt"**: apre la vista Gantt.
*   **"Risorse & Carico"**: apre la pagina Risorse.

---

### 👉 Prossimo Passo
Ora che hai la governance completa del progetto, scopriamo la pianificazione delle risorse. Prossimo capitolo: **Risorse e Carichi di Lavoro**.
