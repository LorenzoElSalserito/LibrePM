# Chapter 8: Executive Dashboard & Charter 📊

The **Charter** page (also called Executive Dashboard) is LibrePM's most powerful management view. It brings together project governance, objectives, risks, deliverables, and performance tracking in a single place.

---

## 📊 The Stats Row

At the top of the page, four summary cards give you an instant overview:

| Card | What it shows |
|------|---------------|
| **Completion** | Percentage of tasks completed in the project |
| **Overdue** | Number of tasks past their deadline |
| **Deliverables** | Completed deliverables out of total (e.g., "3/7") |
| **High Risks** | Number of risks rated as High or Critical |

A **status badge** appears at the top showing the overall project status:
*   🟢 **ON_TRACK**: Everything is going well.
*   🟡 **AT_RISK**: Some indicators need attention.
*   🔴 **DELAYED**: The project is behind schedule.
*   ⚫ **BLOCKED**: Critical blockers preventing progress.

---

## 📋 Project Charter

The Charter is the foundational document of your project. It defines:

### Charter Info Card
*   **Sponsor**: Who is sponsoring/funding the project (text field).
*   **Project Manager**: Who is leading the project (text field).
*   **Objectives**: What the project aims to achieve (text area).
*   Click **"Edit"** to modify these fields, then **"Save"** to save changes.

### Charter Sections (Markdown)
Below the info card, you'll find expandable sections with rich Markdown editing:
*   **Goals / Problem Statement**: What problem does this project solve?
*   **Business Case**: Why is this project worth doing?

Each section has an **Edit/Save** toggle button. When editing, a full Markdown editor appears. When saved, the rendered content is displayed.

---

## 🎯 OKRs (Objectives and Key Results)

OKRs help you track strategic goals and measurable outcomes.

### Creating an OKR
1. Click the **"+"** button in the OKRs section header.
2. Type the **Objective** name (e.g., "Increase User Engagement").
3. Click **"Add"**.

### Adding Key Results
Each OKR can have multiple Key Results:
1. Expand an OKR by clicking on it.
2. Click the **"+"** (plus-circle) button.
3. Fill in:
    *   **Key Result name** (e.g., "Reach 1000 daily active users").
    *   **Target value** (e.g., 1000).
    *   **Unit** (e.g., "users", "percentage", "revenue").
4. Click **"Add"**.

### Tracking Progress
*   Each Key Result shows a **current value** input and a **target value**.
*   The progress bar updates automatically based on current/target ratio.
*   Colors indicate progress: 🟢 green (>=100%), 🔵 blue (>=50%), 🟡 yellow (<50%).
*   The OKR's overall progress is the average of all its Key Results.

### Deleting
*   Click the **trash icon** next to an OKR to delete it.
*   Click the **✕** icon next to a Key Result to remove it.

---

## ⚠️ Risk Register

Track and manage project risks with probability and impact assessment.

### Adding a Risk
1. Click the **"+"** button in the Risks section header.
2. Fill in:
    *   **Risk description** (e.g., "Key developer might leave mid-project").
    *   **Probability**: Low, Medium, High, or Critical.
    *   **Impact**: Low, Medium, High, or Critical.
    *   **Mitigation strategy** (e.g., "Cross-train team members, document everything").
3. Click **"Add"**.

### Viewing Risks
*   Each risk shows its description, probability badge, and impact badge.
*   Click on a risk to **expand** and see the mitigation strategy.
*   Click the **trash icon** to delete a risk.
*   The header badge shows the total number of risks.

---

## 📦 Key Deliverables

Track what your project needs to produce, with progress and risk tracking.

### Adding a Deliverable
1. Click the **"+"** button in the Deliverables section header.
2. Fill in:
    *   **Deliverable name** (e.g., "User Interface Mockups").
    *   **Due date**.
3. Click **"Add"**.

### Tracking Deliverables
Each deliverable in the list shows:
*   **Name** (crossed out if progress reaches 100%).
*   **Risk Status dropdown**: Set to OK, At Risk, or Blocked.
*   **Progress input** (0-100): Type the completion percentage.
*   **Progress bar**: Visual indicator with color coding.
*   **✕ button**: Delete the deliverable.

---

## 📈 Baselines & Variance

Baselines let you take a "snapshot" of your project plan at a point in time, then track how reality deviates from the plan.

### Creating a Baseline
1. Click the **"+"** button in the Baselines section header.
2. Enter a **name** (e.g., "Sprint 1 Plan", "Original Timeline").
3. Click **"Create"**.

LibrePM saves a snapshot of all current tasks (dates, effort, status).

### Viewing Variance
The **Latest Variance Summary** shows at the top of the section:
*   **Baseline name** being compared against.
*   **Project Status** badge (ON_TRACK, AT_RISK, DELAYED).
*   **Schedule Variance**: Number of days ahead or behind. Red = behind, Green = ahead.
*   **Effort Variance**: Hours of effort difference.

Click on any baseline in the list to expand its detailed variance.

---

## 👥 Team Summary

A quick card showing:
*   The project **Sponsor** and **Project Manager** (from the Charter).
*   **Overbooked users**: A badge showing how many team members are overloaded.
*   **"Manage"** button: Navigates to the Team page.

---

## 🔗 Quick Links

At the bottom of the page, quick navigation buttons:
*   **Go to Gantt Chart**: Jump to the Gantt timeline view.
*   **Resources & Workload**: Jump to the resource allocation page.

---

### 👉 Next Step
Now that you have full project governance, let's learn about resource planning. Next chapter: **Resources & Workload**.
