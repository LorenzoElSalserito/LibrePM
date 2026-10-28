# Chapter 5: Ideas, Numbers, and Security 🧠

We've talked about boxes (Projects), things to do (Tasks), and timing (Gantt).
But where do you put your **ideas**, notes, and important documents?

Welcome to the **Knowledge** section!

---

## 📝 Notes: Your Digital Notebook

**Notes** are pages where you can write anything you want, in Markdown.
Example: "Ideas for the new logo", "Meeting minutes", "Why we picked this supplier".

Go to the **"Notes"** page in the sidebar: the page title is **"Notes & Feed"**.

> **📌 A project is required:** the Notes page always works in the context of the **current project**. If you don't have a project yet, you'll see the message *"Select or create a project to manage notes."*

### How to create a Note
1. Click **"New Note"** in the top right.
2. A dialog asks for:
    *   **Title** (required): Give it a name (e.g., "Logo Ideas").
    *   **Note Type**: What kind of note is it? Choose between *Memo* (default), *Rationale*, *Meeting Note*, *Risk Note*, *Grant Note*, *Sponsor Note*, *Audit Note*, *Decision* and *Brief*.
    *   **Associate to**: Link the note to the **Current Project** or to a **Specific Task**.
    *   If you choose "Specific Task", a dropdown with the project's tasks appears.
3. Click **"Create Note"**.

> **💡 Tip:** If you have already filtered the sidebar by a task, the dialog pre-selects that task.

You can also create notes elsewhere in the app: from the notes tab of the **Task Editor** (Chapter 3) and from the **"Project Notes"** box on the Dashboard (see below).

### 🔗 Linking Notes
Notes don't live in a vacuum: they are always linked to a **project** or a **task**.
Example: The note "Logo Ideas" belongs to the "Website" project.

### 👥 Notes shared with the team
Notes are shared with all **project members** (Chapter 6):
*   Every member (OWNER, EDITOR or VIEWER) can **read** the notes of the project and its tasks.
*   Only the **author** of a note can edit or delete it.
*   When someone creates a note, the other project members receive a **notification** in the bell 🔔.

> **📌 Notes ≠ Messages:** notes are documents attached to a project or a task. For a free-form conversation with the team use the **Messages** page (Chapter 19).

### 📂 Note Scopes (Feed)
The Notes page has a left sidebar with three **scope buttons**:
*   **All**: All notes in the projects you are a member of.
*   **Inbox**: Notes written by **other** members in your projects.
*   **Sent**: Notes written by **you**.

> **📌 Note:** the feed includes notes from **all** projects you belong to, not just the current one. Changing the scope resets the context filter.

### 🔍 Filters
Below the scope buttons you can narrow the list further:
*   **All contexts**: The feed of the selected scope.
*   **Project**: Only notes attached directly to the current project.
*   **Task**: Only the notes of one task (a **"Select Task..."** dropdown appears to pick it).
*   **All Types**: A menu to show only one note type (e.g., only *Decisions*).
*   **Evidence Only** (shield icon): Show only notes marked as evidence.

In the top right there is also a **"Search notes..."** field that filters notes by title and content.

### 🗂️ The Note List
Each note in the list shows its title, a content preview and the last modified date. In addition:
*   A **green left border** marks your notes, a **grey** one marks other people's notes (for these, the author's name is shown too).
*   A shield icon marks an **Evidence** note, a padlock a **Frozen** note.

### ✍️ The Editor
When you select a note, the main area shows a Markdown editor. You have **three modes** (buttons in the top right):
*   **✏️ Editor**: Write and edit the Markdown source.
*   **📐 Editor + Preview**: Editor and live preview side by side.
*   **👁️ Preview**: Only the rendered result.

Next to the title you can change the note's **type**. The **"Save"** button turns **green** when there are unsaved changes and shows **"Saved"** when everything is up to date.

> **🔒 Read Only**: If a note was written by someone else, you can read it but not edit it. A **"Read Only"** badge appears next to the title and the editor only shows the preview.

### 🛡️ Evidence, Freezing and Revisions
On your own notes you get a few governance tools:
*   **Mark as Evidence** (shield icon): Flags the note as documentary evidence (e.g., minutes you must keep). Click again to **"Remove Evidence"**.
*   **Freeze Note** (padlock icon): Locks the note. A **Frozen** note cannot be edited until you unlock it with **"Unfreeze Note"**.
*   **Revisions** (clock icon, available to everyone): Opens the history panel. Every time you save a change to the content, LibrePM records a numbered revision. Click a revision to load it back into the editor, then click **"Save"** to restore it.

### 🗑️ Deleting Notes
You can delete notes you authored by clicking the **trash icon** on the note in the list. You will be asked to confirm.

---

## ✍️ Writing with Style (Markdown)

LibrePM uses a special way of writing called **Markdown**.
Don't worry, it's super easy!
Instead of clicking buttons for bold or lists, you use symbols on your keyboard.

Here are the main tricks:

*   **Headings**: Put a `#` before the text.
    *   `# Big Title` -> **Big Title**
    *   `## Medium Title` -> **Medium Title**
*   **Bold**: Put two asterisks `**` around the word.
    *   `**Important**` -> **Important**
*   **Italic**: Put one asterisk `*` around it.
    *   `*Italic*` -> *Italic*
*   **Bullet Lists**: Put a dash `-` and a space.
    *   `- Bread`
    *   `- Milk`
*   **Numbered Lists**: Put a number and a period.
    *   `1. First`
    *   `2. Second`
*   **Links**: Put the text in square brackets `[]` and the address in parentheses `()`.
    *   `[Google](https://google.com)`

Try writing in a note and switch to **Preview** to see how it looks!

---

## 📊 The Dashboard: Your Cockpit

The **Dashboard** is the summary of all your work. The first time you open LibrePM it starts here; afterwards it reopens the last page you were using.

From top to bottom you'll find:
1.  **Four counters**: **Active Projects**, **Completed Tasks**, **Total Tasks** and **Overdue**.
2.  **Focus heatmap** and **estimates chart** (estimated vs actual): the same widgets as the Analytics page (Chapter 11).
3.  **Effort Variance**: Estimated vs actual hours for the tasks of the selected project.
4.  **Pending Approvals**: Approval requests addressed to you, showing who asked (**"Requested by"**), with **Approve** / **Reject** buttons.
5.  **Project Notes**: Pick a project from the menu to see its notes, create one on the fly (**+** button) or open them in the Notes page (**"See all"**).
6.  **Project Files**: The attachments of the project selected in the Project Notes menu. With **Upload file** (or by dropping files on the box) you share them with every member; the download icon also opens files that colleagues uploaded from other PCs; only the uploader can delete a file (Chapter 21). Maximum 50 MB per file.
7.  **Project Health**: The list of active projects, sorted by health, with an **On time**, **Warning** or **Overdue** badge and the number of overdue tasks.
8.  **Overdue Tasks (Top 10)**: The ten oldest overdue tasks. **"See all"** takes you to the Planner.
9.  **Completed / Archived Projects**: Archived projects, with a **Reopen** button.

The **Refresh** button (🔄) at the top reloads all data. If you don't have any project yet, a **"No Projects Yet"** popup invites you to create one with **"Go to Projects"**.

> **💡 Pro Tip:** For a more advanced dashboard with OKRs, baselines, risks, deliverables and the project charter, go to the **Charter** page (Chapter 8).

---

## 💾 Saving and Exporting (Backup)

LibrePM saves everything automatically to its database: you never need to press "save" for your projects.
If you want a safety copy, the tools live in two places:

*   **Settings → Backup and Synchronization**: database file (.db) export/import and **encrypted backup** with restore (Chapter 13).
*   **Integrations → Import / Export Data**: JSON export/import of projects and CSV export/import of a project's tasks (Chapter 12).

There is also an **"Export data (JSON)"** item in the profile menu (top right).

> **💡 Tip:** for a complete safety copy use the database (.db) export or the encrypted backup; the JSON export is handy to move projects between profiles or computers. On a **shared database** (MariaDB) full backups are the server administrator's job (Chapter 21), but JSON export/import is still available.

For the **calendar feed (iCal)** see Chapter 13.

---

### 🎉 Congratulations!
You now know the essential secrets of LibrePM!
But there's much more to explore — continue with the next chapters to discover Team & Collaboration, Calendar, Executive Dashboard, Resources, Templates, Analytics, Integrations, Settings and the advanced modules.
