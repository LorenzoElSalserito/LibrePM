# Chapter 21: Shared Database & Sync 🌐

So far we've pictured LibrePM as a workshop of your own, inside your computer. But what if you want to work with colleagues, each from their own PC, on the **same projects**?

In this chapter we look at LibrePM's two ways of working and what changes when you use a **shared database**.

---

## 🏠 Local Mode (Default)

By default LibrePM stores everything in a **SQLite** database in your computer's data folder (you can find the path via Profile Menu → **Open Data Folder**).
*   It works without internet and without a server.
*   Several profiles on the same computer share the same database: each one signs in with its own password.
*   Other computers don't see your data: to move it use the database export or the encrypted backup (Chapter 13), or the JSON export/import (Chapter 12).

---

## 🏢 Shared Mode (MariaDB)

An **administrator** can connect LibrePM to a shared **MariaDB** database. That way several desktop installations read and write the same data: projects, tasks, notes, messages, file attachments, notifications, team members.

### Who sets it up?
The setup is **not done from the app**. In **Settings** → **External Database** the controls (database type, host, connect button) are deliberately **disabled** and only show the options (Chapter 13): database credentials must not be stored from the interface. That card's message saying you are using the local SQLite database is always shown, even when you are connected to MariaDB.

Instead, the administrator:
1. Prepares an empty MariaDB database and initializes it **once** with LibrePM's provisioning command (optionally importing an existing local database).
2. On each computer starts LibrePM with the MariaDB profile environment variables, for example `SPRING_PROFILES_ACTIVE=mariadb` plus host, database name, user and password (`LIBREPM_DB_*`).
3. Takes care of network security (TLS, firewall), database backups and restores.

> **🛠️ For administrators:** the full procedure, variables and limits are described in the project's `README.md` (section *External MariaDB*) and in `deploy/README.md`.

---

## 👥 What Changes for You

When your LibrePM is connected to the shared database:

### Profiles
The **"Select a profile"** screen (Chapter 1) shows the profiles of **everyone** registered in the shared database. Each person signs in with their own password. Avoid automatic login on computers used by several people.

### Projects and roles
You see the projects you are a **member** of. The owner shares them by adding colleagues from the **Team** page (Chapter 6), with one of these roles:
*   **OWNER** and **ADMIN**: can do everything in the project.
*   **EDITOR**: can read and edit.
*   **VIEWER**: can only read.

### Shared notes
Notes attached to a **project** or a **task** (Chapter 5) are visible to all project members:
*   All members (OWNER, EDITOR, VIEWER…) can **read** each other's notes.
*   Every member, **VIEWERs included**, can also **write** notes on tasks and projects: unlike Messages, notes don't follow the read-only role.
*   Only **the author** can edit or delete their own note; for everyone else it's read-only.
*   A task's **Linked Notes** box only shows notes from projects you are a member of, and only a note's author can link it to other items.
*   Notes on a task or project are listed in chronological order, even when two colleagues write at the same moment.
*   When someone creates a note, the other members get a **"New comment"** notification in the bell 🔔.
*   In the Notes page, the **All / Inbox / Sent** scopes help you separate the notes you received from the ones you wrote.

### Shared files and attachments
**Task** attachments (Chapter 3) and the Dashboard's **Project files** are stored **in the database**, not only on the uploader's disk. That's why a colleague on another PC sees them in the list and opens them with one click.
*   All project members (VIEWERs included) can **see and download** the files.
*   **Uploading** files requires edit permission (OWNER, ADMIN, EDITOR); VIEWERs cannot upload.
*   Only **the uploader** can delete a file, and the deletion applies to everyone.
*   People who are not project members don't see the files and can neither download nor upload them.
*   When someone adds a file, the other members get a **"New file"** notification in the bell 🔔: the **Open files** button takes you to the task (in the Planner) or to the project's Dashboard.
*   Maximum **50 MB** per file. On your PC LibrePM keeps a copy of the files you already opened in the data folder (`assets`), so later openings are instant.
*   Files uploaded before this version were only on the uploader's PC: they are copied into the database automatically the next time LibrePM starts **on that PC**. Until then, others see the file in the list but the download says its content is not available yet.

> **📌 Note:** in local mode too, files are stored in the SQLite database: if the `assets` folder is lost, LibrePM recreates it the first time you open them.

### Messages
The **Messages** page (Chapter 19) shows the same project conversation on every computer.

### How "real-time" is it?
LibrePM doesn't use a live connection: pages update by reading the database.
*   **Messages** refresh on their own every 10 seconds.
*   The **bell** counter refreshes every 30 seconds.
*   Other pages show your colleagues' changes when you reopen them or use their refresh button.

---

## 🔄 Sync Conflicts

The **Sync Conflicts** page (titled **"Conflict Review"**) is for manually resolving cases where the same information exists in two different versions, a **local** one and a **remote** one, that LibrePM can't merge on its own.

*   If everything is fine you'll see *"No pending conflicts"*: that's the normal situation.
*   If there are conflicts, a yellow badge next to the title shows how many. On the left there's the list (item type, conflict type and identifier).
*   Click a conflict to see the **"Local Version"** and the **"Remote Version"** side by side (as raw data).
*   Choose:
    *   **"Keep Local"**: keep the local version.
    *   **"Accept Remote"**: adopt the remote version.
*   A notice confirms the resolution and the conflict disappears from the list.

> **📌 Note:** with the shared database everyone writes directly to the same data, so this page normally stays empty.

---

### 🎉 End of the Journey
You've completed the LibrePM manual! You now know every corner of your digital workshop, alone or as a team. Happy working! 🚀
