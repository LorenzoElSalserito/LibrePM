# Chapter 9: Resources and Workload 📊

The **Resources** page (its title is **"Resources & Workload"**) gives you a bird's-eye view of how your team's time is spread across a project. It helps you spot overloaded members, find available capacity and balance workloads.

---

## 🗓️ The Resource Grid

The main view is a **grid table** with:
*   **Rows**: one row per project member, with avatar (initials circle) and name, pinned on the left. If there are tasks without an assignee in the period, a **"Non assegnato"** (unassigned) row also appears.
*   **Columns**: one column per day in the selected range, showing the weekday abbreviation and the day number.
*   **Cells**: the estimated hours of work for that member on that day (e.g., "2.5h"). Cells without load stay empty.
*   **Last column (Est. / Act.)**: for each member, the total **estimated** hours and, below, the **actual** hours of the tasks in the period. The actual value is 🔴 red if it exceeds the estimate, 🟢 green otherwise.

### Where the Hours Come From
The load is based on the **estimated effort** of the tasks assigned to the member (Chapter 3):
*   If the task has **planned start and finish**, the effort is spread evenly over every day of that range.
*   If it only has a **deadline**, all the effort is counted on the deadline day.
*   Tasks without dates, without estimated effort, or archived are not counted.

### Color Coding
Cells are colored to show the load level at a glance:
*   🟢 **Optimal (≤ 4h)**: healthy load.
*   🟡 **Full (4h - 8h)**: at capacity.
*   🔴 **Overload (> 8h)**: overloaded! Action needed.

A **legend** below the table recalls the colors. Hovering over a cell shows a tooltip with the "estimated hours".

---

## 🎛️ Controls

*   **Project Selector** (in the top bar and in the page toolbar): choose which project's resources to view. Non-archived projects are listed; the first one is selected when the page opens.
*   🔄 **Refresh** (top bar): reloads the data.
*   **Start Date**: pick the first day of the range to view.
*   **Range Length**: choose how many days to show:
    *   **7 days**: focus on the week.
    *   **14 days**: two-week sprint view.
    *   **21 days**: three-week outlook.
    *   **30 days**: one-month overview.

---

## 📋 Task Details per Cell

Click any cell in the grid to see which tasks weigh on that member on that day.

A **"Task: member name"** window opens, showing:
*   The selected day (**"Day:"**).
*   The list of tasks, each with:
    *   Task title.
    *   Estimated effort in minutes (or **"Not set"**).
    *   **"Remove assignment"** button (person-with-X icon): removes the assignee from the task.
*   If there are no tasks: "No tasks assigned on this date."

---

## 👤 Member Management

Click a **member's name** in the left column to open the **"Member Management: name"** window:
*   **Details**: the member's ID.
*   **"Remove from Project"** button: removes the member from the project (with confirmation). As on the Team page, only the project's **Owner** can do this.

> **💡 Tip:** to add members to the project or change their role, use the **Team** page (Chapter 6).

---

### 👉 Next Step
Now that you can manage workloads, let's see how to start projects from templates. Next chapter: **Templates and Gallery**.
