# Chapter 18: Privacy & Compliance 🛡️

LibrePM keeps your data on your computer (or on the shared database configured by your administrator) and offers a few tools to handle requests about personal data.

---

## 🔐 Your Data (Settings)

Go to **Settings** and scroll to the **"Your Data"** card at the bottom of the page. You'll find two buttons:

*   **"Export My Data"**: records a request to export your personal data. You'll see the message *"Data export requested"*.
*   **"Request Data Deletion"**: after a confirmation (*"Are you sure you want to request data deletion? This action cannot be undone."*) records a deletion request. You'll see the message *"Deletion request submitted"*.

> **📌 Important:** These buttons **record a request** (in "pending" state) that must then be handled by whoever administers the installation: they do not produce a file and do not delete anything immediately. To get a copy of your data right away, use the database export described in chapters 5, 12 and 13 instead.

---

## 🗃️ Data Categories & Retention

LibrePM's database already contains five predefined data categories, each with a sensitivity level and a suggested retention period:

| Category | Sensitivity | Suggested retention |
|----------|-------------|---------------------|
| Personal Data | Sensitive | 3 years |
| Financial Data | Restricted | 10 years |
| Project Data | Normal | no expiry |
| Evidence | Sensitive | 10 years |
| Audit Logs | Normal | 2 years |

> **📌 Note:** Categories and retention policies are backend configuration: they are currently neither visible nor editable in the interface, and LibrePM does not automatically delete data when these periods expire.

---

## ♿ Accessibility

Some measures already present in the interface:
*   **Reduced motion**: if you enabled reduced motion in your operating system (`prefers-reduced-motion`), LibrePM keeps animations and transitions to a minimum.
*   **Accessible side navigation**: sidebar entries are labelled buttons reachable with the Tab key; the active page is also announced to screen readers. When the sidebar is collapsed, the page name appears as a tooltip on hover.
*   **Keyboard shortcuts**: the Command Palette (**Ctrl+K**) and the other shortcuts described in Chapter 1 let you move around without a mouse.
*   **Light and dark theme**: pick the one that reads best for you in **Settings** (Chapter 13).

---

### 👉 Next Step
You've seen every governance module, from finance to compliance. What's left are the tools for working together: next chapter, **Project Messages**.
