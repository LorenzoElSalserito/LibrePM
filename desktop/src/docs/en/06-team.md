# Chapter 6: Team and Collaboration 👥

LibrePM is not just for solo work. You can build a team, link colleagues who use LibrePM, create "virtual" members for people without an account, and decide what each person can do in every project.

> **💡 Really working together:** the other LibrePM users you can find and add are those in the **same database**: other profiles created on this computer or, if your administrator has set up a shared MariaDB database, colleagues using the same server. See Chapter 21.

---

## 👥 The Team Page

Click **"Team"** in the sidebar (the page title is **"Team Management"**). Here you manage who has access to your projects and with which role.

### The Team Table
The main view is a table showing the members of all your active projects (one row per member/project pair):

| Column | What it shows |
|--------|---------------|
| **Member** | Avatar (initials circle), display name and @username |
| **Account Type** | **"LibrePM"** badge (real user) or **"Local"** badge (virtual user) |
| **Project** | Which project this membership belongs to |
| **Role** | An **"Owner"** badge for the owner; for everyone else a dropdown with **Admin**, **Editor**, **Viewer** |
| **System Role** | An optional dropdown (**"None"** or a system role) — not available for the owner |
| **Actions** | ✏️ **"Edit Member"** (local users only) and 🗑️ **"Remove from project"** (not available for the owner) |

### Filter by Project
Use the **project selector** at the top left to show only the members of one project, or choose **"All projects"** to see everyone.

---

## 🔐 Roles and Permissions

Every project member has a **project role**:

| Role | What they can do |
|------|------------------|
| **Owner** (OWNER) | Everything. The person who created the project; their role cannot be changed and they cannot be removed. Only the owner can **add** or **remove** members. |
| **Admin** (ADMIN) | All permissions on the project's content, and can **change the roles** of other members. |
| **Editor** (EDITOR) | Can read and modify the project's content (tasks, notes, messages...). This is the default role for new members. |
| **Viewer** (VIEWER) | Read-only access: sees the content but cannot modify it or write. |

### Changing a Role
In the **Role** column, pick the new value from the dropdown. The change is immediate (a "Role updated" message appears). Only the project's **Owner** or an **Admin** can change roles: for anyone else the operation is rejected with an error.

### System Role (optional)
The **System Role** column lets you link a member to a predefined permission profile (OWNER, ADMIN, EDITOR, VIEWER). When set, members who are not Owner or Admin get their permissions from that profile instead of from their project role. Leave **"None"** to simply use the project role.

---

## ➕ Adding Team Members

Click the **"+ New Member"** button at the top right. The **"Add Member to Team"** dialog opens:

### 1. Select a Project
First, choose in **"Project \*"** which project you want to add the member to (required).

### 2. Choose the Member Type
A toggle switch lets you choose between two modes:

#### 🔗 Connect LibrePM User (Friend)
Use this mode to add someone who already exists:
*   The **"Select Contact \*"** dropdown shows two groups: **"Contacts"** (your connected LibrePM contacts) and **"Virtual Members"** (local users you have already created).
*   People who are already members of the selected project appear disabled.
*   Click **"Add to Team"**. The member is added with the **Editor** role; you can change it right away from the table.

> **⚠️ Note:** A real LibrePM user must first be one of your **Contacts** (see below). If you have no connections yet, the dialog tells you so.

#### 👤 Create Local User (Virtual)
Use this mode for a **placeholder member** — someone who doesn't use LibrePM but needs to appear in the project:
*   Enter a **"Display Name \*"** (e.g., "John Doe (External)").
*   Enter a **"Username Identifier \*"** (e.g., "john.doe.ext") — it must be unique in the system.
*   Click **"Create Local"**. The user is created and added to the project as **Editor**.

Local users have no login. They are useful for:
*   External stakeholders or suppliers who don't use the app.
*   Assigning tasks to people not yet on LibrePM.
*   Planning and allocating resources before the team is fully set up.

> **🔒 Permissions:** only the project's **Owner** can add members.

### Understanding the Types
The page's **right panel** ("Member Management") explains the difference:
*   **LibrePM — Real User**: one of your LibrePM contacts with their own account.
*   **Local — Virtual User**: a placeholder without login, used to assign tasks to external resources.

---

## ✏️ Editing a Local User

For local users, the ✏️ **"Edit Member"** button opens a dialog where you can:
*   Change the **"Display Name \*"** and **"Username Identifier \*"**.
*   Tick or untick the **Projects** they belong to (newly ticked projects add them as Editor, unticked ones remove them).

Click **"Save Changes"** to confirm.

---

## 🗑️ Removing Members
To remove a member from a project:
1. Find them in the table.
2. Click the **trash icon** in the Actions column.
3. Confirm the removal.

> **⚠️ Note:** Only the **Owner** can remove members, and the Owner themselves cannot be removed.

---

## 🤝 External Contributors

Below the members table is the **"External Contributors"** section: a register of external people (consultants, reviewers, partners) who contribute to the work without being project members.

1. Click **"+ Add External"**.
2. Fill in **"Display Name \*"**, **Email**, **Organization**.
3. Optionally choose a **System Role**.
4. Choose the **Scope**: **Project**, **Deliverable** or **Review**, and the **Linked Entity** (for Project scope a project or **"All"**; for the others the item's ID).
5. Click **"Add External"**.

The table shows Name, Email, Organization, System Role and Scope. The **"Revoke Access"** button removes the contributor from the register (with confirmation).

> **📌 Note:** external contributors are an informational register: they do not get an account or access to the app.

---

## 🌐 Contacts: Your Professional Network

Go to the **"Contacts"** page in the sidebar. Here you manage your network of LibrePM users and your virtual members.

### Left Column: Your Network

#### Incoming Requests
If someone has sent you a connection request, you'll see the **"Incoming Requests"** card with:
*   The person's name and username.
*   **"Accept"** button: add them to your network.
*   **"Reject"** button: decline the request.

New requests and accepted connections also arrive as notifications in the 🔔 bell (Chapter 1).

#### Your Contacts
A list of your connected contacts, each with avatar, display name and username, and a **"Remove"** button (red person icon) to disconnect.

#### Virtual Members
The **"Virtual Members"** card lists the local users you have created (**"Virtual"** badge):
*   ✏️ **Edit**: change display name and username.
*   🗑️ **Remove**: delete the virtual member (with confirmation).
*   At the bottom of the card you can create a new one by entering **Display Name** and **Username** and clicking **"Create"**. If you have active projects, the **"Assign to Projects"** dialog opens right away: tick the projects and click **"Assign"** (they'll be added as Editor), or click **"Skip"**.

### Right Column: Search and Connect

#### Search Contacts
*   Type at least 2 characters of a **username, name or email** into the **"Username or email..."** field.
*   Click **"Search..."** (or press Enter).
*   In the results (excluding yourself, existing contacts and pending requests) click **"Connect"** to send a request.

#### Sent Requests
Below the results you see your pending **"Sent Requests"** ("To: name") with a **"Pending"** badge.

---

### 👉 Next Step
Your team can also exchange messages inside each project: see Chapter 19 (**Project Messages**). Now let's see how to view commitments over time. Next chapter: the **Calendar**.
