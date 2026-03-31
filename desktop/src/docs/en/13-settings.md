# Chapter 13: Settings & Personalization ⚙️

The **Settings** page lets you customize LibrePM to fit your preferences, manage backups, configure calendar feeds, and report bugs.

---

## 🎨 Appearance

### Theme
Choose between two visual themes:
*   **Light**: Clean and bright interface.
*   **Dark**: Darker interface, easier on the eyes in low light.

The change applies **immediately** — you can see the preview before saving.

### Language
Switch between:
*   **Italian**: Full Italian interface.
*   **English**: Full English interface.

All UI elements update instantly.

### Saving
*   Click **"Save Changes"** (green button) to persist your preferences.
*   The button appears green when you have unsaved changes, and gray when everything is saved.
*   Click **"Restore Defaults"** to reset all settings to their original values (with confirmation).

---

## 📅 External Calendar (iCal)

This section is also available on the Integrations page (Chapter 12):

*   **iCal URL**: A read-only field showing your personal calendar feed URL.
*   **Copy** button: Copies the URL to your clipboard.
*   **"Regenerate Token"** button (red outline): Creates a new URL, invalidating the old one.

---

## 🗄️ External Database

> **📌 Coming Soon:** This section is currently a placeholder for future functionality.

When available, it will let you connect to external databases:
*   **Database Type** selector (currently disabled):
    *   SQLite (Local) — current default.
    *   PostgreSQL (Coming Soon).
    *   MySQL (Coming Soon).
*   **Host** input (currently disabled).
*   **"Connect to External DB"** button (currently disabled).

An info alert shows: *"You are currently using the local SQLite database."*

---

## 💾 Backup and Synchronization

### Automatic Backup
A toggle switch to enable or disable automatic backups.

### Manual Backup
*   **"Export Database (.db)"** button (blue outline): Exports the full SQLite database file.
    *   In Electron: Opens a save dialog.
    *   In Web: Downloads the file.
    *   This is the most complete backup — it contains everything.

*   **"Import Database (.db)"** button (red outline): Imports a previously exported database file.
    *   In Electron: Opens a file picker.
    *   In Web: Opens a file browser.
    *   **Warning**: This replaces all your current data!

---

## 🐛 Bug Report

Found a bug? Help improve LibrePM!
*   Click **"Report a Bug"** (yellow button with envelope icon).
*   This opens your email client with a pre-filled template addressed to the LibrePM team.
*   Describe the bug and send the email.

---

## ℹ️ Information

A card at the bottom showing:
*   **Version**: Current LibrePM version number.
*   **Copyright**: © Lorenzo DM 2026.
*   **License**: AGPLv3.
*   **Website**: Link to https://www.lorenzodm.it
*   **Platform**: Desktop (Electron) or Web.
*   **Support note**: An invitation to support LibrePM's development.

---

### 🎉 You've Made It!
You now know every feature of LibrePM inside and out. You're ready to organize your life and your work like a true professional. Have fun in your digital workshop! 🚀
