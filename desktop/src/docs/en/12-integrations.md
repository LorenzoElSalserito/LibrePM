# Chapter 12: Integrations & Interoperability 🔌

The **Integrations** page is your hub for connecting LibrePM with the outside world — external calendars, data import/export, and backup.

---

## 📅 External Calendar (iCal)

LibrePM generates an **iCal feed URL** that lets you see your task deadlines in any calendar app that supports the iCal/ICS standard.

### Supported Calendar Apps
*   Google Calendar
*   Microsoft Outlook
*   Apple Calendar
*   Any app supporting iCal feeds

### How to Set Up
1. Go to the **Integrations** page.
2. In the **"External Calendar (iCal)"** card, you'll see a read-only URL field with your personal feed link.
3. Click the **Copy** button (clipboard icon) to copy the URL.
4. Open your calendar app and add it as a "Subscribe to Calendar" or "Add calendar by URL".
5. Your task deadlines will appear in your external calendar!

### Regenerating the Token
If you need a new URL (e.g., for security reasons):
1. Click the **"Regenerate Token"** button at the bottom of the card.
2. A new URL is generated. The old URL stops working immediately.
3. **Important**: You'll need to update the link in any external calendars where you added it.

---

## 📤 Exporting Data

### Export JSON
Click the **"Export JSON"** button (blue, with download icon) to export all your data in JSON format:
*   In the Electron desktop app: A save dialog opens to choose where to save.
*   In the web version: The file downloads directly.
*   The JSON file contains all projects, tasks, notes, and settings.

### Export CSV (Per Project)
Click the **"Export CSV (Project)"** button (gray, with spreadsheet icon) to export the current project's tasks as a CSV spreadsheet:
*   This button is **disabled** if no project is currently selected.
*   The CSV file can be opened in Excel, Google Sheets, or any spreadsheet application.

---

## 📥 Importing Data

### Import JSON
Click the **"Import JSON"** button (red, with upload icon) to import data from a previous JSON backup:
*   In the Electron desktop app: A file picker opens to select the JSON file.
*   In the web version: A file browser opens.

> **⚠️ Warning**: Importing JSON will **overwrite** your current data. Make sure you have a backup before importing!

---

## 🔄 Also Available in Settings

The calendar feed URL and token regeneration are also available in the **Settings** page under "External Calendar (iCal)". Export and import of the database (.db format) are available under "Backup and Synchronization" in Settings.

---

### 👉 Next Step
Finally, let's configure LibrePM to work exactly how you want. Next chapter: **Settings & Personalization**.
