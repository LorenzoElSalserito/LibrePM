# Capitolo 14: Finanza di Progetto 💰

La pagina **Finanza** raccoglie i numeri economici di un progetto: le versioni del budget con le loro voci, le fonti di finanziamento e un riepilogo di analisi.

---

## 📂 Su quale progetto lavori?

La pagina **Finanza** non ha un selettore proprio: mostra sempre il **progetto attivo**, cioè quello scelto con il selettore progetto di Planner, Kanban, Gantt o Charter, oppure aprendo un progetto dalla pagina **Progetti**. All'avvio il progetto attivo è il primo della lista.

Se non c'è nessun progetto, la pagina mostra il messaggio *"Seleziona un progetto"*.

In alto trovi tre schede: **Budget**, **Finanziamenti** e **Analisi**.

---

## 🧮 Scheda Budget

### Creare un budget
1. Clicca **"Nuovo Budget"**.
2. LibrePM crea un budget chiamato "Budget v1", "Budget v2"... (il numero segue quanti budget esistono già) in stato **DRAFT** (bozza) e lo seleziona.

Il menu a tendina accanto al pulsante elenca tutti i budget del progetto nel formato *nome (versione) — stato*. Scegline uno per vederne le voci.

### Le voci di budget
Clicca **"Aggiungi Voce"** per inserire una riga chiamata "Nuova voce di budget" nella categoria **Varie**. Ogni riga della tabella si modifica direttamente nella cella:

| Colonna | Cosa contiene |
|---------|---------------|
| **Nome** | Il nome della voce |
| **Categoria** | Personale, Fornitori, Software, Viaggi, Eventi, Attrezzature, Costi Generali, Varie |
| **Pianificato** | L'importo previsto a budget |
| **Impegnato** | L'importo già impegnato (ordini, contratti) |
| **Effettivo** | Quanto hai realmente speso |
| **Previsione** | La tua stima a finire |

Le modifiche ai testi e agli importi vengono salvate quando esci dalla cella; la categoria si salva appena la scegli. L'**icona cestino** a fine riga elimina la voce subito, senza conferma.

La riga **Totale** in fondo somma le quattro colonne di importi.

> **💡 Nota:** I costi effettivi si registrano scrivendo l'importo nella colonna **Effettivo** della voce. Non esiste (per ora) un registro separato delle singole spese nell'interfaccia.

### Approvare un budget
Quando un budget è in stato **DRAFT** compare il pulsante verde **"Approva"**. Cliccandolo il budget passa in stato **APPROVED** e viene registrato chi lo ha approvato.

> **⚠️ Attenzione:** L'approvazione non blocca il budget: le voci restano modificabili anche dopo. Se vuoi conservare una "fotografia", crea un nuovo budget per le modifiche successive.

---

## 💶 Scheda Finanziamenti

Clicca **"Aggiungi Fonte di Finanziamento"** per creare una riga "Nuova fonte di finanziamento" di tipo **Interno**. Anche qui ogni cella si modifica direttamente:

| Colonna | Cosa contiene |
|---------|---------------|
| **Nome** | Il nome della fonte |
| **Tipo** | Interno, Bando, Sponsor, Donazione, Co-finanziamento |
| **Importo Totale** | L'importo messo a disposizione |
| **Valuta** | Codice valuta (predefinito EUR) |
| **Stato** | Attivo, In Sospeso, Esaurito, Annullato |
| **Nome Contatto** / **Email Contatto** | Il riferimento presso il finanziatore |
| **Vincolato** | Casella da spuntare se i fondi hanno un vincolo d'uso |

Quando spunti **Vincolato** appare sotto la riga un campo *"Descrizione restrizione..."* (con l'icona lucchetto) dove spiegare il vincolo.

L'**icona cestino** elimina la fonte subito, senza conferma. Se non ci sono fonti vedrai *"Nessuna fonte di finanziamento"*.

---

## 📈 Scheda Analisi

Se il progetto non ha ancora un budget, la scheda mostra *"Crea un budget per visualizzare le analisi"*.

Altrimenti le analisi si basano sul budget **approvato**; se non ce n'è nessuno, sul budget con la versione più alta. Trovi:

*   **Pianificato**: la somma degli importi pianificati delle voci.
*   **Effettivo**: la somma degli importi effettivi.
*   **Varianza**: Pianificato meno Effettivo, con la percentuale sotto. Verde se sei sotto budget, rosso se lo hai superato.
*   **Copertura Finanziaria**: quanta parte del pianificato è coperta dalla somma degli importi delle fonti di finanziamento. Sotto il 100% compare in giallo e, se manca qualcosa, appare il **Gap** (l'importo scoperto) in rosso.
*   **Tasso di Consumo**: una barra con la percentuale Effettivo/Pianificato. Verde fino al 70%, gialla oltre il 70%, rossa oltre il 90%.
*   **Effettivo per Categoria**: la spesa effettiva raggruppata per categoria (appare solo se ci sono voci).

---

## 🚀 Per Iniziare

1. Scegli il progetto attivo (per esempio dal selettore del Planner).
2. Vai alla pagina **Finanza** → scheda **Budget** → **"Nuovo Budget"**.
3. Aggiungi le voci con **"Aggiungi Voce"** e compila gli importi pianificati.
4. Nella scheda **Finanziamenti** registra chi finanzia il progetto.
5. Man mano che spendi, aggiorna le colonne **Impegnato**, **Effettivo** e **Previsione**.
6. Controlla la scheda **Analisi** per vedere consumo, varianza e copertura.

---

### 👉 Prossimo Passo
Molti progetti vivono di bandi e di relazioni con sponsor e partner. Prossimo capitolo: **Bandi e Stakeholder**.
