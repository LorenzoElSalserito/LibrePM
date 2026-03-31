import { useTranslation } from 'react-i18next';

export default function StatusPill({ value, onChange, onKeyDown, selectRef, disabled = false }) {
    const { t } = useTranslation();

    const STATUS_META = {
        COMPLETED: { label: t("Completed"), dot: "#3bb54a" },
        IN_PROGRESS: { label: t("In progress"), dot: "#f0b429" },
        UNDER_REVIEW: { label: t("Under review"), dot: "#3b82f6" },
        NOT_STARTED: { label: t("Not started"), dot: "#9ca3af" },
    };

    const meta = STATUS_META[value] ?? STATUS_META.NOT_STARTED;

    return (
        <span className="jl-pill">
      <span className="jl-dot" style={{ background: meta.dot }} />
      <select
          ref={selectRef}
          className="form-select form-select-sm"
          style={{ border: 0, background: "transparent", fontWeight: 900 }}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onKeyDown={onKeyDown}
          disabled={disabled}
      >
        {Object.entries(STATUS_META).map(([k, v]) => (
            <option key={k} value={k}>
                {v.label}
            </option>
        ))}
      </select>
    </span>
    );
}
