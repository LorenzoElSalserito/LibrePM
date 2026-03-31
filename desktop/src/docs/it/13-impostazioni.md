# Capitolo 13: Impostazioni e Personalizzazione ⚙️

La pagina **Impostazioni** ti permette di personalizzare LibrePM secondo le tue preferenze, gestire i backup, configurare i feed calendario e segnalare bug.

---

## 🎨 Aspetto

### Tema
Scegli tra due temi visivi:
*   **Light (Chiaro)**: Interfaccia pulita e luminosa.
*   **Dark (Scuro)**: Interfaccia più scura, più riposante per gli occhi in condizioni di poca luce.

Il cambiamento si applica **immediatamente** — puoi vedere l'anteprima prima di salvare.

### Lingua
Passa tra:
*   **Italiano**: Interfaccia completamente in italiano.
*   **English (Inglese)**: Interfaccia completamente in inglese.

Tutti gli elementi dell'interfaccia si aggiornano istantaneamente.

### Salvare
*   Clicca **"Salva Modifiche"** (pulsante verde) per rendere permanenti le tue preferenze.
*   Il pulsante appare verde quando ci sono modifiche non salvate, e grigio quando tutto è salvato.
*   Clicca **"Ripristina Predefiniti"** per resettare tutte le impostazioni ai valori originali (con conferma).

---

## 📅 Calendario Esterno (iCal)

Questa sezione è disponibile anche nella pagina Integrazioni (Capitolo 12):

*   **URL iCal**: Un campo di sola lettura che mostra il tuo URL feed calendario personale.
*   Pulsante **Copia**: Copia l'URL negli appunti.
*   Pulsante **"Rigenera Token"** (bordo rosso): Crea un nuovo URL, invalidando quello vecchio.

---

## 🗄️ Database Esterno

> **📌 Prossimamente:** Questa sezione è attualmente un segnaposto per funzionalità future.

Quando disponibile, permetterà di connettersi a database esterni:
*   Selettore **Tipo Database** (attualmente disabilitato):
    *   SQLite (Locale) — predefinito corrente.
    *   PostgreSQL (Prossimamente).
    *   MySQL (Prossimamente).
*   Input **Host** (attualmente disabilitato).
*   Pulsante **"Connetti a DB Esterno"** (attualmente disabilitato).

Un avviso info mostra: *"Stai attualmente usando il database SQLite locale."*

---

## 💾 Backup e Sincronizzazione

### Backup Automatico
Un interruttore toggle per abilitare o disabilitare i backup automatici.

### Backup Manuale
*   Pulsante **"Esporta Database (.db)"** (bordo blu): Esporta il file database SQLite completo.
    *   In Electron: Apre un dialogo di salvataggio.
    *   In Web: Scarica il file.
    *   Questo è il backup più completo — contiene tutto.

*   Pulsante **"Importa Database (.db)"** (bordo rosso): Importa un file database precedentemente esportato.
    *   In Electron: Apre un selettore file.
    *   In Web: Apre un browser file.
    *   **Attenzione**: Questo sostituisce tutti i tuoi dati correnti!

---

## 🐛 Segnalazione Bug

Trovato un bug? Aiuta a migliorare LibrePM!
*   Clicca **"Segnala un Bug"** (pulsante giallo con icona busta).
*   Questo apre il tuo client email con un template pre-compilato indirizzato al team LibrePM.
*   Descrivi il bug e invia l'email.

---

## ℹ️ Informazioni

Una card in fondo che mostra:
*   **Versione**: Numero di versione corrente di LibrePM.
*   **Copyright**: © Lorenzo DM 2026.
*   **Licenza**: AGPLv3.
*   **Sito Web**: Link a https://www.lorenzodm.it
*   **Piattaforma**: Desktop (Electron) o Web.
*   **Nota supporto**: Un invito a supportare lo sviluppo di LibrePM.

---

### 🎉 Ce l'hai fatta!
Ora conosci ogni funzionalità di LibrePM dentro e fuori. Sei pronto per organizzare la tua vita e il tuo lavoro come un vero professionista. Buon divertimento nel tuo laboratorio digitale! 🚀
