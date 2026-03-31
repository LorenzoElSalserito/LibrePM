import { useTranslation } from 'react-i18next';

export default function PriorityPill({ value, onChange, onKeyDown, selectRef, disabled = false }) {
    const { t } = useTranslation();

    const PRIORITY_META = {
        HIGH: { label: t("High"), dot: "#22c55e" },
        MEDIUM: { label: t("Medium"), dot: "#f97316" },
        LOW: { label: t("Low"), dot: "#ef4444" },
        NOT_SET: { label: t("Not set"), dot: "#9ca3af" },
    };

    const meta = PRIORITY_META[value] ?? PRIORITY_META.NOT_SET;

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
        {Object.entries(PRIORITY_META).map(([k, v]) => (
            <option key={k} value={k}>
                {v.label}
            </option>
        ))}
      </select>
    </span>
    );
}
