import { useTranslation } from 'react-i18next';

export default function TaskToolbar({
                                        search,
                                        setSearch,
                                        statusFilter,
                                        setStatusFilter,
                                        priorityFilter,
                                        setPriorityFilter,
                                        sortBy,
                                        setSortBy,
                                    }) {
    const { t } = useTranslation();
    return (
        <div className="d-flex gap-2 align-items-center flex-wrap">
            <input
                className="form-control bg-dark text-light border-secondary"
                style={{ maxWidth: 320 }}
                placeholder={t("Search tasks...")}
                value={search}
                onChange={(e) => setSearch(e.target.value)}
            />

            <select
                className="form-select bg-dark text-light border-secondary"
                style={{ maxWidth: 160 }}
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
            >
                <option value="ALL">{t("Status: All")}</option>
                <option value="TODO">TODO</option>
                <option value="DOING">DOING</option>
                <option value="DONE">DONE</option>
            </select>

            <select
                className="form-select bg-dark text-light border-secondary"
                style={{ maxWidth: 170 }}
                value={priorityFilter}
                onChange={(e) => setPriorityFilter(e.target.value)}
            >
                <option value="ALL">{t("Priority: All")}</option>
                <option value="LOW">LOW</option>
                <option value="MED">MED</option>
                <option value="HIGH">HIGH</option>
            </select>

            <select
                className="form-select bg-dark text-light border-secondary"
                style={{ maxWidth: 190 }}
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
            >
                <option value="UPDATED_DESC">{t("Sort: Last updated")}</option>
                <option value="CREATED_DESC">{t("Sort: Newest")}</option>
                <option value="PRIORITY_DESC">{t("Sort: High priority")}</option>
                <option value="TITLE_ASC">{t("Sort: Title A-Z")}</option>
            </select>

            <button
                className="btn btn-outline-secondary"
                onClick={() => {
                    setSearch("");
                    setStatusFilter("ALL");
                    setPriorityFilter("ALL");
                    setSortBy("UPDATED_DESC");
                }}
            >
                {t("Reset")}
            </button>
        </div>
    );
}
