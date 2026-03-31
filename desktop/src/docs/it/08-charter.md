# Capitolo 8: Dashboard Esecutiva e Charter 📊

La pagina **Charter** (anche chiamata Dashboard Esecutiva) è la vista gestionale più potente di LibrePM. Riunisce governance di progetto, obiettivi, rischi, deliverable e monitoraggio delle performance in un unico posto.

---

## 📊 La Riga di Statistiche

In cima alla pagina, quattro card riassuntive danno una panoramica istantanea:

| Card | Cosa mostra |
|------|-------------|
| **Completamento** | Percentuale dei task completati nel progetto |
| **Scaduti** | Numero di task oltre la loro scadenza |
| **Deliverable** | Deliverable completati sul totale (es. "3/7") |
| **Rischi Alti** | Numero di rischi classificati come Alti o Critici |

Un **badge di stato** appare in alto mostrando lo stato generale del progetto:
*   🟢 **ON_TRACK**: Tutto procede bene.
*   🟡 **AT_RISK**: Alcuni indicatori richiedono attenzione.
*   🔴 **DELAYED**: Il progetto è in ritardo.
*   ⚫ **BLOCKED**: Bloccanti critici impediscono il progresso.

---

## 📋 Charter del Progetto

Il Charter è il documento fondante del tuo progetto. Definisce:

### Card Info Charter
*   **Sponsor**: Chi sponsorizza/finanzia il progetto (campo testo).
*   **Project Manager**: Chi guida il progetto (campo testo).
*   **Obiettivi**: Cosa mira a raggiungere il progetto (area di testo).
*   Clicca **"Modifica"** per modificare questi campi, poi **"Salva"** per salvare le modifiche.

### Sezioni Charter (Markdown)
Sotto la card info, troverai sezioni espandibili con editing Markdown ricco:
*   **Goals / Problem Statement**: Quale problema risolve questo progetto?
*   **Business Case**: Perché vale la pena fare questo progetto?

Ogni sezione ha un pulsante toggle **Modifica/Salva**. In modifica appare un editor Markdown completo. Una volta salvato, il contenuto renderizzato viene visualizzato.

---

## 🎯 OKR (Objectives and Key Results)

Gli OKR ti aiutano a tracciare obiettivi strategici e risultati misurabili.

### Creare un OKR
1. Clicca il pulsante **"+"** nell'intestazione della sezione OKR.
2. Digita il nome dell'**Obiettivo** (es. "Aumentare il Coinvolgimento Utenti").
3. Clicca **"Aggiungi"**.

### Aggiungere Key Results
Ogni OKR può avere più Key Results:
1. Espandi un OKR cliccandoci sopra.
2. Clicca il pulsante **"+"** (plus-circle).
3. Compila:
    *   **Nome Key Result** (es. "Raggiungere 1000 utenti attivi al giorno").
    *   **Valore target** (es. 1000).
    *   **Unità** (es. "utenti", "percentuale", "fatturato").
4. Clicca **"Aggiungi"**.

### Monitorare il Progresso
*   Ogni Key Result mostra un input **valore corrente** e un **valore target**.
*   La barra di progresso si aggiorna automaticamente in base al rapporto corrente/target.
*   I colori indicano il progresso: 🟢 verde (>=100%), 🔵 blu (>=50%), 🟡 giallo (<50%).
*   Il progresso complessivo dell'OKR è la media di tutti i suoi Key Results.

### Eliminare
*   Clicca l'**icona cestino** accanto a un OKR per eliminarlo.
*   Clicca l'icona **✕** accanto a un Key Result per rimuoverlo.

---

## ⚠️ Registro dei Rischi

Traccia e gestisci i rischi di progetto con valutazione di probabilità e impatto.

### Aggiungere un Rischio
1. Clicca il pulsante **"+"** nell'intestazione della sezione Rischi.
2. Compila:
    *   **Descrizione del rischio** (es. "Lo sviluppatore chiave potrebbe lasciare a metà progetto").
    *   **Probabilità**: Low, Medium, High o Critical.
    *   **Impatto**: Low, Medium, High o Critical.
    *   **Strategia di mitigazione** (es. "Formare il team trasversalmente, documentare tutto").
3. Clicca **"Aggiungi"**.

### Visualizzare i Rischi
*   Ogni rischio mostra descrizione, badge probabilità e badge impatto.
*   Clicca su un rischio per **espandere** e vedere la strategia di mitigazione.
*   Clicca l'**icona cestino** per eliminare un rischio.
*   Il badge nell'intestazione mostra il numero totale dei rischi.

---

## 📦 Deliverable Chiave

Traccia cosa il tuo progetto deve produrre, con monitoraggio di progresso e rischio.

### Aggiungere un Deliverable
1. Clicca il pulsante **"+"** nell'intestazione della sezione Deliverable.
2. Compila:
    *   **Nome deliverable** (es. "Mockup Interfaccia Utente").
    *   **Data di scadenza**.
3. Clicca **"Aggiungi"**.

### Monitorare i Deliverable
Ogni deliverable nella lista mostra:
*   **Nome** (barrato se il progresso raggiunge il 100%).
*   **Dropdown Stato Rischio**: Impostabile su OK, At Risk o Blocked.
*   **Input Progresso** (0-100): Digita la percentuale di completamento.
*   **Barra di progresso**: Indicatore visivo con codifica colore.
*   **Pulsante ✕**: Elimina il deliverable.

---

## 📈 Baseline e Varianza

Le Baseline ti permettono di fare un "fotografia" del piano di progetto in un punto nel tempo, poi monitorare come la realtà devia dal piano.

### Creare una Baseline
1. Clicca il pulsante **"+"** nell'intestazione della sezione Baseline.
2. Inserisci un **nome** (es. "Piano Sprint 1", "Timeline Originale").
3. Clicca **"Crea"**.

LibrePM salva un'istantanea di tutti i task correnti (date, sforzo, stato).

### Visualizzare la Varianza
Il **Riepilogo Varianza Recente** appare in cima alla sezione:
*   **Nome baseline** usato come confronto.
*   **Badge Stato Progetto** (ON_TRACK, AT_RISK, DELAYED).
*   **Varianza Schedule**: Numero di giorni in anticipo o ritardo. Rosso = ritardo, Verde = anticipo.
*   **Varianza Effort**: Differenza in ore di lavoro.

Clicca su qualsiasi baseline nella lista per espandere la sua varianza dettagliata.

---

## 👥 Riepilogo Team

Una card rapida che mostra:
*   Lo **Sponsor** e il **Project Manager** del progetto (dal Charter).
*   **Utenti sovraccaricati**: Un badge che mostra quanti membri del team sono sovraccarichi.
*   Pulsante **"Gestisci"**: Naviga alla pagina Team.

---

## 🔗 Link Rapidi

In fondo alla pagina, pulsanti di navigazione rapida:
*   **Vai al Diagramma Gantt**: Salta alla vista timeline Gantt.
*   **Risorse e Carichi**: Salta alla pagina allocazione risorse.

---

### 👉 Prossimo Passo
Ora che hai la governance completa del progetto, scopriamo la pianificazione delle risorse. Prossimo capitolo: **Risorse e Carichi di Lavoro**.
