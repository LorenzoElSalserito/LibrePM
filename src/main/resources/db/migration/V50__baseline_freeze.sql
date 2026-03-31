-- V50: Baseline immutability and extended snapshots
ALTER TABLE baselines ADD COLUMN is_frozen BOOLEAN DEFAULT TRUE;
ALTER TABLE baselines ADD COLUMN budget_snapshot TEXT;
ALTER TABLE baselines ADD COLUMN deliverable_snapshot TEXT;
