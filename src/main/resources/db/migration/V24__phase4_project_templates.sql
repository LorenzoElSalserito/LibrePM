-- ============================================
-- V24: Phase 4 - Project Templates System
-- PRD-16: Template Gallery, Blueprints, Instantiation
-- ============================================

CREATE TABLE project_templates (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    category TEXT,
    use_cases TEXT,
    prerequisites TEXT,
    version TEXT NOT NULL DEFAULT '1.0',
    requires_planning_engine INTEGER NOT NULL DEFAULT 0,
    template_scope TEXT NOT NULL DEFAULT 'USER',
    structure_json TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

CREATE INDEX idx_template_scope ON project_templates(template_scope);
CREATE INDEX idx_template_category ON project_templates(category);

-- ============================================
-- Seed: 4 System Templates (PRD-16-FR-002)
-- ============================================

-- 1. Project Timeline (PRD-16-FR-007)
-- Focus: fasi principali, milestones, timeline ad alto livello
INSERT INTO project_templates (
    id, name, description, category, use_cases, prerequisites,
    version, requires_planning_engine, template_scope, structure_json,
    created_at, updated_at, sync_status
) VALUES (
    'tpl-system-timeline',
    'Project Timeline',
    'Template per progetti strutturati in fasi con milestone chiare e timeline di alto livello.',
    'PROJECT_MANAGEMENT',
    'Ideale per PM che devono comunicare lo stato del progetto a stakeholder. Perfetto per progetti con fasi ben definite e milestone misurabili.',
    'Nessun prerequisito speciale.',
    '1.0',
    0,
    'SYSTEM',
    '{
      "phases": [
        {
          "name": "Avvio",
          "tasks": [
            {"title": "Kick-off meeting", "taskType": "MEETING", "priorityKey": "HIGH", "estimatedEffort": 60},
            {"title": "Raccolta requisiti", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480},
            {"title": "Approvazione scope", "taskType": "MILESTONE", "priorityKey": "CRITICAL", "estimatedEffort": 0}
          ]
        },
        {
          "name": "Pianificazione",
          "tasks": [
            {"title": "Definizione WBS", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 240},
            {"title": "Stima effort e timeline", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 240},
            {"title": "Piano approvato", "taskType": "MILESTONE", "priorityKey": "CRITICAL", "estimatedEffort": 0}
          ]
        },
        {
          "name": "Esecuzione",
          "tasks": [
            {"title": "Sviluppo / Realizzazione", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 2400},
            {"title": "Review intermedia", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 120},
            {"title": "Consegna intermedia", "taskType": "MILESTONE", "priorityKey": "HIGH", "estimatedEffort": 0}
          ]
        },
        {
          "name": "Chiusura",
          "tasks": [
            {"title": "Test e validazione", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480},
            {"title": "Documentazione finale", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 240},
            {"title": "Consegna finale", "taskType": "MILESTONE", "priorityKey": "CRITICAL", "estimatedEffort": 0}
          ]
        }
      ],
      "deliverables": [
        {"name": "Project Charter", "description": "Documento di avvio del progetto approvato dagli stakeholder"},
        {"name": "Piano di progetto", "description": "WBS, timeline e piano delle risorse"},
        {"name": "Report finale", "description": "Report di chiusura con lessons learned"}
      ],
      "okrs": [
        {
          "objective": "Consegnare il progetto nei tempi e nei costi pianificati",
          "keyResults": [
            {"name": "Varianza di schedule", "targetValue": 0, "unit": "giorni"},
            {"name": "Soddisfazione stakeholder", "targetValue": 4.0, "unit": "/5"}
          ]
        }
      ],
      "charter": {
        "objectives": "Descrivere gli obiettivi principali del progetto.",
        "problemStatement": "Descrivere il problema o l opportunita che questo progetto risolve.",
        "businessCase": "Descrivere il valore di business atteso dal progetto."
      },
      "viewPresets": ["BOARD", "TIMELINE"],
      "tags": ["timeline", "pianificazione"]
    }',
    datetime('now'), datetime('now'), 'LOCAL_ONLY'
);

-- 2. Project Tracking (PRD-16-FR-008)
-- Focus: tracking deliverable, KPI, budget/ore, dashboard
INSERT INTO project_templates (
    id, name, description, category, use_cases, prerequisites,
    version, requires_planning_engine, template_scope, structure_json,
    created_at, updated_at, sync_status
) VALUES (
    'tpl-system-tracking',
    'Project Tracking',
    'Template per il monitoraggio continuo di deliverable, KPI, ore e budget su progetti in corso.',
    'PROJECT_MANAGEMENT',
    'Ideale per project controller e PM che devono monitorare avanzamento e scostamenti. Include dashboard esecutiva preconfigurata.',
    'Nessun prerequisito speciale.',
    '1.0',
    0,
    'SYSTEM',
    '{
      "phases": [
        {
          "name": "Setup tracking",
          "tasks": [
            {"title": "Definire KPI da monitorare", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 120},
            {"title": "Configurare dashboard", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 60},
            {"title": "Baseline iniziale", "taskType": "MILESTONE", "priorityKey": "HIGH", "estimatedEffort": 0}
          ]
        },
        {
          "name": "Monitoraggio settimanale",
          "tasks": [
            {"title": "Aggiornamento stato deliverable", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 60},
            {"title": "Review metriche KPI", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 60},
            {"title": "Status report stakeholder", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 60}
          ]
        },
        {
          "name": "Revisione mensile",
          "tasks": [
            {"title": "Analisi scostamenti", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 120},
            {"title": "Aggiornamento forecast", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 120},
            {"title": "Review rischi", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 60}
          ]
        }
      ],
      "deliverables": [
        {"name": "Status report mensile", "description": "Report mensile sullo stato del progetto"},
        {"name": "Dashboard KPI", "description": "Cruscotto con le metriche chiave del progetto"},
        {"name": "Risk register aggiornato", "description": "Registro rischi con mitigazioni aggiornate"}
      ],
      "okrs": [
        {
          "objective": "Mantenere il progetto on-track rispetto al piano",
          "keyResults": [
            {"name": "Deliverable completati nei tempi", "targetValue": 100, "unit": "%"},
            {"name": "Ore a consuntivo vs pianificate", "targetValue": 100, "unit": "%"},
            {"name": "Rischi critici aperti", "targetValue": 0, "unit": "count"}
          ]
        }
      ],
      "charter": {
        "objectives": "Garantire il tracking preciso e continuo dell avanzamento del progetto.",
        "problemStatement": "La mancanza di visibilita sullo stato reale del progetto causa ritardi e sorprese.",
        "businessCase": "Un tracking efficace riduce i rischi di sforamento e migliora la predictability."
      },
      "viewPresets": ["BOARD", "TIMELINE"],
      "tags": ["tracking", "kpi", "monitoring"]
    }',
    datetime('now'), datetime('now'), 'LOCAL_ONLY'
);

-- 3. Gantt Project (PRD-16-FR-009)
-- Focus: planning avanzato, dipendenze, Gantt, CPM
INSERT INTO project_templates (
    id, name, description, category, use_cases, prerequisites,
    version, requires_planning_engine, template_scope, structure_json,
    created_at, updated_at, sync_status
) VALUES (
    'tpl-system-gantt',
    'Gantt Project',
    'Template per progetti complessi che richiedono pianificazione avanzata con dipendenze e vista Gantt.',
    'GANTT',
    'Ideale per PM di progetti con dipendenze complesse tra task, risorse multiple e vincoli di scheduling. Attiva il motore di pianificazione CPM.',
    'Richiede il planning engine (PRD-08). Abilitare la vista Gantt nel progetto.',
    '1.0',
    1,
    'SYSTEM',
    '{
      "phases": [
        {
          "name": "Analisi e Design",
          "tasks": [
            {"title": "Analisi dei requisiti", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 960},
            {"title": "Architettura della soluzione", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480},
            {"title": "Design approvato", "taskType": "MILESTONE", "priorityKey": "CRITICAL", "estimatedEffort": 0}
          ]
        },
        {
          "name": "Sviluppo - Sprint 1",
          "tasks": [
            {"title": "Setup ambiente", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 240},
            {"title": "Sviluppo modulo A", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 1200},
            {"title": "Unit test modulo A", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480}
          ]
        },
        {
          "name": "Sviluppo - Sprint 2",
          "tasks": [
            {"title": "Sviluppo modulo B", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 1200},
            {"title": "Unit test modulo B", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480},
            {"title": "Integration test", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480}
          ]
        },
        {
          "name": "Test e Deploy",
          "tasks": [
            {"title": "User acceptance test", "taskType": "TASK", "priorityKey": "CRITICAL", "estimatedEffort": 480},
            {"title": "Fix bugs critici", "taskType": "TASK", "priorityKey": "CRITICAL", "estimatedEffort": 240},
            {"title": "Deploy in produzione", "taskType": "MILESTONE", "priorityKey": "CRITICAL", "estimatedEffort": 0}
          ]
        }
      ],
      "deliverables": [
        {"name": "Documento di analisi", "description": "Specifiche funzionali e tecniche approvate"},
        {"name": "Modulo A", "description": "Primo modulo sviluppato e testato"},
        {"name": "Modulo B", "description": "Secondo modulo sviluppato e testato"},
        {"name": "Sistema integrato", "description": "Prodotto finale testato e deployato"}
      ],
      "okrs": [
        {
          "objective": "Consegnare il progetto con qualita e nei tempi",
          "keyResults": [
            {"name": "Test coverage", "targetValue": 80, "unit": "%"},
            {"name": "Bug critici in produzione", "targetValue": 0, "unit": "count"},
            {"name": "Varianza di schedule", "targetValue": 0, "unit": "giorni"}
          ]
        }
      ],
      "charter": {
        "objectives": "Sviluppare e rilasciare il sistema nei tempi e con la qualita attesa.",
        "problemStatement": "Il progetto richiede coordinamento preciso di task interdipendenti e risorse multiple.",
        "businessCase": "La pianificazione con Gantt e CPM ottimizza l utilizzo delle risorse e riduce i ritardi."
      },
      "viewPresets": ["GANTT", "BOARD"],
      "tags": ["gantt", "planning", "sviluppo"]
    }',
    datetime('now'), datetime('now'), 'LOCAL_ONLY'
);

-- 4. Event Marketing Timeline (PRD-16-FR-010)
-- Focus: fasi e task tipici di marketing/eventi
INSERT INTO project_templates (
    id, name, description, category, use_cases, prerequisites,
    version, requires_planning_engine, template_scope, structure_json,
    created_at, updated_at, sync_status
) VALUES (
    'tpl-system-event-marketing',
    'Event Marketing Timeline',
    'Template per la pianificazione e gestione di eventi di marketing: lanci, conferenze, campagne.',
    'MARKETING',
    'Ideale per marketing manager, event planner e team creativi. Include fasi tipiche di un evento: strategia, produzione, promozione, esecuzione e post-evento.',
    'Nessun prerequisito speciale.',
    '1.0',
    0,
    'SYSTEM',
    '{
      "phases": [
        {
          "name": "Strategia e Pianificazione",
          "tasks": [
            {"title": "Definire obiettivi evento", "taskType": "TASK", "priorityKey": "CRITICAL", "estimatedEffort": 120},
            {"title": "Budget e approvazione", "taskType": "TASK", "priorityKey": "CRITICAL", "estimatedEffort": 120},
            {"title": "Scelta sede e data", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 240},
            {"title": "Kick-off team", "taskType": "MEETING", "priorityKey": "HIGH", "estimatedEffort": 60}
          ]
        },
        {
          "name": "Produzione Contenuti",
          "tasks": [
            {"title": "Concept creativo", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480},
            {"title": "Materiali grafici", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 960},
            {"title": "Copywriting e testi", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480},
            {"title": "Video promo", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 960}
          ]
        },
        {
          "name": "Promozione",
          "tasks": [
            {"title": "Campagna social media", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 480},
            {"title": "Email marketing", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 240},
            {"title": "PR e media outreach", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 480},
            {"title": "Gestione registrazioni", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 240}
          ]
        },
        {
          "name": "Esecuzione Evento",
          "tasks": [
            {"title": "Setup venue", "taskType": "TASK", "priorityKey": "CRITICAL", "estimatedEffort": 480},
            {"title": "Briefing staff", "taskType": "MEETING", "priorityKey": "HIGH", "estimatedEffort": 60},
            {"title": "Evento live", "taskType": "MILESTONE", "priorityKey": "CRITICAL", "estimatedEffort": 0},
            {"title": "Live social coverage", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 240}
          ]
        },
        {
          "name": "Post-Evento",
          "tasks": [
            {"title": "Analisi risultati e metriche", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 240},
            {"title": "Report ROI", "taskType": "TASK", "priorityKey": "HIGH", "estimatedEffort": 120},
            {"title": "Follow-up con partecipanti", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 120},
            {"title": "Lessons learned", "taskType": "TASK", "priorityKey": "MEDIUM", "estimatedEffort": 60}
          ]
        }
      ],
      "deliverables": [
        {"name": "Piano evento approvato", "description": "Documento di pianificazione completo con budget"},
        {"name": "Kit materiali grafici", "description": "Tutti i materiali visivi per l evento"},
        {"name": "Report post-evento", "description": "Analisi risultati, ROI e lessons learned"}
      ],
      "okrs": [
        {
          "objective": "Organizzare un evento di successo che raggiunga gli obiettivi di marketing",
          "keyResults": [
            {"name": "Partecipanti", "targetValue": 200, "unit": "persone"},
            {"name": "Copertura media", "targetValue": 10, "unit": "articoli"},
            {"name": "Lead generati", "targetValue": 50, "unit": "lead"},
            {"name": "Net Promoter Score", "targetValue": 8.0, "unit": "/10"}
          ]
        }
      ],
      "charter": {
        "objectives": "Organizzare un evento memorabile che aumenti la brand awareness e generi lead qualificati.",
        "problemStatement": "La mancanza di una pianificazione strutturata causa ritardi, costi extra e scarsa qualita degli eventi.",
        "businessCase": "Un evento ben pianificato genera ROI misurabile in termini di lead, visibilita e relazioni."
      },
      "viewPresets": ["BOARD", "TIMELINE"],
      "tags": ["marketing", "evento", "campagna"]
    }',
    datetime('now'), datetime('now'), 'LOCAL_ONLY'
);

-- ============================================
-- End V24
-- ============================================
