# Capitolo 15: Bandi e Stakeholder 📣

LibrePM ha due pagine dedicate a chi finanzia e a chi è coinvolto nel progetto:
*   **Bandi**: bandi di finanziamento, requisiti, obblighi e periodi di rendicontazione.
*   **Stakeholder**: registro e mappa degli stakeholder, sponsor, donatori e organizzazioni partner.

Come la pagina Finanza, entrambe lavorano sul **progetto attivo** (scelto dal selettore progetto di Planner, Kanban, Gantt o Charter, o aprendo un progetto da **Progetti**). Senza progetti mostrano *"Seleziona un progetto"*.

> **⚠️ Attenzione:** In queste pagine i pulsanti cestino eliminano l'elemento **subito, senza chiedere conferma**.

---

## 📣 La Pagina Bandi

In alto trovi tre schede: **Bandi**, **Obblighi** e **Rendicontazione**.

### Scheda Bandi
A sinistra c'è l'elenco **"Bandi e Finanziamenti"**; a destra il dettaglio del bando selezionato.

#### Creare un bando
1. Clicca **"Nuovo Bando"**.
2. LibrePM crea un bando "Nuovo bando" e apre subito il modulo di modifica.
3. Compila i campi (ogni campo si salva quando ne esci):
    *   **Titolo**
    *   **Stato**: Identificato, In Valutazione, In Preparazione, Inviato, Aggiudicato, Respinto, Chiuso.
    *   **Ente Emittente**
    *   **Scadenza** (data)
    *   **Budget Disponibile**
    *   **Descrizione**
    *   **Codice Riferimento**
    *   **URL**

Ogni bando nell'elenco mostra titolo, ente emittente, un badge colorato con lo stato e la scadenza.

#### Vedere e modificare
Nell'intestazione del dettaglio trovi:
*   **"Modifica"** / **"Vista"**: passa dal modulo di modifica alla vista di sola lettura e viceversa.
*   **"Elimina"**: elimina il bando.

#### Requisiti
Sotto il dettaglio c'è la sezione **Requisiti**, una checklist per verificare se puoi partecipare:
*   Clicca **"Aggiungi"** per inserire un requisito "Nuovo requisito" di tipo **Ammissibilità**.
*   Spunta la casella per segnarlo come soddisfatto (il testo diventa barrato).
*   Il contatore **Soddisfatti: x/y** mostra a che punto sei.

> **📌 Nota:** Dall'interfaccia, per ora, i requisiti si possono solo aggiungere e spuntare: testo e tipo (Documento, Ammissibilità, Budget, Partner, Tecnico) non sono modificabili e i requisiti non si possono eliminare.

### Scheda Obblighi
Qui registri gli impegni che derivano dal bando o dal contratto. Clicca **"Aggiungi"** per creare un "Nuovo obbligo", poi compila la riga in tabella:
*   **Titolo**
*   **Tipo**: Contrattuale, Bando, Regolamentare, Rendicontazione.
*   **Scadenza** (data)
*   **Stato**: In Sospeso, In Corso, Completato, Scaduto (lo imposti tu: non cambia da solo alla scadenza).

L'icona cestino elimina l'obbligo.

### Scheda Rendicontazione
Elenca i **Periodi di Rendicontazione** come card. Clicca **"Aggiungi"** per creare un "Nuovo periodo" che va da oggi a 90 giorni da oggi. Su ogni card puoi:
*   Rinominare il periodo (clicca sul nome).
*   Cambiare lo stato: In Arrivo, Aperto, Inviato, Accettato.
*   Eliminarlo con **"Elimina"**.

> **📌 Nota:** Le date di inizio e fine del periodo non sono modificabili dall'interfaccia.

---

## 🧭 La Pagina Stakeholder

In alto trovi quattro schede: **Mappa Stakeholder**, **Sponsor**, **Donatori** e **Partner**. In ogni scheda il pulsante **"Aggiungi"** crea un nuovo elemento che poi modifichi direttamente nelle celle della tabella (i testi si salvano quando esci dal campo, i menu a tendina appena scegli).

### Mappa Stakeholder
Contiene la **Matrice Influenza / Interesse** e il **Registro Stakeholder**.

#### La matrice
È un grafico dove ogni stakeholder appare come un cerchio con le prime due lettere del nome:
*   Asse verticale: **Influenza** (Basso, Medio, Alto, Critico, dal basso verso l'alto).
*   Asse orizzontale: **Interesse** (Basso, Medio, Alto, da sinistra a destra).
*   Il colore del cerchio dipende dall'influenza (verde, azzurro, giallo, rosso).
*   Passando il mouse vedi nome, influenza e interesse.

I quattro quadranti ti suggeriscono come trattarli:
*   **Gestire da Vicino** (in alto a destra: alta influenza, alto interesse)
*   **Mantenere Soddisfatti** (in alto a sinistra: alta influenza, basso interesse)
*   **Mantenere Informati** (in basso a destra: bassa influenza, alto interesse)
*   **Monitorare** (in basso a sinistra: bassa influenza, basso interesse)

#### Il registro
Clicca **"Aggiungi"** per creare un "Nuovo stakeholder". Colonne: **Nome**, **Organizzazione**, **Ruolo**, **Influenza**, **Interesse** e **Canale** (Email, Telefono, Riunione, Portale). Cambiando influenza o interesse, il cerchio si sposta nella matrice.

### Sponsor
Per ogni sponsor: **Nome**, **Organizzazione**, **Email**, **Contatto**, **Tipo** (Aziendale, Individuale, Fondazione, Governativo, Altro).

Sotto ogni sponsor c'è la sezione **Impegni**: clicca il suo **"Aggiungi"** per registrare un impegno con **Descrizione**, **Importo**, **Valuta**, **Stato** (Proposto, Concordato, Attivo, Completato, Ritirato) e **Data Accordo**.

### Donatori
Per ogni donatore: **Nome**, **Organizzazione**, **Email**, **Telefono**, **Tipo** (Individuale, Fondazione, Aziendale, Governativo, Altro).

Sotto ogni donatore c'è la sezione **Donazioni**: con **"Aggiungi"** registri una donazione con **Importo**, **Valuta**, **Data**, **Note** e la casella **Vincolato** per i fondi a destinazione vincolata.

### Partner
La tabella **Organizzazioni Partner** serve per i progetti in consorzio. Colonne: **Nome**, **Paese**, **Ruolo** (Coordinatore, Partner, Associato, Subappaltatore), **Contatto** e **Quota Budget**.

---

### 👉 Prossimo Passo
I progetti cambiano: serve un modo ordinato per gestire le modifiche. Prossimo capitolo: **Controllo Modifiche e Branch**.
