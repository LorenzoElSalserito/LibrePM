# LibrePM

LibrePM is a comprehensive productivity and project management suite designed to bridge the gap between personal task management and small team collaboration. Built with a "Local-First" philosophy, it provides a robust desktop experience powered by a reusable Java Spring Boot backend, designed to be scalable for future cloud synchronization and multi-platform support (Web/Mobile).

## Project Overview

LibrePM aims to provide a consistent user experience across platforms, acting as a central hub for:
- Core Work & Project Management
- Contextual Notes & Knowledge Base
- Time Tracking, Estimates & Productivity Analytics
- Advanced Planning (WBS, Dependencies, Calendars)
- Resource Planning & Allocation
- Executive Dashboard, Tracking & Charter
- Project Templates & Guided Setup
- Local-First Data Platform & Semantic Sync

The architecture separates the frontend (Electron + React) from the backend (Java + Spring Boot), allowing the backend to serve as a headless API that can run embedded locally or hosted on a remote server.

## Key Features

### Core Productivity & Work Management
- **Advanced Task Management:** Support for priorities, deadlines, status workflows, tags, and rich Markdown notes.
- **Task Dependencies:** Implementation of blocking relationships (FS, SS, FF, SF) with lead and lag.
- **Checklists:** Granular sub-tasks within a main task to track specific steps.
- **Views:** List, Kanban Board, Timeline, Gantt, and Workload views with shared selection contexts and cross-view experience.

### Advanced Planning & Execution Control
- **WBS & Milestones:** Work Breakdown Structure numbering, summary tasks, and zero-duration milestones.
- **Execution Control:** Baselines, variance tracking (schedule, effort), forecasting, and tracking of project progress.
- **Work Calendars & Capacity Engine:** Definition of working hours, holidays, exceptions, and daily capacity rules at workspace, team, or user level.
- **Resource Allocation:** Effort assignments, workload slicing, ghost users, and over-allocation alerts.

### Executive Dashboard & Charter
- **Chart Tab:** A synthetic view containing project charter, team roles, goals, and problem statement.
- **Business Case & Key Success Metrics:** Track target vs. achieved metrics and business value.
- **Risk & Deliverable Tracking:** Visual risk register, OKRs panel, and high-level phase timelines.

### Time Tracking & Analytics
- **Focus Sessions:** Integrated timer for tracking actual work time against estimates, with heatmap visualization and session history.
- **Analytics:** Calculation of estimation deviation (Estimated vs Actual minutes) to improve planning accuracy.
- **OKR Tracking:** Objectives and Key Results alignment with project goals.

### Team & Collaboration
- **Role-Based Access Control (RBAC):** Granular permissions at the project level (Owner, Admin, Editor, Viewer).
- **Team Management:** Creation of teams and management of members, followers/watchers, and connections.
- **Ghost Users:** Ability to create placeholder users for resource planning and assignment before they have a real account.
- **Notifications:** System for tracking task assignments, status changes, and mentions.

### Technical & Architecture
- **Local-First Database:** Uses SQLite for zero-configuration local storage, with support for external supported databases.
- **Planning-Aware Semantic Sync:** 
  - **Differentiated Merge Policies:** Treats text, sets, checklists, hierarchies, and graphs differently to avoid unresolvable conflicts.
  - **Soft Delete & Conflict Review:** Entities are marked as deleted rather than removed, with manual conflict review support.
- **Project Templates:** Template Gallery (e.g., Project Timeline, Gantt Project, Event Marketing Timeline) providing structural blueprints, default views, deliverables, and metrics.
- **Integrations:** ICS calendar feed export/subscription, CSV import/export with column mapping, and transfer manifests.
- **Database Backup/Restore:** Full `.db` export and import for data portability and backups.
- **Risk Register:** Visual risk matrix with severity/likelihood scoring and mitigation tracking.
- **Asset Management:** Abstracted file storage service for task, note, and project attachments (currently local filesystem).

## Desktop Experience

The Electron desktop app provides native-quality features beyond the web UI:

- **Command Palette (Ctrl+K):** Quick navigation, task search, and rapid task/project creation from anywhere in the app.
- **Focus Timer:** Integrated Pomodoro-style timer with heatmap visualization and full session tracking history.
- **System Notifications:** Native OS notifications for task reminders, focus session events, and assignment alerts.
- **Native File Dialogs:** System file picker for import/export operations (JSON, CSV, `.db` database files).
- **Linux/Wayland Focus Management:** Robust keyboard input handling that recovers key focus after Alt+Tab, modal dismissals, and window manager focus cycles — critical for text-heavy workflows on Linux.
- **Debug Logger:** Built-in development diagnostics panel for renderer-to-main process log inspection.

## Architecture

### Electron Preload Bridge
The renderer process communicates with the backend and native OS through the `window.librepm` API surface, exposed via Electron's `contextBridge`. All IPC calls are allowlisted and serialized safely.

### Focus Management Subsystem
The `LPMWindow` bridge (`lpm:force-focus`, `lpm:force-focus-sync`, `lpm:ensure-webcontent-focus`) provides synchronized focus recovery for Linux/Wayland environments where `webContents` can lose key focus independently of the window manager's focus state.

### IPC Channel Conventions
- `lpm:*` — Focus management (synchronous where needed for input event timing)
- `backend:*` — Backend lifecycle and port discovery
- `data:*` — Import/export with native dialogs
- `window:*` — Window controls (minimize, maximize, close)
- `app:*` — Application lifecycle events (focus, blur)

## Technology Stack

### Backend
- **Language:** Java 21
- **Framework:** Spring Boot 3
- **Persistence:** Hibernate / Spring Data JPA
- **Database:** SQLite (via JDBC)
- **Migrations:** Flyway
- **Build Tool:** Gradle

### Frontend (Desktop)
- **Runtime:** Electron
- **Framework:** React
- **Styling:** Bootstrap / Custom CSS

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 21 or higher
- Node.js and npm (for the frontend)

### Running the Backend
The backend is a standard Gradle project. You can run it directly from the root directory:

```bash
./gradlew bootRun
```

By default, the application runs with the `desktop` profile, using a local SQLite database located in the user's home directory (or configured path).

### Building the JAR
To create a standalone executable JAR file:

```bash
./gradlew bootJar
```

The output file will be located in `build/libs/`.

## Configuration

The application is configured via `application.yml`. Key configuration properties include:

- `librepm.data.path`: The root directory for database and asset storage.
- `librepm.assets.allowed-extensions`: Whitelist of file types allowed for upload.
- `spring.profiles.active`: Controls the execution mode (e.g., `desktop`, `web`, `production`).

## Database Schema

The database schema is managed via Flyway migrations located in `src/main/resources/db/migration`. This ensures that the local database schema is always consistent with the code version.

Key tables and entities include:
- `users`, `teams`, `memberships`: Identity and collaboration.
- `projects`, `tasks`, `dependencies`: Core domain entities and planning.
- `focus_sessions`: Time tracking logs.
- `user_settings`, `work_calendars`: Configuration and capacity management.
- `dashboard_widgets`, `metrics`, `deliverables`: Execution tracking and charter data.

## License

Copyright © Lorenzo De Marco 2026 (Lorenzo DM)

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

See the [LICENSE](LICENSE) file for details.