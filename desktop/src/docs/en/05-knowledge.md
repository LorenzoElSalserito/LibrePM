# Chapter 5: Ideas, Numbers, and Security 🧠

We talked about boxes (Projects), things to do (Tasks), and time (Gantt).
But where do you put your **ideas**, your notes, and important documents?

Welcome to the **Knowledge** section!

---

## 📝 Notes: Your Digital Notebook

**Notes** are blank pages where you can write whatever you want.
Example: "Shopping list for the party", "Ideas for the new logo", "Meeting minutes".

### How to create a Note
1. Go to the **"Notes"** page.
2. Click on **"+ New Note"** (top right).
3. A dialog opens asking for:
    *   **Title**: Write a name (e.g., "Logo Ideas").
    *   **Associate to**: Choose whether to link the note to the **Current Project** or a **Specific Task**.
    *   If you choose "Specific Task", a task selector dropdown appears.
4. Click **"Create Note"**.

### 🔗 Linking Notes
Notes don't live in a vacuum. They are always connected to a project or a specific task.
Example: The "Logo Ideas" note is for the "Website" project.

When you open that project or task, you will immediately see the linked note!

### 📂 Note Scopes
The Notes page has a sidebar on the left with **scope buttons** to filter your notes:
*   **All**: See all notes you have access to.
*   **Inbox**: See notes sent to you by other team members.
*   **Sent**: See notes you created and sent.

### 🔍 Context Filters
Below the scope buttons, you can further filter:
*   **All contexts**: See everything.
*   **Project**: See only project-level notes.
*   **Task**: See only task-level notes (a task selector dropdown appears to pick a specific task).

### ✍️ The Editor
The main area shows the selected note with a powerful Markdown editor. You have **three editor modes** (toggle buttons at the top right):
*   **✏️ Edit**: Write and edit the Markdown source.
*   **📐 Live (Split)**: See the editor on the left and a live preview on the right, side by side.
*   **👁️ Preview**: See only the rendered output (read-only view).

The **"Save"** button turns **green** when there are unsaved changes, and shows "Saved" (gray) when everything is up to date.

> **🔒 Read Only**: If a note was created by someone else, you can read it but not edit it. A "Read Only" badge appears next to the title.

### 🗑️ Deleting Notes
You can delete notes you own by clicking the **trash icon** on the note card in the sidebar. A confirmation will be asked.

---

## ✍️ Writing with Style (Markdown)

LibrePM uses a special way of writing called **Markdown**.
Don't worry, it's super easy!
Instead of clicking buttons for bold or lists, you use symbols on the keyboard.

Here are the main tricks:

*   **Titles**: Put a `#` before the text.
    *   `# Big Title` -> **Big Title**
    *   `## Medium Title` -> **Medium Title**
*   **Bold**: Put two asterisks `**` around the word.
    *   `**Important**` -> **Important**
*   **Italic**: Put one asterisk `*` around.
    *   `*Italic*` -> *Italic*
*   **Bullet Lists**: Put a dash `-` and a space.
    *   `- Bread`
    *   `- Milk`
*   **Numbered Lists**: Put a number and a dot.
    *   `1. First`
    *   `2. Second`
*   **Links**: Put the text in square brackets `[]` and the address in parentheses `()`.
    *   `[Google](https://google.com)`

Try writing in the note and look at the **Preview** to see how it looks!

---

## 📊 The Dashboard: The Cockpit

The **Dashboard** is the first thing you see when you open LibrePM.
It is the summary of all your work.

Here you find:
1.  **Active Projects**: How many you have open.
2.  **Completed Tasks**: How many you have finished. Well done!
3.  **Upcoming Deadlines**: Tasks that expire soon. Don't forget them!
4.  **Recent Activity**: What you have done lately (created tasks, modified notes...).

It is useful to understand "How is it going?" at a glance.

> **💡 Pro Tip:** For a more advanced dashboard with OKRs, baselines, risks, deliverables and project charter, check the **Charter** page (Chapter 8).

---

## 💾 Save and Export (Backup)

LibrePM saves everything automatically on your computer. You don't have to worry.
But if you want a backup copy or want to move data to another computer?

### 1. Export the Database (The Heart)
Go to **Settings** -> **Backup and Synchronization**.
Click on **"Export Database (.db)"**.
It will save a `.db` file with EVERYTHING inside (projects, tasks, notes...). Keep it safe!

### 2. Export to JSON (Readable)
Do you want to see data in a format that other programs can also read?
Click on your profile picture at the top right -> **"Export Database"** -> **"Export JSON"**.
It will create a readable text file with all your data.

### 3. Export CSV (Per Project)
Need a spreadsheet? Go to the **Integrations** page and click **"Export CSV (Project)"** to export the current project's tasks as a CSV file.

### 4. Import
If you change computers, you can **"Import Database"** (always from Settings) to put everything back in place.

You can also import from a JSON backup via the **Integrations** page (**"Import JSON"**). Note: this will overwrite current data.

### 5. Calendar Feed (iCal)
LibrePM generates an **iCal feed URL** that you can add to Google Calendar, Outlook, or Apple Calendar to see your task deadlines there. Find it in **Settings** or in the **Integrations** page. See Chapter 12 for details.

---

### 🎉 Congratulations!
Now you know all the essential secrets of LibrePM!
But there's much more to explore — continue to the next chapters to learn about Team Collaboration, Calendar, Executive Dashboard, Resources, Templates, Analytics, Integrations, and Settings.
