# Capitolo 18: Privacy e Conformità 🛡️

LibrePM conserva i dati sul tuo computer (o sul database condiviso configurato dall'amministratore) e offre alcuni strumenti per gestire le richieste sui dati personali.

---

## 🔐 I Tuoi Dati (Impostazioni)

Vai in **Impostazioni** e scorri fino alla card **"I Tuoi Dati"** in fondo alla pagina. Trovi due pulsanti:

*   **"Esporta i Miei Dati"**: registra una richiesta di esportazione dei tuoi dati personali. Vedrai il messaggio *"Esportazione dati richiesta"*.
*   **"Richiedi Cancellazione Dati"**: dopo una conferma (*"Sei sicuro di voler richiedere la cancellazione dei dati? Questa azione non può essere annullata."*) registra una richiesta di cancellazione. Vedrai il messaggio *"Richiesta di cancellazione inviata"*.

> **📌 Importante:** Questi pulsanti **registrano una richiesta** (in stato "in attesa") che va poi gestita da chi amministra l'installazione: non generano un file e non cancellano nulla immediatamente. Per ottenere subito una copia dei tuoi dati usa invece l'esportazione del database descritta nei capitoli 5, 12 e 13.

---

## 🗃️ Categorie di Dati e Conservazione

Il database di LibrePM contiene già cinque categorie di dati predefinite, con un livello di sensibilità e un periodo di conservazione suggerito:

| Categoria | Sensibilità | Conservazione suggerita |
|-----------|-------------|--------------------------|
| Dati Personali | Sensibile | 3 anni |
| Dati Finanziari | Riservato | 10 anni |
| Dati di Progetto | Normale | nessuna scadenza |
| Evidenze | Sensibile | 10 anni |
| Log di Audit | Normale | 2 anni |

> **📌 Nota:** Categorie e politiche di conservazione sono configurazioni del backend: al momento non sono visibili né modificabili dall'interfaccia e LibrePM non cancella automaticamente i dati alla scadenza di questi periodi.

---

## ♿ Accessibilità

Alcuni accorgimenti già presenti nell'interfaccia:
*   **Movimento ridotto**: se nel sistema operativo hai attivato la riduzione del movimento (`prefers-reduced-motion`), LibrePM riduce al minimo animazioni e transizioni.
*   **Navigazione laterale accessibile**: le voci della barra laterale sono pulsanti con etichetta, raggiungibili con il tasto Tab; la pagina attiva è segnalata anche agli screen reader. Con la barra compressa, il nome della pagina appare come suggerimento al passaggio del mouse.
*   **Scorciatoie da tastiera**: la Command Palette (**Ctrl+K**) e le altre scorciatoie descritte nel Capitolo 1 permettono di muoversi senza mouse.
*   **Tema chiaro e scuro**: scegli quello più leggibile per te in **Impostazioni** (Capitolo 13).

---

### 👉 Prossimo Passo
Hai visto tutti i moduli di governance, dalla finanza alla conformità. Restano gli strumenti per lavorare insieme: prossimo capitolo, **Messaggi di Progetto**.
