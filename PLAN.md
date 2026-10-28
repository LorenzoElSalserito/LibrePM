# PLAN.md

## Piano operativo prioritario — 2026-10-16

1. Stabilità prima del rilascio: navigazione integrale, lifecycle Gantt e risposte HTTP corrette.
2. Design system condiviso, temi, focus e verifiche sulla build distribuita.
3. Messaggistica persistente con permessi di progetto, validazione e polling limitato.
4. MariaDB: provisioning esplicito, collegamento API remoto, test database reale e runbook.
5. Gate qualità: test Java, E2E Chromium con backend SQLite reale, regressione packaging e rapporto riproducibile.
6. Gate ancora necessari per distribuzione pubblica: IdP reale e onboarding remoto, verifica degli aggiornamenti futuri MariaDB, tutte le operazioni CRUD dei moduli, installatori per ogni OS, accessibilità e carico.

Stato di esecuzione ed evidenze: `reconstruction_ui.md` e `QUALITY_REPORT.md`. Il piano storico seguente resta valido solo dove compatibile con PRD revisionato.


**Prodotto:** LibrePM — European Project Governance Platform  
**Documento:** Piano evolutivo scrum-ready per team junior  
**Versione:** 3.0  
**Data:** 2026-05-18

---

## 1. Principio operativo

Questo piano è scritto per sviluppatori junior. Ogni sprint è verticale: deve produrre un pezzo funzionante attraversando schema, database, backend, API, UI, test e documentazione. Non sono ammessi sprint solo frontend o solo backend salvo refactoring tecnico esplicitamente approvato.

## 2. Stack target

- Desktop: Electron.
- Frontend: React + TypeScript.
- Backend locale: Python FastAPI.
- Database: SQLite + SQLCipher o cifratura equivalente.
- ORM/query layer: SQLAlchemy o repository SQL esplicito, scelto una volta sola in Sprint 0.
- Validation: Pydantic generato da schema.
- UI state: Zustand o Redux Toolkit, scelto una volta sola in Sprint 0.
- Testing backend: pytest.
- Testing frontend: Vitest + Testing Library.
- E2E: Playwright.
- Packaging: electron-builder.
- Codegen: Python CLI in `tools/codegen`.

## 3. Regola di implementazione assoluta

Non copiare OpenProject. Implementare capability equivalenti reinterpretate per LibrePM. I nomi utente-facing possono ispirarsi agli standard di project management, ma il modello deve essere LibrePM-native.

## 4. Branching e workflow Git

- `main`: sempre stabile.
- `develop`: integrazione sprint.
- `feature/PRD-XX-short-name`: feature verticale.
- `fix/short-name`: bug fix.
- `docs/short-name`: sola documentazione.

Ogni pull request deve includere:

- link a PRD e sprint task;
- screenshot UI se c'è UI;
- lista migration;
- test eseguiti;
- nota privacy/security se la feature tocca dati personali, file, rete, token o analytics.

## 5. Definition of Ready

Una user story può iniziare solo se contiene:

- scopo utente;
- entità coinvolte;
- permessi;
- dati minimi;
- schermata prevista;
- casi di errore;
- acceptance criteria;
- eventuale export/import;
- nota privacy.

## 6. Definition of Done globale

Una storia è completa solo quando:

- schema aggiornato;
- migration applicata;
- backend repository/service implementato;
- endpoint locale implementato o motivazione se non serve;
- UI accessibile implementata;
- permessi applicati lato backend e UI;
- test unitari e integration test passano;
- test e2e happy path passa;
- nessuna chiamata rete nascosta;
- nessun secret in chiaro;
- documentazione aggiornata.

## 7. Struttura repository consigliata

```text
librepm/
  apps/
    desktop/                 # Electron shell
    backend/                 # Python FastAPI local service
  packages/
    ui/                      # componenti React condivisi
    shared/                  # tipi TS generati e utility
  schemas/                   # metamodel YAML/JSON
    entities/
    permissions/
    workflows/
    metrics/
    views/
  tools/
    codegen/                 # generatori
    migration/               # migration runner
    importers/               # OpenProject/Jira/CSV
  docs/
    adr/
    qa/
    security/
  tests/
    e2e/
```

## 8. Convenzioni UI/UX

- Ogni schermata deve essere chiara, ariosa, elegante, leggibile.
- Ogni tabella deve supportare density confortevole e compatta.
- Ogni azione primaria deve essere visibile ma non aggressiva.
- Ogni modulo avanzato deve essere disattivabile.
- Ogni messaggio di errore deve dire cosa è successo e come correggerlo.
- Ogni elemento interattivo deve essere raggiungibile da tastiera.
- Ogni vista deve avere empty state istruttivo.

## 9. Convenzioni privacy/security

- Network off by default.
- API locale off by default.
- Telemetry assente.
- Crash report solo export manuale.
- Token salvati solo in secret store.
- Webhook inbound solo con firma.
- Logs senza dati personali se non strettamente necessario.
- Export sempre esplicito.
- Delete/redact/anonymize tracciati in audit locale.

## 10. Piano sprint


    ## Sprint 00 — Bootstrap architetturale, repository e standard qualità

    **Obiettivo sprint:** Stabilire una base sviluppabile da junior senza ambiguità.

    ### Task verticali
    - [ ] Creare monorepo `apps/desktop`, `apps/backend`, `packages/shared`, `schemas`, `tools/codegen`, `docs`.
- [ ] Configurare lint, formatter, typecheck, test runner, pre-commit.
- [ ] Creare design tokens base e layout shell.
- [ ] Definire SQLite dev database e script reset.

    ### Definition of Done dello sprint
    - Repo avviabile con un comando.
- CI locale passa.
- Schermata desktop vuota mostra sidebar e status privacy/offline.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 01 — Metamodel, codegen e migration engine

    **Obiettivo sprint:** Rendere ogni dominio schema-driven.

    ### Task verticali
    - [ ] Parser YAML schema.
- [ ] Generatore TypeScript types.
- [ ] Generatore Pydantic models.
- [ ] Generatore migration SQL.
- [ ] Generatore form metadata.
- [ ] Fixture generator.

    ### Definition of Done dello sprint
    - Da schema `WorkItem` vengono generati model, type, migration e form.
- Test snapshot codegen stabile.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 02 — Workspace, project hierarchy e lifecycle base

    **Obiettivo sprint:** Creare struttura base dati e UI per workspace/progetti.

    ### Task verticali
    - [ ] CRUD Workspace.
- [ ] CRUD Project/subproject.
- [ ] Project status.
- [ ] Lifecycle state.
- [ ] Project template minimal.

    ### Definition of Done dello sprint
    - Creare progetto e sottoprogetto.
- Archiviare/ripristinare progetto.
- Nessuna rete richiesta.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 03 — WorkItem Core, tipi, stati e activity

    **Obiettivo sprint:** Implementare WorkItem come nucleo centrale.

    ### Task verticali
    - [ ] CRUD WorkItem.
- [ ] Tipi e status configurabili.
- [ ] Commenti e activity.
- [ ] Relations parent/child and related.
- [ ] Attachments metadata placeholder.

    ### Definition of Done dello sprint
    - Creare task, bug, milestone.
- Aggiungere commento e relazione.
- Activity log visibile.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 04 — Viste WorkItem: table, split, detail, saved queries

    **Obiettivo sprint:** Costruire viste operative potenti.

    ### Task verticali
    - [ ] Table view.
- [ ] Filters.
- [ ] Sort/group.
- [ ] Split view.
- [ ] Detail panel.
- [ ] Saved queries.

    ### Definition of Done dello sprint
    - Salvare vista privata.
- Filtrare milestone overdue.
- Split width persistente locale.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 05 — RBAC, permission report e modulo activation

    **Obiettivo sprint:** Proteggere funzioni e dati con RBAC.

    ### Task verticali
    - [ ] Role registry.
- [ ] Permission DSL.
- [ ] Project membership.
- [ ] Module activation.
- [ ] Permission report UI.

    ### Definition of Done dello sprint
    - Viewer non modifica WorkItem.
- Modulo finance nascosto se disattivato.
- Report permessi esportabile.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 06 — Gantt, scheduling e working days

    **Obiettivo sprint:** Raggiungere parità Gantt base.

    ### Task verticali
    - [ ] Timeline component.
- [ ] Drag resize/move.
- [ ] Working days.
- [ ] Dependencies precedes/follows.
- [ ] Manual/auto scheduling.
- [ ] Baseline comparison.

    ### Definition of Done dello sprint
    - Follower spostato automaticamente dopo predecessore.
- Non-working day evidenziato.
- Export PNG/PDF stub funzionante.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 07 — Agile boards, backlogs e sprint

    **Obiettivo sprint:** Implementare boards e sprint.

    ### Task verticali
    - [ ] Kanban by status.
- [ ] Action boards by assignee/version/custom field.
- [ ] Backlog.
- [ ] Sprint CRUD.
- [ ] WIP limit.

    ### Definition of Done dello sprint
    - Drag card cambia status.
- Sprint chiuso produce summary.
- Board non duplica dati.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 08 — Roadmap, versions, calendar e reminders

    **Obiettivo sprint:** Costruire roadmap e calendario.

    ### Task verticali
    - [ ] Version entity.
- [ ] Roadmap view.
- [ ] Calendar project/global.
- [ ] Reminder locale.
- [ ] iCal export token.

    ### Definition of Done dello sprint
    - Milestone appare su roadmap e calendario.
- Token iCal revocabile.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 09 — Time tracking, progress e cost entries

    **Obiettivo sprint:** Tracciare tempo, progresso e costi base.

    ### Task verticali
    - [ ] TimeEntry CRUD.
- [ ] Timer locale.
- [ ] Activity types.
- [ ] Progress fields.
- [ ] CostEntry CRUD.

    ### Definition of Done dello sprint
    - Time entry aggiorna spent hours.
- Timer salva solo su conferma.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 10 — Budget, project finance e cost reporting

    **Obiettivo sprint:** Project finance solido.

    ### Task verticali
    - [ ] Budget CRUD.
- [ ] Budget lines.
- [ ] Labor/unit planned costs.
- [ ] Actual costs aggregation.
- [ ] Variance calculation.
- [ ] Budget detail view.

    ### Definition of Done dello sprint
    - Budget mostra planned/spent/available.
- Cost report per periodo.
- Budget revision auditata.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 11 — Grants, funding, stakeholder e sponsor governance

    **Obiettivo sprint:** Differenziare LibrePM per mercato UE.

    ### Task verticali
    - [ ] GrantCall.
- [ ] GrantSubmission.
- [ ] FundingSource.
- [ ] SponsorCommitment.
- [ ] Stakeholder.
- [ ] Obligation register.
- [ ] Eligibility checklist.

    ### Definition of Done dello sprint
    - Da GrantCall creare progetto template.
- Obligation genera reminder.
- Funding coverage widget.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 12 — Meetings, agenda, minutes e decision log

    **Obiettivo sprint:** Meeting management completo.

    ### Task verticali
    - [ ] Meeting CRUD.
- [ ] Recurring series.
- [ ] Agenda sections/items.
- [ ] Minutes.
- [ ] Decision log.
- [ ] Action item creation.

    ### Definition of Done dello sprint
    - Da agenda item creare WorkItem.
- PDF meeting export.
- Occurrence modificabile.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 13 — Wiki, documents, file vault e knowledge macros

    **Obiettivo sprint:** Knowledge e file governance.

    ### Task verticali
    - [ ] Wiki Markdown editor.
- [ ] Wiki history/diff.
- [ ] Macros.
- [ ] Document register.
- [ ] Attachment vault.
- [ ] Nextcloud/WebDAV abstraction.

    ### Definition of Done dello sprint
    - Wiki embed WorkItem table.
- Document required da grant passa stato.
- File resta locale.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 14 — Notifications, search, command palette e global modules

    **Obiettivo sprint:** Ridurre attrito operativo.

    ### Task verticali
    - [ ] Notification center.
- [ ] Watchers.
- [ ] Mentions.
- [ ] Global search.
- [ ] Command palette.
- [ ] Global modules.

    ### Definition of Done dello sprint
    - Mention genera notifica.
- Search rispetta permessi.
- Command palette crea WorkItem.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 15 — Repository Git, GitHub/GitLab adapters e secure webhooks

    **Obiettivo sprint:** Sviluppo software integrato.

    ### Task verticali
    - [ ] Local Git browser.
- [ ] Commit reference parser.
- [ ] GitHub PR adapter.
- [ ] GitLab MR adapter.
- [ ] Webhook signature verification.
- [ ] Secret storage.

    ### Definition of Done dello sprint
    - Branch name generato.
- Webhook non firmato rifiutato.
- PR merged aggiunge activity.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 16 — Import/export, migration OpenProject/Jira/CSV/XLSX

    **Obiettivo sprint:** Migrazione e portabilità.

    ### Task verticali
    - [ ] CSV/XLSX importer.
- [ ] OpenProject mapper.
- [ ] Jira mapper.
- [ ] Markdown/ZIP wiki import.
- [ ] Dry run report.
- [ ] Workspace export package.

    ### Definition of Done dello sprint
    - Dry run non scrive dati.
- Mapper work packages→WorkItems testato.
- Export contiene manifest e checksum.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 17 — Analytics avanzate, dashboards e reports PDF/XLSX

    **Obiettivo sprint:** Analytics e report.

    ### Task verticali
    - [ ] Metric registry.
- [ ] Dashboard widgets.
- [ ] Budget report.
- [ ] Time/cost report.
- [ ] Grant health report.
- [ ] Portfolio light report.
- [ ] PDF/XLSX export.

    ### Definition of Done dello sprint
    - Nessuna metrica people scoring.
- Report esportato offline.
- Dashboard personalizzabile.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 18 — Branch evolutivi, change control e scenario merge

    **Obiettivo sprint:** Branch e change governance.

    ### Task verticali
    - [ ] ProjectBranch.
- [ ] ChangeRequest.
- [ ] Impact analysis.
- [ ] Compare branch/baseline.
- [ ] Merge proposal.
- [ ] Conflict resolver.

    ### Definition of Done dello sprint
    - Branch scenario mostra differenze.
- Merge budget richiede ruolo.
- Decision log collegato.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 19 — API locale, plugin SDK, MCP e automation

    **Obiettivo sprint:** Automation sicura.

    ### Task verticali
    - [ ] Local API toggle.
- [ ] OpenAPI generation.
- [ ] Webhooks outbound signed.
- [ ] Plugin manifest.
- [ ] MCP server optional.
- [ ] Scoped tokens.

    ### Definition of Done dello sprint
    - API off by default.
- Token read-only non scrive.
- MCP mostra capability limitate.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 20 — Privacy, security, accessibility hardening e packaging

    **Obiettivo sprint:** Rendere prodotto pronto per utenti reali.

    ### Task verticali
    - [ ] SQLCipher integration.
- [ ] Backup/restore.
- [ ] SBOM.
- [ ] Axe accessibility tests.
- [ ] Keyboard navigation.
- [ ] Packaging .deb/.exe/.dmg.
- [ ] Update channels.

    ### Definition of Done dello sprint
    - Backup ripristina checksum.
- Core screens pass accessibility.
- Installer generati.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


    ## Sprint 21 — Mobile companion e BIM/BCF opzionali

    **Obiettivo sprint:** Estensioni post-core.

    ### Task verticali
    - [ ] Responsive mode.
- [ ] Companion PWA prototype.
- [ ] BCF issue model.
- [ ] IFC adapter placeholder.
- [ ] PM² and BIM templates.

    ### Definition of Done dello sprint
    - Core non dipende da mobile/BIM.
- BCF issue crea WorkItem.
- PWA read-mostly.

    ### Istruzioni per junior developer
    - Prima creare o aggiornare schema in `schemas/`.
    - Poi eseguire codegen.
    - Poi implementare repository/service backend.
    - Poi implementare API locale.
    - Poi implementare UI.
    - Poi scrivere test unitari e almeno un test e2e happy path.
    - Non hardcodare campi o status se esistono nel metamodel.
    - Non introdurre chiamate di rete non esplicitamente richieste dal task.


## 11. Backlog tecnico trasversale

### Testing

- Creare factory fixture da schema.
- Testare ogni permission con almeno allow e deny.
- Testare import/export roundtrip per ogni entità persistente.
- Testare migration up/down quando possibile.

### Accessibilità

- Aggiungere test axe su schermate core.
- Garantire focus ring visibile.
- Non usare solo colore per stato.
- Supportare contrast mode.

### Performance

- Tabelle WorkItem fino a 10.000 righe devono restare utilizzabili con virtualizzazione.
- Search locale deve rispondere sotto 300 ms su dataset medio.
- Gantt deve virtualizzare righe e timeline.

### Sicurezza

- SQL parametrico sempre.
- Validazione Pydantic su input backend.
- Sanitizzazione Markdown/HTML.
- CSP stretta in Electron.
- Disabilitare Node integration nella renderer window salvo eccezione documentata.
- IPC whitelistata.

## 12. Milestone di prodotto

| Milestone | Contenuto | Sprint |
|---|---|---|
| M0 Developer Preview | Shell, metamodel, workspace, project, WorkItem base | 0-4 |
| M1 Planning Alpha | RBAC, Gantt, boards, roadmap, calendar | 5-8 |
| M2 Governance Alpha | Time, costs, budget, grants, meetings | 9-12 |
| M3 Collaboration Alpha | Wiki, docs, notifications, search | 13-14 |
| M4 Integration Beta | Git, GitHub/GitLab, import/export, API | 15-16, 19 |
| M5 Executive Beta | Analytics, branch, reports, compliance hardening | 17-20 |
| M6 European Release Candidate | Packaging, accessibility, backup/restore, templates EU | 20-21 |

## 13. Errori da evitare

- Implementare board come entità separata dal WorkItem.
- Implementare budget senza collegamento a time/cost entries.
- Implementare grants come semplici note testuali.
- Implementare analytics per-persona invasive.
- Implementare integrazioni cloud come default.
- Implementare permission solo lato UI.
- Creare form hardcoded per ogni modulo ignorando il metamodel.
- Accumulare feature senza import/export.
- Rimandare accessibilità e privacy alla fine.

## 14. Regola finale per il team

Ogni sviluppatore deve poter rispondere a questa domanda prima di aprire una PR:

> Quale problema di governance di progetto sto risolvendo, quale dato sto creando, chi può vederlo, come viene esportato, come viene cancellato o redatto, e quale test prova che funziona?

Se la risposta non è chiara, la story non è pronta.
