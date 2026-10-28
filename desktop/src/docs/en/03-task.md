# Chapter 3: Tasks, Lists, and Boards 📝

If the Project is the container, **Tasks** are the operational content. LibrePM offers advanced tools to manage them, going beyond a simple shopping list.

A task in LibrePM is an entity rich in information:
*   **Status**: Where are we at? (TODO, IN_PROGRESS, REVIEW, DONE, BLOCKED, CANCELLED)
*   **Priority**: How urgent is it? (Visible at a glance).
*   **Time**: Deadlines, planning and real-time tracking (Focus Timer).
*   **Sub-tasks**: Detailed checklists.
*   **Context**: Notes, attachments, and discussions.

LibrePM gives you three main entry points to your tasks:
1.  **Inbox**: To capture an idea on the fly without deciding yet which project it belongs to.
2.  **Planner (List)**: For a structured and sortable overview.
3.  **Kanban (Board)**: For a visual and dynamic workflow.

Planner, Kanban, Gantt and Calendar always work on the **current project**, which you can change from the selector at the top of each page.

---

## 📥 Inbox: Quick Capture

The **"Inbox"** page in the sidebar is your idea tray: *"Quick capture tasks — move them to a project when ready."*

### Adding a task
1. Type the title in the **"Quick add task..."** field at the top.
2. Press Enter or click **"Add"**.

The task lands in your personal Inbox, without having to choose a project. A badge in the top-right corner shows how many tasks are in the Inbox.

### What you see for each task
*   Title and description, if any.
*   Colored **status** and **priority** badges.
*   The **deadline**, if set.

### Moving a task into a project
1. Click **"Move"** next to the task.
2. Pick the project from the **"Select project"** menu.
3. Confirm with the green ✓ button (or cancel with ✕).

The task leaves the Inbox and appears in the chosen project (Planner, Kanban, etc.).

If the Inbox is empty you'll see: *"Your inbox is empty. Add a task to get started."*

---

## 📝 Advanced Task Creation

From the Planner and the Kanban board, the **"+ Task"** button at the top opens the full editor (the Gantt also has **"Add Task"**; in the Calendar just click a day).

### The Task Editor
It's not a simple entry window, but a command center. For a new task the title is **"New Task"**; for an existing task it is **"Edit Task"** and **three tabs** appear: **Details**, **Notes & Discussion** and **History**.

#### Tab 1: Details
1.  **Title** (Required): What do you need to do? (e.g., "Write email"). Maximum 500 characters.
2.  **Short Description**: A brief explanation of the task.
3.  **Status**: Select from a dropdown with the workflow statuses loaded from the system (TODO, IN_PROGRESS, REVIEW, DONE, BLOCKED, CANCELLED). The default for new tasks is TODO.
4.  **Priority**: How urgent is it? Select from the system priorities — each has its own color for quick identification:
    *   ⚪ **LOW**: Can wait (grey).
    *   🔵 **MEDIUM**: Normal (blue, default).
    *   🟠 **HIGH**: Important (orange).
    *   🔴 **CRITICAL**: Do it NOW! (red).
5.  **Deadline**: By when? Choose a date with the picker. You can clear it if the task has no deadline.
6.  **Owner**: Who is responsible? Select from the project team members.
7.  **Planning & Gantt** (dedicated box):
    *   **Type**: Choose the task type: Task, Milestone, Meeting, Call, Appointment, Deadline, Reminder, Task Block, Summary Task, Deliverable Task, Review Task, Finance Task. Selecting **Milestone** makes the finish equal to the start and the effort becomes "0 min".
    *   **Planned Start**: When should it start? Select date and time.
    *   **Planned Finish**: When should it end? If it is empty when you pick the start, it is automatically set to start + 1 day. It cannot be earlier than the start.
    *   **Estimated Effort**: The time needed, in minutes. A calculator icon (🔢) appears when the effort is **auto-calculated from dates**: LibrePM counts working days (Mon–Fri) between start and finish (at least 1), multiplied by 8 hours (480 min/day). You can always override it manually. From 480 minutes upwards a label shows the equivalent in days (e.g., "≈ 5.0 days").
8.  **Advanced Planning** (collapsible section, click its title to open it): **Actual Start**, **Actual End**, **Est. Hours**, **Actual Hours**, and the **Deliverable** and **Stakeholder** fields where you enter the ID of a linked deliverable or stakeholder.
9.  **Finance Context** (collapsible section): the **Funding Context** field to note where the money comes from (e.g., "Grant XYZ, Budget line 42").
10. **Checklist**: Break down a complex task into smaller steps.
    *   *Example*: Task "Prepare Release" → Checklist: "Update version", "Write changelog", "Upload build".
    *   Type an item and press Enter (or click the **+** button) to add it.
    *   Check items off as you go. A percentage indicator shows progress (e.g., "67% completed").
    *   Click the **✕** button to remove an item.
11. **Tags**: Add labels to categorize your tasks.
    *   Type a tag name and press Enter (or click **+**) to add it.
    *   Tags appear as badges. Click the **✕** on a tag to remove it.
    *   Useful for grouping tasks by topic (e.g., "frontend", "urgent", "design").
12. **Markdown Notes (Personal)**: A text editor for formatted notes with styles, links, and code. Turn on the **Preview** switch to see how it renders.
13. **Attachments**: Drag files (Drag & Drop) directly onto the upload zone or click to browse.
    *   **New task**: files are uploaded when you save the task.
    *   **Existing task**: the **Attachments** box lists the files already there with size, uploader and date. Click **Upload file** (or drop files on the box) to upload them right away, without saving the task.
    *   Click the **download** icon to open or save a file. This also works for files a colleague uploaded from another PC: the content travels with the database (Chapter 21).
    *   Only **the uploader** sees the trash icon: after confirming **"Delete for everyone?"** the file disappears for every project member.
    *   Maximum **50 MB** per file. Accepted formats: images (png, jpg, jpeg, webp, gif), pdf, text (txt, md, json, csv), Office and OpenDocument documents (doc, docx, xls, xlsx, ppt, pptx, odt, ods, odp, rtf) and zip.
    *   Project attachments (not tied to a task) are managed from the **Project files** box on the Dashboard.

#### Tab 2: Notes & Discussion (existing tasks only)
A **chat interface** built into the task to communicate with the team. Each message is a **note attached to the task**, so you will also find it in the Notes page (Chapter 5):
*   At the top, the **Linked Notes** box lists the notes that reference this task; clicking one opens it in the Notes page.
*   Your messages appear on the right (blue), those of other members on the left (grey) with their initials.
*   Each message shows author, date/time, and content.
*   Type in the **"Write a note..."** field and press Enter or the send button.
*   On **your** messages you'll find the pencil (opens the note in the Notes page to edit it) and the trash can (delete, with confirmation). Other people's messages are read-only.
*   The other project members get a notification about the new note.

> **💡 Note:** This discussion belongs to a single task. For a general project chat use the **Messages** page (Chapter 19).

#### Tab 3: History (existing tasks only)
Shows the task's **status changes**: for each step you see previous status → new status and the date/time. If there are no changes you'll see *"No history yet"*.

### Saving the Task
*   Click **"Create Task"** (for new) or **"Update"** (for existing). The button is on the Details tab.
*   The **"Close"** button closes the editor, discarding unsaved changes.
*   **Request Review** (existing tasks only, Details tab): pick a project member from the menu; they will receive a review-request notification.

---

## 📋 The Planner: Order and Precision

Go to the **"Planner"** page (or, from Project Detail, click **"Go to Planner"**).

The **Planner** view is ideal for planning and organizing.

### Header Controls
*   **Project Selector**: A dropdown to switch between your projects. Choosing the **"Select project..."** entry takes you to the Projects page.
*   **Search** (**"Search..."**): Filters tasks by looking for the text in the title or description.
*   **+ Task**: The blue button to create a new task (disabled if no project is selected).

### The Task List
Here you see your tasks one below the other, each with:

*   **Drag Handle** (⋮⋮): Grab the row to **reorder tasks** by dragging them up or down. LibrePM saves the new order automatically.
*   **Checkbox**: Click the box to toggle a task between DONE and TODO. Completed tasks appear struck through and grey.
*   **Title**: Click on it to open the full Task Editor.
*   **Visual Indicators**:
    *   🔴 **Overdue Badge**: If the deadline has passed and the task is not DONE, a red "OVERDUE" badge appears and the row gets a red border.
    *   🎨 **Priority Badge**: A colored badge with the priority name.
    *   📅 **Deadline**: The due date with a calendar icon.
    *   👤 **Owner** and ✅ **Assignee**: Who is responsible for the task.
    *   ☑️ **Checklist Progress**: How many sub-items are completed (e.g., "3/5").

### Quick Actions
*   **Notes Button**: A button on each task that takes you directly to the Notes page filtered on that task, without opening the full editor.
*   **Three-Dot Menu** (⋮):
    *   **Mark as complete** / **Reopen**: Changes the task status.
    *   **Delete Task**: Removes the task (with confirmation).

### Empty State
If there are no tasks, you'll see a message: *"No tasks found. Click on '+ Task' to create one."*

---

## 📌 The Kanban Board: The Workflow

Go to the **"Kanban"** page. Here everything is more visual!

### 1. Dynamic Columns
Columns are loaded from the system statuses: **To Do**, **In Progress**, **Review**, **Done**, **Blocked** and **Cancelled**. Each column header shows:
*   The **status icon** and **name** on a colored background.
*   A **count badge** showing how many tasks are in that column.

### 2. Intelligent Drag & Drop
*   **Grab and Move**: Click a card, hold, and drag it to another column.
*   Release the mouse. The task status changes **automatically and instantly** (a message confirms "Task moved to ...").
*   The dragged card tilts slightly with a shadow effect.
*   A dashed blue border highlights the target column.

🎉 **Magic!** You don't need to open any window to change the status.

### 3. The Enhanced Card
Every card on the board is packed with features accessible without opening it:
*   **Colored Border**: The left side of the card has the **Priority** color (red CRITICAL, orange HIGH, blue MEDIUM, grey LOW).
*   **Title**: The task name, highlighted.
*   **Description**: A truncated (2-line) preview of the task description.
*   **Priority Badge**: A colored label with the priority name.
*   **Checklist Badge**: See sub-task progress at a glance (e.g., `2/4`).
*   **Deadline**: Day and month of the deadline. If overdue, it turns **red** with a warning icon.
*   **Integrated Focus Timer** ⏱️:
    *   Each card has a **Play (▶)** button ("Start timer"). Click it to start a focus session on that task.
    *   **Automation**: When you start the timer, if the task is not already "In Progress", LibrePM automatically moves it to **"In Progress"**!
    *   The right border turns **green** to show the timer is running. You can have timers running on several cards at the same time.
    *   Click **Stop (■)** ("Stop timer") to end the session.
    *   Recorded sessions feed the Focus Heatmap in Analytics (Chapter 11). To log hours on a task with a timer or manual entries there is also the **Time Tracking** page (Chapter 20).
*   **Quick Menu** (⋮): The three dots in the top right offer **Edit** (opens the editor) and **Delete** (with confirmation).

### 4. Filters and Controls
*   **Project Selector**: Changes the project shown on the board.
*   **"Search tasks..."**: Filters the board instantly by title.
*   **+ Task**: Creates a new task.
*   **Refresh** (🔄): Reloads tasks and running timers from the server.

---

## ✅ Checklist: The Small Pieces

Sometimes a task is big (e.g., "Pack the suitcase").
Instead of creating 10 small tasks ("T-shirts", "Pants", "Shoes"...), you can use a **Checklist** inside the big task!

1. Open a task (click it in the Planner, or use **Edit** on the Kanban card).
2. Scroll down to "Checklist".
3. Type an item (e.g., "Toothbrush") and press Enter.
4. Type another one (e.g., "Charger").
5. Click **"Update"** to save.

Now, as you pack things, check the items off in the checklist. When everything is done, mark the big task as "Done"!

---

## ⚡ Maximum Productivity (Tips)

1.  **Clear your mind**: During the day, drop every idea into the **Inbox**; move it to a project when you have time.
2.  **Start the day**: Open the Kanban, look at the "In Progress" column.
3.  **Focus**: Click "Play" on the most important task. The timer starts, the status updates.
4.  **Execution**: Work. If you need info, open the task and check Attachments or the Checklist.
5.  **Completion**: When you're done, drag the card to "Done". Stop the timer if you haven't already.
6.  **Review**: At the end of the day, look at the "Done" column to reward yourself for the work done!

---

### 👉 Next Step
Now that you master Tasks, it's time to look at time differently. In the next chapter, we will explore the **Gantt** and how to manage time dependencies.
