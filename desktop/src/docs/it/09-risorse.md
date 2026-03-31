# Capitolo 9: Risorse e Carichi di Lavoro 📊

La pagina **Risorse** ti dà una vista panoramica di come il tempo del tuo team è allocato. Ti aiuta a individuare membri del team sovraccaricati, trovare capacità disponibile e bilanciare i carichi di lavoro nel tuo progetto.

---

## 🗓️ La Griglia delle Risorse

La vista principale è una **tabella a griglia** con:
*   **Righe**: Una riga per membro del team, con il loro avatar (cerchio con iniziali) e nome (fisso a sinistra).
*   **Colonne**: Una colonna per giorno nell'intervallo selezionato, mostrando l'abbreviazione del giorno della settimana e il numero del giorno.
*   **Celle**: Ogni cella mostra il numero di ore allocate a quel membro in quel giorno.

### Codifica Colori
Le celle sono codificate per colore per mostrare istantaneamente i livelli di carico:
*   🟢 **Verde (Ottimale)**: Fino a 4 ore allocate — carico sano.
*   🟡 **Giallo (Pieno)**: Da 4 a 8 ore — a capacità.
*   🔴 **Rosso (Sovraccarico)**: Più di 8 ore — sovraccaricato! Azione necessaria.

Una **legenda** sotto la tabella spiega i colori.

Passando il mouse su una cella appare un tooltip con le ore stimate esatte.

---

## 🎛️ Controlli

La barra strumenti in alto fornisce:

*   **Selettore Progetto**: Scegli di quale progetto visualizzare le risorse, o seleziona "Tutti i progetti".
*   **Data Inizio**: Scegli il primo giorno dell'intervallo da visualizzare.
*   **Durata Intervallo**: Scegli quanti giorni mostrare:
    *   **7 giorni**: Focus sulla settimana corrente.
    *   **14 giorni**: Vista sprint di due settimane.
    *   **21 giorni**: Prospettiva di tre settimane.
    *   **30 giorni**: Panoramica del mese intero.

---

## 📋 Dettagli Task per Cella

Clicca su qualsiasi cella nella griglia per vedere quali task sono assegnati a quel membro in quel giorno:

Si apre una **modale** che mostra:
*   Il **nome del membro** e il **giorno selezionato**.
*   Una lista di task assegnati, ognuno che mostra:
    *   Titolo del task.
    *   Tempo stimato (o "Non impostato" se nessuna stima).
    *   Pulsante **Rimuovi assegnazione** (icona persona con X): Disassegna questo task da questo membro.
*   Se nessun task è assegnato, appare un messaggio "Nessun task".

---

## 👤 Gestione Membro

Clicca sul **nome di un membro** nella colonna sinistra per aprire una modale di gestione membro:
*   Mostra i dettagli del membro (ID).
*   Pulsante **"Rimuovi dal Progetto"**: Rimuovi questo membro dal progetto completamente.

---

### 👉 Prossimo Passo
Ora che puoi gestire i carichi di lavoro, scopriamo come avviare progetti da template. Prossimo capitolo: **Template e Gallery**.
