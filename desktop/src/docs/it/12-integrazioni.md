# Capitolo 12: Integrazioni e Interoperabilità 🔌

La pagina **Integrazioni** (titolo interno **"Integrazioni & Dati"**) è il tuo hub per collegare LibrePM con il mondo esterno: feed calendario, import/export dei dati e profili di mappatura.

Il pulsante **Aggiorna** (🔄) nell'intestazione ricarica il token del feed calendario.

---

## 📅 Calendario Esterno (iCal)

LibrePM può generare un **feed iCal** (standard ICS) per vedere i tuoi task in un'app calendario come Microsoft Outlook, Apple Calendar o qualsiasi app che supporti l'iscrizione a un calendario tramite URL.

Nella card **"Calendario Esterno (iCal)"** di questa pagina trovi:
*   Un campo di sola lettura con l'URL del feed e il pulsante **Copia** (icona appunti).
*   Il pulsante **"Rigenera Token"**: crea un nuovo token (con conferma). Il vecchio link smette di funzionare e va aggiornato in tutti i calendari dove l'hai aggiunto.

> **⚠️ Stato attuale:** il link mostrato in questa pagina punta a un indirizzo che il backend non espone, quindi le app calendario non riescono a leggerlo. Usa invece il link della card **"Calendario Esterno (iCal)"** nelle **Impostazioni** (Capitolo 13), che funziona dopo aver cliccato **"Rigenera Token"**.

---

## 📤 Esportare i Dati

Nella card **"Importa / Esporta Dati"** trovi due gruppi di pulsanti. Nell'app desktop ogni pulsante apre la finestra di sistema per scegliere il file; durante l'operazione compare un indicatore di avanzamento e al termine un messaggio con l'esito e il percorso del file.

### Workspace (JSON) → Esporta JSON
Il pulsante **"Esporta JSON"** (blu) salva in un file `LibrePM-export-….json` **tutti i progetti del profilo corrente** con task, sotto-task, stato, priorità, date, tag e checklist. Funziona sia con il database locale sia con un **database condiviso** (MariaDB). Lo stesso export è disponibile dal menu del profilo, voce **"Esporta dati (JSON)"**.

### Task del progetto (CSV) → Esporta CSV (Progetto)
Il pulsante **"Esporta CSV (Progetto)"** (grigio, icona foglio di calcolo) esporta i task del **progetto corrente** in un file CSV, apribile con Excel, LibreOffice o Google Sheets.
*   Il pulsante è **disabilitato** se nessun progetto è selezionato.
*   Le descrizioni su più righe vengono racchiuse tra virgolette, così il file si può reimportare senza perdite.

---

## 📥 Importare i Dati

### Importa JSON
Il pulsante **"Importa JSON"** apre un file creato con **"Esporta JSON"** e **aggiunge** i progetti che contiene come **nuovi progetti** del profilo corrente (con i loro task, sotto-task, tag e checklist). I dati già presenti **non vengono modificati né cancellati**: importare due volte lo stesso file crea due copie. Se il file non è valido non viene importato nulla. Al termine la pagina si ricarica per mostrare i nuovi progetti.
*   I tag vengono riutilizzati se ne hai già uno con lo stesso nome. Se il nome è già usato da **un altro profilo** il tag non viene applicato e un avviso te lo segnala.
*   Un file di **backup cifrato** (`.enc.json`) non si importa da qui: usa **"Importa backup cifrato"** nelle Impostazioni (Capitolo 13).

### Importa CSV (Progetto)
Il pulsante **"Importa CSV (Progetto)"** aggiunge al **progetto corrente** i task di un file CSV, per esempio uno creato con **"Esporta CSV (Progetto)"**. Colonne riconosciute: `title` (obbligatoria), `description`, `status`, `priority`, `deadline` (AAAA-MM-GG), `estimatedEffort`. Le righe senza titolo vengono saltate e segnalate.

---

## 🗂️ Profili di Mappatura Import

L'ultima card, **"Profili di Mappatura Import"**, elenca i profili di mappatura colonne salvati per gli import CSV, con **Nome**, **Tipo Entità** e data di creazione (**Creato**). Puoi eliminare un profilo con l'icona cestino (con conferma).

Se non ci sono profili vedrai *"Nessun profilo di mappatura salvato."* L'app al momento non offre una procedura per crearne di nuovi da questa pagina.

---

## 🔄 Disponibile anche nelle Impostazioni

Il feed calendario, l'export/import del file di database (.db) e il **backup cifrato** (export e import) si trovano in **Impostazioni → Backup e Sincronizzazione** (Capitolo 13). Se usi un **database condiviso** (MariaDB), vedi il Capitolo 21.

---

### 👉 Prossimo Passo
Infine, configuriamo LibrePM per funzionare esattamente come vuoi. Prossimo capitolo: **Impostazioni e Personalizzazione**.
