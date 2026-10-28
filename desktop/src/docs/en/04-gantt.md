# Chapter 4: Time and Planning (Gantt) 🗓️

If Tasks are "What to do", the **Gantt** is "When to do it and in what order".
It is a visual calendar that shows you the entire project on a timeline.

---

## 📅 What is a Gantt?

Imagine a chart with dates at the top (January, February...) and your tasks on the left.
Each task is a colored bar that goes from a start date to an end date.

The longer the bar, the more time it takes.

Go to the **"Gantt"** page in the sidebar: the chart shows the tasks of the **current project**, which you can change with the selector at the top.

### Where do the dates come from?
Bars use the task's **Planned Start** and **Planned Finish** (the "Planning & Gantt" section of the Task Editor, Chapter 3). If a task doesn't have them, LibrePM estimates them so it can still be drawn:
*   **Start**: from the deadline minus the estimated duration (effort), or the day before the deadline, or the creation date.
*   **End**: from the deadline, or start + effort, or start + 1 day.

These estimates are only used for display: for precise planning, set the dates in the editor.

---

## 🔗 Dependencies: "First this, then that"

In a complex project, you can't do everything at once.
Example: "First I have to buy the paint (Task A), then I can paint the wall (Task B)".
You can't paint without paint!

A dependency says: **"Task B depends on Task A"**.
In the Gantt it appears as an **arrow** connecting A to B.

The planning engine supports four dependency types:
*   **FS (Finish-to-Start)**: The most common. B starts after A finishes.
*   **SS (Start-to-Start)**: B starts when A starts.
*   **FF (Finish-to-Finish)**: B finishes when A finishes.
*   **SF (Start-to-Finish)**: B finishes when A starts.

### Where do they come from?
In the current version **there is no command in the interface to draw or edit dependencies** (you cannot drag arrows between bars). Dependencies are created automatically when you start a project from a **Gallery template** (Chapter 10) that includes them.

### What happens when a task changes?
When you save a task after changing its planned start, planned finish or effort, the **Planning Engine** recalculates the project plan:
*   Tasks that depend on the changed one are **pushed forward** if needed, so the dependency is respected.
*   For tasks with an estimated effort, the planned finish is recalculated from the start and the effort, counting working time only.
*   Milestones stay zero-length (finish = start).

---

## 💎 Milestones: "Important Checkpoints"

**Milestones** are special moments in the project.
They are not things to do (they last zero minutes), but they are **important goals**.
Example: "Project delivery", "Client approval", "Website launch".

In the Gantt, Milestones are highlighted in **orange** (in the legend you recognize them by the diamond ♦️).
They help you see crucial dates immediately.

### How to create them?
In the Task Editor, in the **"Type"** field, choose **"Milestone"**.
The planned finish automatically equals the start and the effort becomes "0 min".

---

## 🖱️ Moving in Time

### View Modes
In the top right, you'll find three **view buttons** to change the time scale:
*   **Day**: See every single day — ideal for short-term planning.
*   **Week**: See the project by weeks — the default view.
*   **Month**: See the big picture — ideal for long-term projects.

The selected mode is shown in bold.

The floating **"Today"** button in the corner of the chart brings the view back to today's date (the Gantt also centers on today when it opens).

### Interactive Features
*   **Click to Edit**: Click a bar to see a summary (title, CRITICAL badge if any, type, start, end, progress, status) and open the Task Editor to update details, dates, or effort.
*   **No dragging**: Bars cannot be moved or stretched with the mouse; to change dates use the Task Editor.
*   **Progress**: The completion percentage is calculated automatically: from the checklist if the task has one, otherwise from the ratio of actual to estimated effort. Tasks in a completed status always count as 100%.

### Toolbar Buttons
*   **Project selector** and **+ Task** in the page header.
*   **🔄 Refresh**: Reloads all Gantt data from the server.
*   **+ Add Task**: Create a new task directly from the Gantt view (opens the Task Editor).

If the project has no tasks, you'll see a message inviting you to add tasks with start and end dates.

---

## 📊 The Legend

When the project has tasks, a **legend** appears next to the "| Timeline" title (on wide enough windows):
*   🔴 **Critical**: tasks on the critical path.
*   🔶 **Milestone**: zero-duration checkpoints.
*   🔵 **Task**: standard tasks.

**Summary Tasks** (type "Summary Task") are drawn in **dark grey**.

---

## 📐 WBS Codes

When a task is part of a WBS (Work Breakdown Structure), its **WBS code** (e.g., "1.1", "1.2.3") appears before the title on the bar and in the summary. It helps identify the position of each task in the project structure.

> **📌 Note:** In the current version the interface has no WBS structure editor: codes only appear for tasks that already have a WBS node assigned. If no task has one, bars simply show the title.

---

## 🚨 The Critical Path

Some tasks are more important than others to finish on time.
If you delay "Buying chairs", maybe nothing happens.
But if you delay "Building the roof", you delay the whole house!

Tasks that cannot be delayed without delaying the entire project form the **Critical Path**.
LibrePM colors them **Red** in the Gantt and shows the **CRITICAL** badge in the summary. Keep an eye on them!

The critical path is calculated automatically by LibrePM's **Planning Engine** using the CPM (Critical Path Method) algorithm, based on planned dates and the dependencies between tasks.

---

### 👉 Next Step
Now you know how to plan like a pro. But where do you put all the information, ideas, and documents? In the next chapter, we'll talk about **Notes**.
