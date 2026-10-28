# Chapter 14: Project Finance 💰

The **Finance** page collects the economic figures of a project: budget versions with their lines, funding sources and an analytics summary.

---

## 📂 Which project are you working on?

The **Finance** page has no selector of its own: it always shows the **active project**, i.e. the one chosen with the project selector in Planner, Kanban, Gantt or Charter, or by opening a project from the **Projects** page. At startup the active project is the first one in the list.

If there is no project, the page shows the message *"Select a project"*.

At the top you'll find three tabs: **Budget**, **Funding** and **Analytics**.

---

## 🧮 Budget Tab

### Creating a budget
1. Click **"New Budget"**.
2. LibrePM creates a budget named "Budget v1", "Budget v2"... (the number follows how many budgets already exist) in **DRAFT** status and selects it.

The dropdown next to the button lists all the project's budgets as *name (version) — status*. Pick one to see its lines.

### Budget lines
Click **"Add Line"** to insert a row named "New budget line" in the **Miscellaneous** category. Every row of the table is edited directly in its cells:

| Column | What it holds |
|--------|---------------|
| **Name** | The line name |
| **Category** | Personnel, Suppliers, Software, Travel, Events, Equipment, Overhead, Miscellaneous |
| **Planned** | The budgeted amount |
| **Committed** | The amount already committed (orders, contracts) |
| **Actual** | What you have really spent |
| **Forecast** | Your estimate at completion |

Text and amounts are saved when you leave the cell; the category is saved as soon as you pick it. The **trash icon** at the end of the row deletes the line immediately, without confirmation.

The **Total** row at the bottom sums the four amount columns.

> **💡 Note:** Actual costs are recorded by typing the amount in the line's **Actual** column. There is (currently) no separate log of individual expenses in the interface.

### Approving a budget
While a budget is in **DRAFT** status the green **"Approve"** button is shown. Clicking it moves the budget to **APPROVED** and records who approved it.

> **⚠️ Warning:** Approval does not lock the budget: lines remain editable afterwards. If you want to keep a "snapshot", create a new budget for later changes.

---

## 💶 Funding Tab

Click **"Add Funding Source"** to create a row "New funding source" of type **Internal**. Here too every cell is edited in place:

| Column | What it holds |
|--------|---------------|
| **Name** | The source name |
| **Type** | Internal, Grant, Sponsor, Donation, Co-Funding |
| **Total Amount** | The amount made available |
| **Currency** | Currency code (default EUR) |
| **Status** | Active, Pending, Exhausted, Cancelled |
| **Contact Name** / **Contact Email** | Your contact at the funder |
| **Restricted** | Tick it if the funds come with a usage restriction |

When you tick **Restricted**, a *"Restriction description..."* field (with a lock icon) appears below the row to describe the restriction.

The **trash icon** deletes the source immediately, without confirmation. With no sources you'll see *"No funding sources"*.

---

## 📈 Analytics Tab

If the project has no budget yet, the tab shows *"Create a budget to see analytics"*.

Otherwise the analytics are based on the **approved** budget; if there is none, on the budget with the highest version. You'll find:

*   **Planned**: the sum of the lines' planned amounts.
*   **Actual**: the sum of the actual amounts.
*   **Variance**: Planned minus Actual, with the percentage below. Green when you are under budget, red when you have exceeded it.
*   **Funding Coverage**: how much of the planned total is covered by the sum of the funding sources' amounts. Below 100% it turns yellow and, if something is missing, the **Gap** (the uncovered amount) appears in red.
*   **Burn Rate**: a bar with the Actual/Planned percentage. Green up to 70%, yellow above 70%, red above 90%.
*   **Actual by Category**: actual spending grouped by category (shown only when there are lines).

---

## 🚀 Getting Started

1. Choose the active project (for example from the Planner selector).
2. Go to the **Finance** page → **Budget** tab → **"New Budget"**.
3. Add lines with **"Add Line"** and fill in the planned amounts.
4. In the **Funding** tab, record who funds the project.
5. As you spend, update the **Committed**, **Actual** and **Forecast** columns.
6. Check the **Analytics** tab to see burn rate, variance and coverage.

---

### 👉 Next Step
Many projects live on grants and on relationships with sponsors and partners. Next chapter: **Grants & Stakeholders**.
