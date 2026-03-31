# Chapter 3: Tasks, Lists, and Boards 📝

Here we are! If the Project is the box, the **Tasks** are the things you have to put inside to say "Done!".
A task can be anything:
*   "Go grocery shopping"
*   "Write the report"
*   "Call the client"

A task in LibrePM is a rich entity packed with information:
*   **Status**: Where are we? (To Do, In Progress, Done, Blocked...)
*   **Priority**: How urgent is it? (Visible at a glance).
*   **Time**: Deadlines and real-time tracking (Focus Timer).
*   **Sub-tasks**: Detailed checklists.
*   **Context**: Notes, attachments, and discussions.

LibrePM gives you two main views:
1.  **Planner (List)**: A structured, sortable overview of everything.
2.  **Kanban (Board)**: A visual, dynamic workflow.

---

## 📝 Advanced Task Creation

Wherever you are (Planner or Kanban), the **"+ Task"** button opens the full editor.

### The Task Editor (TaskEditorModal)
This is not a simple input window, but a full command center with **two tabs**:

#### Tab 1: Details
1.  **Title** (Required): What do you have to do? (e.g., "Write email"). Maximum 500 characters.
2.  **Short Description**: A brief explanation of the task.
3.  **Status**: Select from a dropdown of workflow statuses loaded from the system (e.g., TODO, IN_PROGRESS, DONE, REVIEW, BLOCKED). The default is "TODO" for new tasks.
4.  **Priority**: How urgent is it? Select from system priorities — each has its own color for quick identification:
    *   🔵 **Low**: Can wait.
    *   🟢 **Medium**: Normal (default).
    *   🟠 **High**: Important.
    *   🔴 **Critical**: Do it NOW!
5.  **Deadline**: By when? Pick a date using the date picker. You can clear it if the task has no deadline.
6.  **Owner**: Who is responsible? Select from the project's team members.
7.  **Checklist**: Break down a complex task into smaller steps.
    *   *Example*: Task "Prepare Release" → Checklist: "Update version", "Write changelog", "Upload build".
    *   Type an item and press Enter (or click the **+** button) to add it.
    *   Check items off as you complete them. A percentage indicator shows your progress (e.g., "67% completed").
    *   Click the **✕** button to remove an item.
8.  **Tags**: Add labels to categorize your tasks.
    *   Type a tag name and press Enter (or click **+**) to add it.
    *   Tags appear as colored badges. Click the **✕** on a tag to remove it.
    *   Useful for grouping tasks by topic (e.g., "frontend", "urgent", "design").
9.  **Planning & Gantt** (collapsible section):
    *   **Type**: Choose the task type (Task, Milestone, Meeting, Call, etc.). Selecting "Milestone" automatically sets duration to zero.
    *   **Planned Start**: When should this task begin? Pick a date and time.
    *   **Planned Finish**: When should it end? If left empty, it defaults to start + 1 day.
    *   **Estimated Effort**: The time needed, in minutes. A calculator icon (🔢) appears when the effort is **auto-calculated from dates**: LibrePM counts the business days (Mon–Fri) between start and finish, multiplied by 8 hours (480 min/day). You can always override it manually. A helper label shows the equivalent in days (e.g., "≈ 5.0 days").
10.  **Markdown Notes (Personal)**: A rich text editor for personal notes, formatted with headings, bold, italic, lists, links, and code blocks. Toggle the **Preview** switch to see how it renders.
11. **Attachments (Assets)**: Drag files directly into the dropzone (Drag & Drop) or click to browse. Documents, images, or logs are always at hand.
    *   Maximum 10MB per file.
    *   Existing attachments can be downloaded or removed.

#### Tab 2: Notes & Discussion (only when editing an existing task)
A **chat-style interface** integrated into the task for team communication:
*   See all notes from team members in a conversation view.
*   Your messages appear on the right (blue), others' on the left (gray).
*   Each message shows the author, timestamp, and content.
*   Type a message and click the **Send** button (or press Enter).
*   You can **edit** or **delete** your own messages.

### Saving the Task
*   Click **"Create Task"** (for new) or **"Update"** (for existing).
*   The **"Close"** button discards unsaved changes.

---

## 📋 The Planner: Order and Precision

Go to the **"Planner"** page (or open your project and click "Planner").

The Planner view is ideal for planning and organizing.

### Header Controls
*   **Project Selector**: A dropdown at the top to switch between your projects.
*   **Search**: A text field to filter tasks by title instantly.
*   **+ Task**: The blue button to create a new task (disabled if no project is selected).

### The Task List
Here you see your tasks one below the other, each with:

*   **Drag Handle** (≡): Grab this icon on the left to **reorder tasks** by dragging them up or down. LibrePM saves the new order automatically.
*   **Checkbox**: Click the square to toggle a task between Done and To Do. Done tasks appear crossed out and grayed.
*   **Title**: Click on it to open the full Task Editor.
*   **Indicators**:
    *   🔴 **Overdue Badge**: If the deadline has passed, a red "OVERDUE" badge appears.
    *   🎨 **Priority Badge**: A colored badge showing the priority level.
    *   📊 **Checklist Progress**: Shows how many sub-items are completed (e.g., "3/5").
    *   📅 **Deadline**: The due date with a calendar icon.
    *   👤 **Owner**: Who is responsible.
    *   ✅ **Assigned**: Who it's assigned to.

### Quick Actions
*   **Notes Button**: A dedicated button on each task that takes you directly to the Notes page for that task, without opening the full editor.
*   **Three Dots Menu** (⋮): Click for quick actions:
    *   **Mark as complete** / **Reopen**: Toggle the task status.
    *   **Delete**: Remove the task (with confirmation).

### Empty State
If there are no tasks, you'll see a message: *"No tasks found. Click on '+ Task' to create one."*

---

## 📌 The Board (Kanban)

Go to the **"Kanban"** page. Here everything is more visual!

The LibrePM Kanban is much more than a series of columns. It's an interactive tool designed for *flow*.

### 1. Dynamic Columns
The columns reflect your real workflow. They are loaded dynamically from the system and can include statuses like `To Do`, `In Progress`, `Review`, `Done`, `Blocked`. Each column header shows:
*   The **status icon** and **name** on a colored background.
*   A **count badge** showing how many tasks are in that column.

### 2. Intelligent Drag & Drop
*   **Take and Move**: Click a card, hold and drag it to another column.
*   Release the mouse. The task status changes **automatically and instantly**.
*   The cards adapt visually while dragging (slight rotation and shadow effect).
*   A dashed blue border highlights the target column.

🎉 **Magic!** You don't have to open any window to change status.

### 3. The Enhanced Card
Each card on the board is rich with features accessible without opening it:
*   **Colored Left Border**: Indicates the **Priority** (e.g., Red for Critical, Yellow for High, Green for Medium, Gray for Low).
*   **Title**: The task name, prominently displayed.
*   **Description**: A truncated preview (2 lines) of the task description.
*   **Priority Badge**: A colored label showing the priority name.
*   **Checklist Badge**: At-a-glance progress of sub-items (e.g., `2/4`).
*   **Deadline**: The due date. If overdue, it turns **red** with a warning icon.
*   **Focus Timer** ⏱️ (Integrated):
    *   Each card has a **Play (▶)** button. Click it to start the timer on that task.
    *   **Automation**: When you start the timer, if the task was in "To Do", LibrePM automatically moves it to **"In Progress"**!
    *   The right border turns **green** to indicate it's the active task.
    *   Click **Stop (■)** to end the timer session.
*   **Quick Menu** (⋮): The three dots in the top right allow quick **Edit** and **Delete**.

### 4. Real-Time Filters
Use the **search bar** in the top header to filter the board instantly by title. Useful when you have dozens of tasks and need to find a specific one.

The **Refresh** button (🔄) reloads all tasks from the server.

---

## ✅ Checklist: The Small Pieces

Sometimes a task is big (e.g., "Pack suitcase").
Instead of creating 10 small tasks ("T-shirts", "Pants", "Shoes"...), you can use a **Checklist** inside the big task!

1. Open a task (click on it).
2. Scroll down to "Checklist".
3. Write an item (e.g., "Toothbrush") and press Enter.
4. Write another one (e.g., "Charger").

Now, as you put things in the suitcase, check the boxes in the checklist. When you have finished everything, mark the big task as "Done"!

---

## ⚡ Productivity at Maximum (Tips)

1.  **Start your day**: Open the Kanban, look at the "In Progress" column.
2.  **Focus**: Click "Play" on the most important task. The timer starts, the status updates.
3.  **Execution**: Work. If you need info, open the task and check Attachments or the Checklist.
4.  **Completion**: When you're done, drag the card to "Done". Stop the timer if you haven't already.
5.  **Review**: At the end of the day, look at the "Done" column to appreciate your progress!

---

### 👉 Next Step
Now you know how to manage daily work. But if the project is large and complex? You need a precise timeline. In the next chapter, we will see the **Gantt**.
