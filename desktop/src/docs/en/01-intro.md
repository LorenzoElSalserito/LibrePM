# Welcome to LibrePM! 👋

Hi! Think of LibrePM as your **digital workshop**. It's a special place where you can organize your ideas, your work, and your projects without fear of losing anything.

You don't need the internet to use it: by default LibrePM lives on your computer ("Local-First"), just like your personal files. It's fast, safe, and always ready.

> **💡 Working in a team?** An administrator can connect several computers to the same shared database (MariaDB), so you and your colleagues see the same projects, notes and messages. Everything is explained in Chapter 21.

---

## 🏗️ Step 1: Who are you? (The Profile)

Everyone who uses LibrePM has a **Profile**, protected by a password.
Think of the profile as your lab coat: LibrePM uses it to know who is moving things around in the workshop.

### 🌍 Pick your language right away
In the top-right corner of the start screen there are two buttons, **IT** and **EN**. Click one to switch to Italian or English before you even sign in.

### Creating your profile
On first launch there are no profiles yet, so LibrePM shows the **"Create your profile"** form straight away. (If other profiles already exist, click **"Create new profile"** below the list.)

1. **Username** *(required)*: a unique identifier of at least 3 characters (e.g. "mario.rossi").
2. **Display Name** *(required)*: how you want to be called (e.g. "Mario Rossi").
3. **Email** *(optional)*: if you enter one, it must be a valid address.
4. **Password** *(required)*: at least 8 characters, with at least one uppercase letter, one lowercase letter, one number and one special character (e.g. `!@#`). The eye button 👁️ shows or hides what you type.
5. Optionally tick **"Automatically login on next startup"**.
6. Click **"Create profile and start"**.

If the username is already taken, LibrePM tells you below the field.

#### Choose your workspace (optional)
Right after creation, if more than one workspace profile is available, the **"Choose your workspace"** screen appears: click the profile that best matches how you work, or **"Skip for now"**. You can change it later in **Settings** → **Workspace Profile** (Chapter 13).

🎉 **Done!** You're in.

### Signing in with an existing profile
If profiles already exist, you'll see the **"Select a profile"** screen:
1. Click your profile card (the last one used has the **"Last used"** badge).
2. Type your **Password** and click **"Login"** (or press Enter). **"Cancel"** closes the box.

### Forgot your password?
1. Select your profile and click **"Forgot password?"**.
2. In **"Confirm username"** type the profile's username again (it must match).
3. Enter **"New password"** and **"Confirm new password"** (same rules as when creating a profile).
4. Click **"Reset password"**: the password is changed and you are signed in directly.

### Automatic login
Below the profile list there is the **"Automatically login with last used profile"** checkbox. If it is ticked when you sign in, on the next startup LibrePM signs in with that profile on its own, **without asking for the password**: only use it on a personal computer. Automatic login is turned off when you use **Change Profile** or **Exit**.

### Deleting a profile
Every card has a **trash icon** (**"Delete profile"**). After you confirm, the profile is deleted: this cannot be undone.

> **💡 Fun Fact:** You can create multiple profiles on the same computer! This is useful if you want to keep work ("Mario Work") separate from hobbies ("Mario Hobby").

---

## 🧭 Step 2: The Workshop Map (The Interface)

Let's look at the screen together. It's divided into four areas, like a well-organized workbench.

### 1. The Sidebar (On the Left)
This is your toolbox. From here you can go anywhere (items appear in this order):
*   **Dashboard**: The overview of your work (Chapter 5).
*   **Messages**: The project's shared conversations (Chapter 19).
*   **Inbox**: Quick capture of tasks to sort into projects later (Chapter 3).
*   **Charter**: Executive dashboard with project charter, OKRs, risks, deliverables and baselines (Chapter 8).
*   **Projects**: Where all your work "boxes" are kept (Chapter 2).
*   **Gallery**: Ready-made templates to start new projects quickly (Chapter 10).
*   **Team**: Your projects' members and their roles (Chapter 6).
*   **Resources**: Workloads and people allocation (Chapter 9).
*   **Contacts**: Your network of other LibrePM users (Chapter 6).
*   **Calendar**: A calendar view of your tasks and events (Chapter 7).
*   **Planner**: An ordered list, perfect for seeing everything together (Chapter 3).
*   **Gantt**: A timeline with dependencies and milestones (Chapter 4).
*   **Kanban**: Cards to move between columns (Chapter 3).
*   **Notes**: Your Markdown notebook, shared with project members (Chapter 5).
*   **Time Tracking**: Timer and manual logging of worked time (Chapter 20).
*   **Finance**: Budgets, funding and costs (Chapter 14).
*   **Grants**: Funding calls and their obligations (Chapter 15).
*   **Stakeholders**: Stakeholder register and map (Chapter 15).
*   **Change Control**: Change requests, branches and decisions (Chapter 16).
*   **Portfolio**: Programs grouping several projects (Chapter 17).
*   **Guides**: This documentation — always at hand.
*   **Analytics**: Charts and statistics about your productivity (Chapter 11).
*   **Integrations**: Calendar feed, data import/export (Chapter 12).
*   **Sync Conflicts**: Review of any synchronization conflicts (Chapter 21).
*   **Settings**: Appearance, security, backup and preferences (Chapter 13).

All items are always visible, whichever workspace profile you chose. The sidebar can be **collapsed** by clicking the arrow button at the bottom: only the icons remain (hover over one to read its name).

> **💡 Welcome back:** LibrePM remembers the last page you opened and shows it again the next time you sign in.

### 2. The Top Bar (At the Top)
On the left you see the title of the current page. On the right:

*   **Page Actions**: Buttons that change depending on the page (e.g. "+ New Project", searches, filters).
*   **🔔 Notification Bell**: A red badge shows how many unread notifications you have (the count refreshes every 30 seconds). Click the bell to see your **unread** notifications:
    *   **New comment** (blue icon): a project member wrote a new note on a project or task you share.
    *   **Incoming Requests** (yellow icon): someone wants to connect with you in Contacts.
    *   **Success** (green icon): one of your connection requests was accepted.
    *   **Notification** (grey icon): every other alert, for example when someone assigns you a task.
    *   The **"Mark as read"** button on each notification and **"Mark all as read"** at the top remove them from the list.
*   **Panel ▸ / Panel ◂**: Shows or hides the information panel on the right.
*   **👤 Profile Menu**: Your initials in a circle. Click it to open a menu with:
    *   Your name and username at the top.
    *   **Settings**: Opens the Settings page.
    *   **Info LibrePM**: A popup with version, website, copyright, license and the **"Donations"** button.
    *   **Open Data Folder**: Shows, in a notice, the path of the folder where LibrePM stores your data.
    *   **Export data (JSON)**: Quickly exports all projects of the profile to a JSON file, like **"Export JSON"** in Integrations (Chapter 12). For a full database backup use Settings (Chapter 13).
    *   **Change Profile**: Returns to the profile selection screen (with confirmation).
    *   **Exit**: Ends the session (with confirmation) and turns off automatic login.

### 3. The Workspace (In the Center)
This is where the magic happens! If you're in Projects, you'll see your boxes. If you're in the Planner, you'll see your lists.

### 4. The Right Panel (Right Side)
On the right side of the screen there's an **information panel** that adapts to the page you're on. It can show details about the selected item, statistics and tips. When the page doesn't use it, it shows the LibrePM version, the **Active Project** and the user you signed in with.

You can show or hide it at any time with the **"Panel"** button in the top bar, or close it with **Esc**.

---

## ⌨️ Step 3: The Command Palette (Pro Shortcut)

LibrePM has a **Command Palette** — a quick search bar to move around and find things instantly.

### How to open it:
Press **Ctrl+K** (or **Cmd+K** on Mac). Press **Ctrl+K** again or **Esc** to close it.

### What you can do:
*   **With an empty search box** you see the quick commands: **Go to Dashboard**, **Go to Projects**, **Go to Planner**, **Go to Kanban**, **Go to Calendar**, **Go to Notes**, **Go to Settings** and **New Project** (opens the project creation dialog straight away).
*   **While typing** you filter the "Go to…" commands for Dashboard, Planner, Kanban and Notes; from 2 characters on, LibrePM also searches the **text of your tasks and notes**. Pick a result to open the task in the Planner or the note in the Notes page.
*   Use the **arrow keys** to move through the list and **Enter** to select.

---

## ⌨️ Keyboard Shortcuts

| Shortcut | What it does |
|----------|--------------|
| **Ctrl+K** | Opens/closes the Command Palette |
| **Ctrl+N** | Goes to the **Planner** |
| **Ctrl+Shift+N** | Goes to **Notes** |
| **Ctrl+L** | Locks the session: goes straight back to profile selection (no confirmation) and turns off automatic login |
| **Esc** | Closes the right panel, the Command Palette or the open dialog |

On Mac use **Cmd** instead of **Ctrl**.

---

## 🎨 Step 4: Light and Dark (Themes)

LibrePM supports two visual themes:
*   **Light**: Clean and bright, ideal for daytime work.
*   **Dark**: Easy on the eyes, perfect for late-night sessions (it's the default theme).

You can switch themes in **Settings** → **Appearance** → **Theme**. The preview is immediate; remember to save (Chapter 13).

---

## 🌍 Step 5: Your Language

LibrePM is available in **Italian** and **English**. You can choose the language with the **IT/EN** buttons on the start screen or in **Settings** → **Appearance** → **Language**.

> **📖 Note:** In the **Guides** page you can choose the manual's language with a dedicated selector, independently of the interface language.

---

## 🚀 What's next?
Now that you have your lab coat (Profile) and know where the tools are (Interface), you're ready to start building. In the next chapter, we'll learn how to create your first "box": the **Project**.
