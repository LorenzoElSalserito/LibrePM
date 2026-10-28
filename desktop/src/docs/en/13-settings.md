# Chapter 13: Settings and Personalization ⚙️

The **Settings** page lets you personalize LibrePM, protect your profile, manage backups, configure the calendar feed and notifications, and report bugs. You reach it from the sidebar or from the profile menu (top right) → **Settings**.

---

## 💾 Saving Preferences

In the top right of the page there are two buttons:
*   **"Save Changes"**: turns **green** when there are unsaved changes (theme, language, automatic backup) and is disabled when everything is saved.
*   **"Restore Defaults"**: resets settings to their original values (after confirmation).

The other sections (workspace profile, password, notifications, exports) act **immediately**, without going through "Save Changes".

---

## 🎨 Appearance

### Theme
Choose between two visual themes:
*   **Light**: Clean, bright interface.
*   **Dark**: Darker interface, easier on the eyes in low light.

The change is shown **immediately** as a preview; click **"Save Changes"** to make it permanent.

### Language
Switch between **Italian** and **English**. The interface updates instantly; save to keep your choice on the next start.

> **💡 Note:** the language of this **Guide** is chosen separately, with the selector at the top of the Guides page.

---

## 🧩 Workspace Profile

A set of cards with the available profiles (e.g., **Personal**, **Team Operations**, **PM / PMO Light**, **Nonprofit & Donations**, **Grant & Calls**, **Agency / Consultancy**, **R&D Consortium**). Each card shows a description and its associated modules; the active one has a blue border and a check mark. Click a card to activate it: the choice is saved immediately.

> **📌 Current status:** the selected profile is stored, but in this version it **does not hide** sidebar items: every page stays reachable whatever profile you pick. You can also pick the profile while creating your user profile (Chapter 1).

---

## 🔐 Security

### Change Password
Fill in **Current Password**, **New Password** and **Confirm Password** (each field has a 👁️ icon to show/hide the text), then click **"Change Password"**.
*   The new password must be at least **4 characters** long and both fields must match.
*   If your profile has no password yet, leave "Current Password" empty.

### Remove Password
The **"Remove Password"** button removes the password from your profile; it requires your **Current Password**. Without a password, anyone using the computer can open your profile.

> **💡 Forgot your password?** You can reset it from the sign-in screen by confirming your username (Chapter 1).

### Linked Identities
The **"Link Google"**, **"Link Microsoft"** and **"Link Keycloak"** buttons are visible but **disabled**: *"Available in a future version"*.

---

## 📅 External Calendar (iCal)

LibrePM can expose an **iCal feed** with the (non-archived) tasks **assigned to you** that have a deadline or a planned start.

*   **URL field**: your personal feed link (read-only) with a **Copy** button.
*   **"Regenerate Token"** (red outline): creates a new token after confirmation and shows the new URL. The previous link stops working.

> **📌 First use:** if the URL field is empty, click **"Regenerate Token"** to generate your link.

> **⚠️ Reachability:** the link points to the backend running on your computer (`localhost`). It works with calendar apps installed on the same computer while LibrePM is open; online services such as Google Calendar cannot reach it.

The Integrations page has a similar card (Chapter 12), but the working link is the one on this page.

---

## 🗄️ External Database

This card shows the planned database modes, but its controls are **intentionally disabled**:
*   **Database Type** selector: *SQLite (Local)* and *MariaDB (External, administrator configuration)*.
*   **Host** field and **"Connect to External DB"** button.

Connecting to a shared **MariaDB** database is not done here: the administrator configures it through backend environment variables. How collaboration on a shared database works is explained in **Chapter 21**.

> **📌 Note:** the message *"You are currently using the local SQLite database."* is fixed and also appears when the app is connected to MariaDB.

---

## 💾 Backup and Synchronization

### Automatic Backup
A toggle that stores your preference (on by default). It is saved with **"Save Changes"**.

> **📌 Current status:** in this version no process runs scheduled backups based on this toggle: do not treat it as a safety copy.

### Database file (.db)
*   **"Export Database (.db)"** (blue outline): saves a complete, consistent copy of the local database (every profile) to a `LibrePM-db-….db` file chosen with the system dialog.
*   **"Import Database (.db)"** (red outline): restores a previously exported `.db` file, **replacing all current data**. After you confirm, LibrePM:
    1.  checks that the file really is an intact LibrePM database not created by a newer version (otherwise it is rejected and nothing changes);
    2.  saves a **backup copy** of the current database to `config/backup/librepm-before-import-….db` in the data folder (find it with **"Open Data Folder"** in the profile menu);
    3.  restarts the backend, applies the file and reloads the app.

A progress indicator is shown during the restore; do not close the app until it reloads.

> **📌 Shared database (MariaDB):** when the administrator has configured a shared database, the `.db` and encrypted-backup buttons are **disabled** with a notice: on a shared server backups are the administrator's job (Chapter 21). The **JSON** export/import in Integrations (Chapter 12) still works.

### Encrypted backup
Enter a **Passphrase** (at least 4 characters) and click **"Export Encrypted"**: LibrePM saves a `LibrePM-encrypted-….enc.json` file containing the database encrypted with **AES-256-GCM**. Keep the passphrase safe: without it the file cannot be decrypted.

To restore it, enter **the same passphrase**, click **"Import encrypted backup"** and pick the file. The restore works like the `.db` import (validation, backup copy, restart). With a wrong passphrase an error is shown and your current data stays unchanged.

---

## 🔔 Notification Preferences

A table with one event type per row (e.g., *New Note*, *Task Assigned*, *Mention*, *Connection Request*, *Deadline Approaching*, *Budget Threshold*, *Approval Requested*) and two channel columns: **In-App** (the bell) and **Desktop** (system notification). Check or uncheck a box: each change is saved immediately.

Three buttons apply a preset to the whole table:
*   **All**: every event enabled.
*   **Essential**: task assignments, mentions, approaching deadlines and approval requests.
*   **Critical Only**: only approaching deadlines and budget thresholds.

---

## 🩺 Diagnostics

The **"Generate Diagnostics Bundle"** button downloads a ZIP file useful for troubleshooting. The bundle does **not** contain personal data or project contents.

---

## 🐛 Bug Report

Found a bug? Help improve LibrePM!
*   Click **"Report a Bug"** (yellow button with an envelope icon).
*   In the desktop app this opens your email client with a pre-filled message addressed to the developer.
*   Describe the problem and send the email.

---

## ℹ️ Information

A card showing:
*   **Version**: the current LibrePM version.
*   **Copyright**: © Lorenzo DM 2026.
*   **License**: AGPLv3.
*   **Website**: link to https://www.lorenzodm.it
*   **Support**: an invitation to support development, with a **"Donations"** button.
*   **Platform**: Desktop (Electron) or Web.

The same information is available from the profile menu → **Info LibrePM**.

---

## 🛡️ Your Data

At the bottom of the page, the **"Your Data"** card offers **"Export My Data"** and **"Request Data Deletion"**. They are described in **Chapter 18** (Privacy & Compliance).

---

### 🎉 You did it!
You now know LibrePM's main features. The following chapters cover the advanced modules (finance, grants, change control, portfolio, compliance), project **Messages** (Chapter 19), **Time Tracking** (Chapter 20) and working on a **shared database** (Chapter 21). Have fun in your digital workshop! 🚀
