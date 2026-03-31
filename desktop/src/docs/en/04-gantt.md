# Chapter 4: Time and Planning (Gantt) 🗓️

If Tasks are "What to do", the **Gantt** is "When to do it and in what order".
It is a visual calendar that shows you the whole project on a timeline.

---

## 📅 What is a Gantt?

Imagine a chart with dates at the top (January, February...) and your tasks on the left.
Each task is a colored bar that goes from a start date to an end date.

The longer the bar, the more time it takes.

---

## 🔗 Dependencies: "First this, then that"

In a complex project, you can't do everything at once.
Example: "First I have to buy the paint (Task A), then I can paint the wall (Task B)".
You can't paint without paint!

LibrePM allows you to say: **"Task B depends on Task A"**.
This creates an arrow that connects the end of A to the start of B.

LibrePM supports four types of dependency:
*   **FS (Finish-to-Start)**: The most common. B starts after A finishes.
*   **SS (Start-to-Start)**: B starts when A starts.
*   **FF (Finish-to-Finish)**: B finishes when A finishes.
*   **SF (Start-to-Finish)**: B finishes when A starts.

### How to create them?
1. In the Gantt, you see a small dot at the end of the Task A bar.
2. Click and drag that dot to the beginning of the Task B bar.
3. Release.

🎉 **Done!** Now there is an arrow. If you move Task A forward (delay!), Task B will move automatically to respect the rule.

> **📌 Note:** Dependency creation via drag is a planned feature. Currently, dependencies can be managed through the backend and are fully visualized in the Gantt view with connecting arrows.

---

## 💎 Milestones: "Important Checkpoints"

**Milestones** are special moments in the project.
They are not things to do (duration zero minutes), but they are **important achievements**.
Example: "Project Delivery", "Client Approval", "Website Launch".

In the Gantt, Milestones are not bars, but **diamonds (♦️)** colored in yellow.
They serve to see crucial dates immediately.

### How to create them?
When creating a task, in the "Type" field, choose **"Milestone"**.
Its duration will automatically become 0 and it will appear as a diamond in the Gantt.

> **📌 Note:** Milestone creation through a dedicated "Type" field is a planned enhancement. Currently, milestones are identified by backend criteria and displayed as diamond shapes in the Gantt view.

---

## 🖱️ Moving in Time

The Gantt is interactive!

### View Modes
At the top right, you'll find three **view buttons** to change the time scale:
*   **Day**: See every single day — ideal for short-term planning.
*   **Week**: See the project by weeks — the default balanced view.
*   **Month**: See the big picture — ideal for long-term projects.

The currently selected mode is highlighted in blue.

### Interactive Features
*   **Click to Edit**: Click on any task bar to open the Task Editor and update details, dates, or effort.
*   **Progress**: The completion percentage is calculated automatically from checklist items or actual vs. estimated effort.

### Toolbar Buttons
*   **🔄 Refresh**: Reload all Gantt data from the server.
*   **+ Add Task**: Create a new task directly from the Gantt view (opens the Task Editor).

---

## 📊 The Legend

When your project has tasks on the critical path, a **legend** appears below the toolbar:
*   🔴 **Critical Path**: Red — tasks that cannot be delayed without delaying the whole project.
*   ⬜ **Summary Task**: Gray — parent tasks that group other tasks.
*   🟡 **Milestone**: Yellow with border — zero-duration checkpoints.

---

## 📐 WBS Codes

Each task in the Gantt may display a **WBS code** (Work Breakdown Structure) — a hierarchical numbering system (e.g., "1.1", "1.2.3") that helps identify each task's position in the project structure.

---

## 🚨 The Critical Path

Some tasks are more important than others to finish on time.
If you delay "Buy chairs", maybe nothing happens.
But if you delay "Build roof", you delay the whole house!

Tasks that cannot be delayed without delaying the whole project form the **Critical Path**.
LibrePM colors them **Red** in the Gantt. Keep an eye on them!

The critical path is calculated automatically by LibrePM's **Planning Engine** using the CPM (Critical Path Method) algorithm, taking into account all dependencies between tasks.

---

### 👉 Next Step
Now you know how to plan like a pro. But where do you put all the information, ideas, and documents? In the next chapter, we will talk about **Notes**.
