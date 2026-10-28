# Chapter 12: Integrations and Interoperability 🔌

The **Integrations** page (inner title **"Integrations & Data"**) is your hub for connecting LibrePM with the outside world: calendar feed, data import/export and mapping profiles.

The **Refresh** button (🔄) in the header reloads the calendar feed token.

---

## 📅 External Calendar (iCal)

LibrePM can generate an **iCal feed** (ICS standard) so you can see your tasks in a calendar app such as Microsoft Outlook, Apple Calendar or any app that can subscribe to a calendar by URL.

In the **"External Calendar (iCal)"** card on this page you'll find:
*   A read-only field with the feed URL and a **Copy** button (clipboard icon).
*   The **"Regenerate Token"** button: creates a new token (after confirmation). The old link stops working and must be updated in every calendar where you added it.

> **⚠️ Current status:** the link shown on this page points to an address the backend does not serve, so calendar apps cannot read it. Use the link in the **"External Calendar (iCal)"** card in **Settings** instead (Chapter 13), which works after you click **"Regenerate Token"**.

---

## 📤 Exporting Data

The **"Import / Export Data"** card has two groups of buttons. In the desktop app each button opens the system dialog to choose the file; a progress indicator is shown while it runs, followed by a message with the result and the file path.

### Workspace (JSON) → Export JSON
The **"Export JSON"** button (blue) saves a `LibrePM-export-….json` file with **all projects of the current profile**, including tasks, sub-tasks, status, priority, dates, tags and checklists. It works with both the local database and a **shared database** (MariaDB). The same export is available from the profile menu, **"Export data (JSON)"**.

### Project tasks (CSV) → Export CSV (Project)
The **"Export CSV (Project)"** button (grey, spreadsheet icon) exports the tasks of the **current project** to a CSV file you can open in Excel, LibreOffice or Google Sheets.
*   The button is **disabled** when no project is selected.
*   Multi-line descriptions are quoted, so the file can be imported back without losses.

---

## 📥 Importing Data

### Import JSON
The **"Import JSON"** button opens a file created with **"Export JSON"** and **adds** the projects it contains as **new projects** of the current profile (with their tasks, sub-tasks, tags and checklists). Existing data is **not changed or deleted**: importing the same file twice creates two copies. If the file is invalid nothing is imported. When it finishes the page reloads to show the new projects.
*   Tags are reused when you already have one with the same name. If the name is already used by **another profile** the tag is not applied and a warning tells you.
*   An **encrypted backup** (`.enc.json`) cannot be imported here: use **"Import encrypted backup"** in Settings (Chapter 13).

### Import CSV (Project)
The **"Import CSV (Project)"** button adds the tasks of a CSV file, for example one created with **"Export CSV (Project)"**, to the **current project**. Recognised columns: `title` (required), `description`, `status`, `priority`, `deadline` (YYYY-MM-DD), `estimatedEffort`. Rows without a title are skipped and reported.

---

## 🗂️ Import Mapping Profiles

The last card, **"Import Mapping Profiles"**, lists the saved column mapping profiles for CSV imports, with **Name**, **Entity Type** and creation date (**Created**). You can delete a profile with the trash icon (after confirmation).

If there are no profiles you'll see *"No mapping profiles saved yet."* The app currently offers no way to create new profiles from this page.

---

## 🔄 Also Available in Settings

The calendar feed, database file (.db) export/import and the **encrypted backup** (export and import) are in **Settings → Backup and Synchronization** (Chapter 13). If you use a **shared database** (MariaDB), see Chapter 21.

---

### 👉 Next Step
Finally, let's configure LibrePM to work exactly the way you want. Next chapter: **Settings and Personalization**.
