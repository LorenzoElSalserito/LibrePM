# Chapter 16: Change Control & Branches 🔀

The **Change Control** page helps you manage project changes formally: change requests, branches (alternative scenarios) and the decision log.

It works on the **active project** (chosen with the project selector in Planner, Kanban, Gantt or Charter, or by opening a project from **Projects**). Without projects it shows *"Select a project"*.

At the top you'll find three tabs: **Change Requests**, **Branches** and **Decision Log**.

> **⚠️ Warning:** On this page the delete buttons act **immediately, without asking for confirmation**.

---

## 📝 Change Requests

On the left is the list of requests; on the right, the form of the selected request.

### Creating a request
1. Click **"New"**.
2. LibrePM creates a "New change request" in your name and opens it on the right.
3. Fill in the fields:
    *   **Title**: what needs to change.
    *   **Status**: Draft, Submitted, In Review, Approved, Rejected, Implemented.
    *   **Priority**: Low, Medium, High, Critical.
    *   **Scope**: Schedule, Budget, Scope, Resources or All.
    *   **Description**: the details of the change.
    *   **Motivation**: why it is needed.
    *   **Expected Impact**: what effects it will have.

Text fields are saved when you leave them; status, priority and scope as soon as you pick them.

In the list each request shows its title, a status badge and, below, priority and scope. Click a request to reopen it; the **"Delete"** button at the top of the form removes it.

> **📌 Note:** You change the status by hand: LibrePM does not enforce an approval workflow nor apply the change to the project automatically.

---

## 🌿 Branches

Branches are **scenarios** that let you record alternatives to the main plan (for example a "what if..." analysis).

### Creating a branch
Click **"New Branch"**: LibrePM creates a "New branch" of type **Scenario**, with status **Active**.

Each branch appears as a card with:
*   Its **name** and a badge with its **type** (Scenario, Reforecast, Candidature, Pivot, Spin-off, Evolutive).
*   A dropdown to change its **status**: Active, Approved, Rejected, Merged, Archived.
*   The creation date and, when present, the description and the **Linked CR** (the associated change request).
*   The **"Delete"** button.

> **📌 Note:** From the interface you can currently change only a branch's status: name, type, description and the link to a change request cannot be edited. A branch is a record of the scenario: it does not create a separate copy of the project's tasks.

---

## 📒 Decision Log

Keeps track of the decisions taken in the project, with their date.

1. Click **"Add"**: LibrePM creates a "New decision" in your name, dated today.
2. On each entry you can write:
    *   the **title** (first line);
    *   the **Decision**: what was decided;
    *   the **Rationale**: why.
3. Text is saved when you leave the field.

On the right of each entry you see the decision date and the **trash icon** to delete it.

This leaves you a trail of **why** the project evolved the way it did.

---

### 👉 Next Step
When projects multiply, it pays to group them. Next chapter: **Portfolio Management**.
