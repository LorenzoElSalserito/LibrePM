# Capitolo 12: Integrazioni e Interoperabilità 🔌

La pagina **Integrazioni** è il tuo hub per connettere LibrePM con il mondo esterno — calendari esterni, import/export dati e backup.

---

## 📅 Calendario Esterno (iCal)

LibrePM genera un **URL feed iCal** che ti permette di vedere le scadenze dei tuoi task in qualsiasi app calendario che supporta lo standard iCal/ICS.

### App Calendario Supportate
*   Google Calendar
*   Microsoft Outlook
*   Apple Calendar
*   Qualsiasi app che supporta feed iCal

### Come Configurarlo
1. Vai nella pagina **Integrazioni**.
2. Nella card **"Calendario Esterno (iCal)"**, vedrai un campo URL di sola lettura con il tuo link feed personale.
3. Clicca il pulsante **Copia** (icona appunti) per copiare l'URL.
4. Apri la tua app calendario e aggiungilo come "Iscriviti a Calendario" o "Aggiungi calendario tramite URL".
5. Le scadenze dei tuoi task appariranno nel tuo calendario esterno!

### Rigenerare il Token
Se hai bisogno di un nuovo URL (es. per motivi di sicurezza):
1. Clicca il pulsante **"Rigenera Token"** in fondo alla card.
2. Un nuovo URL viene generato. Il vecchio URL smette di funzionare immediatamente.
3. **Importante**: Dovrai aggiornare il link in tutti i calendari esterni dove l'hai aggiunto.

---

## 📤 Esportare i Dati

### Esporta JSON
Clicca il pulsante **"Esporta JSON"** (blu, con icona download) per esportare tutti i tuoi dati in formato JSON:
*   Nell'app desktop Electron: Si apre un dialogo di salvataggio per scegliere dove salvare.
*   Nella versione web: Il file si scarica direttamente.
*   Il file JSON contiene tutti i progetti, task, note e impostazioni.

### Esporta CSV (Per Progetto)
Clicca il pulsante **"Esporta CSV (Progetto)"** (grigio, con icona foglio di calcolo) per esportare i task del progetto corrente come foglio CSV:
*   Questo pulsante è **disabilitato** se nessun progetto è selezionato.
*   Il file CSV può essere aperto in Excel, Google Sheets o qualsiasi applicazione foglio di calcolo.

---

## 📥 Importare i Dati

### Importa JSON
Clicca il pulsante **"Importa JSON"** (rosso, con icona upload) per importare dati da un backup JSON precedente:
*   Nell'app desktop Electron: Si apre un selettore file per scegliere il file JSON.
*   Nella versione web: Si apre un browser file.

> **⚠️ Attenzione**: L'importazione JSON **sovrascriverà** i tuoi dati correnti. Assicurati di avere un backup prima di importare!

---

## 🔄 Disponibile anche nelle Impostazioni

L'URL del feed calendario e la rigenerazione del token sono disponibili anche nella pagina **Impostazioni** sotto "Calendario Esterno (iCal)". L'esportazione e importazione del database (formato .db) sono disponibili sotto "Backup e Sincronizzazione" nelle Impostazioni.

---

### 👉 Prossimo Passo
Infine, configuriamo LibrePM per funzionare esattamente come vuoi. Prossimo capitolo: **Impostazioni e Personalizzazione**.
