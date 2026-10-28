# Capitolo 16: Controllo Modifiche e Branch 🔀

La pagina **Controllo Modifiche** ti aiuta a gestire in modo formale i cambiamenti di un progetto: le richieste di modifica, i branch (scenari alternativi) e il registro delle decisioni.

Lavora sul **progetto attivo** (scelto dal selettore progetto di Planner, Kanban, Gantt o Charter, o aprendo un progetto da **Progetti**). Senza progetti mostra *"Seleziona un progetto"*.

In alto trovi tre schede: **Richieste di Modifica**, **Branch** e **Registro Decisioni**.

> **⚠️ Attenzione:** In questa pagina i pulsanti di eliminazione agiscono **subito, senza chiedere conferma**.

---

## 📝 Richieste di Modifica

A sinistra c'è l'elenco delle richieste; a destra il modulo della richiesta selezionata.

### Creare una richiesta
1. Clicca **"Nuovo"**.
2. LibrePM crea una "Nuova richiesta di modifica" a tuo nome e la apre a destra.
3. Compila i campi:
    *   **Titolo**: cosa deve cambiare.
    *   **Stato**: Bozza, Inviata, In Revisione, Approvata, Rifiutata, Implementata.
    *   **Priorità**: Bassa, Media, Alta, Critica.
    *   **Ambito**: Pianificazione, Budget, Ambito, Risorse o Tutto.
    *   **Descrizione**: il dettaglio della modifica.
    *   **Motivazione**: perché è necessaria.
    *   **Impatto Previsto**: quali effetti avrà.

I campi di testo si salvano quando ne esci; stato, priorità e ambito appena li scegli.

Nell'elenco ogni richiesta mostra il titolo, un badge con lo stato e, sotto, priorità e ambito. Clicca una richiesta per riaprirla; il pulsante **"Elimina"** in alto nel modulo la cancella.

> **📌 Nota:** Lo stato lo cambi tu a mano: LibrePM non impone un flusso di approvazione né applica automaticamente la modifica al progetto.

---

## 🌿 Branch

I branch sono **scenari** che ti permettono di annotare alternative al piano principale (per esempio un'analisi "e se...").

### Creare un branch
Clicca **"Nuovo Branch"**: LibrePM crea un "Nuovo branch" di tipo **Scenario**, in stato **Attivo**.

Ogni branch appare come una card con:
*   Il **nome** e un badge con il **tipo** (Scenario, Riprevisione, Candidatura, Pivot, Spin-off, Evolutiva).
*   Un menu per cambiare lo **stato**: Attivo, Approvato, Rifiutato, Unito, Archiviato.
*   La data di creazione e, se presente, la descrizione e la **CR Collegata** (la richiesta di modifica associata).
*   Il pulsante **"Elimina"**.

> **📌 Nota:** Dall'interfaccia, per ora, di un branch puoi cambiare solo lo stato: nome, tipo, descrizione e collegamento a una richiesta di modifica non sono modificabili. Il branch è un registro dello scenario: non crea una copia separata dei task del progetto.

---

## 📒 Registro Decisioni

Tiene traccia delle decisioni prese nel progetto, con data.

1. Clicca **"Aggiungi"**: LibrePM crea una "Nuova decisione" a tuo nome, con la data di oggi.
2. Su ogni voce puoi scrivere:
    *   il **titolo** (prima riga);
    *   la **Decisione**: cosa è stato deciso;
    *   la **Motivazione**: perché.
3. I testi si salvano quando esci dal campo.

A destra di ogni voce vedi la data della decisione e l'**icona cestino** per eliminarla.

Così ti resta una traccia di **perché** il progetto si è evoluto in quel modo.

---

### 👉 Prossimo Passo
Quando i progetti diventano tanti, conviene raggrupparli. Prossimo capitolo: **Gestione Portfolio**.
