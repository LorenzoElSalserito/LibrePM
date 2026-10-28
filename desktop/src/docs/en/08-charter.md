# Chapter 8: Executive Dashboard and Charter 📊

The **Charter** page (its title is **"Executive Dashboard"**) is LibrePM's most complete management view. It brings together project governance, objectives, risks, deliverables and performance tracking in one place, always for the **current project**.

---

## 🎛️ Choosing the Project

In the top bar you'll find:
*   A **project selector**: choose the project to analyze. The **"Select project..."** entry takes you to the Projects page.
*   A 🔄 **Refresh** button to reload the data.

If no project is selected, the page only shows the message **"Select a project"**.

At the top of the page you see the **project name**, the caption "Executive Dashboard & Charter", the **status badge** and a **"Refresh"** button.

---

## 📊 The Stats Row

Four summary cards give an instant overview:

| Card | What it shows |
|------|---------------|
| **Completion** | Percentage of completed tasks in the project |
| **Overdue** | Number of tasks past their deadline |
| **Deliverables** | Completed deliverables (progress at 100%) out of the total (e.g., "3/7") |
| **High Risks** | Number of risks with High or Critical **impact** |

### The Status Badge
Next to the project name you see the overall status:
*   🟢 **On Track**: everything is going to plan.
*   🟡 **At Risk**: the latest baseline shows a small deviation.
*   🔴 **Delayed**: the latest baseline shows a significant deviation.
*   ⚫ **Blocked**: at least one risk has **Critical** impact.

How it's calculated: if any risk has Critical impact the status is always **Blocked**; otherwise it comes from the comparison with the most recent baseline (see "Baselines and Variance"). Without baselines the project is **On Track**.

---

## 📋 Project Charter

The Charter is your project's founding document.

### The "Project Charter" Card
*   **Sponsor**: who sponsors/funds the project (text field).
*   **Project Manager**: who leads the project (text field).
*   **Objectives**: what the project aims to achieve (text area).
*   Click **"Edit"** in the card header to modify the fields, then **"Save"** to store them.

### Markdown Sections
Below the card you'll find two sections with a Markdown editor:
*   **Goals / Problem Statement**: what problem does this project solve?
*   **Business Case**: why is this project worth doing?

Each section has an **"Edit"** / **"Save"** button. In edit mode the Markdown editor appears; after saving, the formatted content is shown. An empty section reads "No content provided. Click Edit to add."

---

## 🎯 OKRs (Objectives and Key Results)

OKRs help you track strategic objectives and measurable results.

### Creating an OKR
1. Click the **"+"** button in the **OKRs** section header.
2. Type the objective in the **"Objective..."** field (e.g., "Increase user engagement").
3. Click **"Add"** (or press Enter).

### Adding Key Results
Each OKR can have several Key Results:
1. Click the **⊕** icon ("Add Key Result") next to the OKR.
2. Fill in:
    *   **Key Result name** (e.g., "Reach 1000 daily active users").
    *   **Target**: the target value (e.g., 1000).
    *   **Unit** (e.g., "users", "%", "€").
3. Click **"Add"**.

### Tracking Progress
*   Click an OKR to **expand** its Key Results.
*   Each Key Result shows a field with the **current value** (edit it: it's saved when you leave the field), the target with its unit ("/ 1000 users") and a small progress bar.
*   The OKR's **overall progress** is the average of its Key Results. Its bar is 🟢 green (≥100%), 🔵 blue (≥50%) or 🟡 yellow (<50%).

### Deleting
*   Click the **trash icon** next to an OKR to delete it (with confirmation).
*   Click the **ⓧ** icon next to a Key Result to remove it (no confirmation).

---

## ⚠️ Risk Register

Track and manage project risks with probability and impact assessment. The red badge in the header shows the total number of risks.

### Adding a Risk
1. Click the **"+"** button in the **Risks** section header.
2. Fill in:
    *   **"Risk description..."** (e.g., "Key developer might leave mid-project").
    *   **Probability**: Low, Medium, High or Critical (default: Medium).
    *   **Impact**: Low, Medium, High or Critical (default: Medium).
    *   **"Mitigation strategy..."** (e.g., "Cross-train the team, document everything").
3. Click **"Add"**.

### Viewing Risks
*   Each risk shows its description and two badges: **P:** (probability) and **I:** (impact).
*   Click a risk to **expand** it and read the **Mitigation**.
*   Click the **trash icon** to delete it (with confirmation).

> **💡 Remember:** a risk with **Critical** impact sets the project status to **Blocked**, and risks with High or Critical impact are counted in **High Risks**.

---

## 📦 Key Deliverables

Track what your project must produce, with progress and risk status.

### Adding a Deliverable
1. Click the **"+"** button in the **Key Deliverables** section header.
2. Fill in:
    *   **"Deliverable name..."** (e.g., "User Interface Mockups").
    *   The **due date** (optional).
3. Click **"Add"**. The deliverable starts at 0% with status OK.

### Tracking Deliverables
Each deliverable shows:
*   **Name** (struck through when progress reaches 100%) and due date.
*   **Risk status dropdown**: OK, At Risk or Blocked.
*   **Progress bar** and a number field (0–100) to update the percentage.
*   **✕ button**: deletes the deliverable (with confirmation).

---

## 📈 Baselines and Variance

Baselines are a "snapshot" of the project plan at a specific moment: they let you measure how far reality is drifting from the plan.

### Creating a Baseline
1. Click the **"+"** button in the **Baselines** section header.
2. Enter a name in the **"Baseline name..."** field (e.g., "Sprint 1 Plan", "Original Timeline").
3. Click **"Create"**.

LibrePM saves each task's **planned start and finish dates** and **estimated effort**.

### Viewing Variance
At the top of the section, the **"Latest Variance"** box compares the current plan with the most recent baseline:
*   **Baseline name** and **status badge** (On Track, At Risk, Delayed).
*   **Schedule Variance**: how many days the project's planned finish has moved compared to the baseline. 🔴 Red (positive) = late, 🟢 green (negative) = early.
*   **Effort Variance**: difference in hours between the current estimated effort and the baseline's.

The status is **On Track** if neither schedule nor effort has grown, **At Risk** if the slip is at most 3 days or effort has grown by at most 10%, otherwise **Delayed**.

### The Baseline List
*   Each baseline shows its name, date and the number of tasks captured.
*   Click a baseline to see its variance (**Schedule** and **Effort**); click again to close it.
*   Click the **trash icon** to delete it (with confirmation).

---

## 👥 Team Card

A quick card showing:
*   The **Sponsor** and **Project Manager** (from the Charter).
*   If any members are overbooked from today to the end of the month, an **"N overbooked"** badge.
*   If Sponsor and Project Manager are not set: "View Team page for full roster".
*   **"Manage"** button: opens the Team page.

---

## 🔗 Quick Links

At the bottom of the right column:
*   **"Go to Gantt Chart"**: opens the Gantt view.
*   **"Resources & Workload"**: opens the Resources page.

---

### 👉 Next Step
Now that you have complete project governance, let's explore resource planning. Next chapter: **Resources and Workload**.
