package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.*;
import com.lorenzodm.librepm.repository.*;
import com.lorenzodm.librepm.service.ReportGenerationService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private final ProjectRepository projectRepo;
    private final TaskRepository taskRepo;
    private final DeliverableRepository deliverableRepo;
    private final RiskRegisterEntryRepository riskRepo;
    private final ProjectCharterRepository charterRepo;

    public ReportGenerationServiceImpl(ProjectRepository projectRepo,
                                       TaskRepository taskRepo,
                                       DeliverableRepository deliverableRepo,
                                       RiskRegisterEntryRepository riskRepo,
                                       ProjectCharterRepository charterRepo) {
        this.projectRepo = projectRepo;
        this.taskRepo = taskRepo;
        this.deliverableRepo = deliverableRepo;
        this.riskRepo = riskRepo;
        this.charterRepo = charterRepo;
    }

    @Override
    public byte[] generateReport(String projectId, ReportType type) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        String html = switch (type) {
            case EXECUTIVE_SUMMARY -> buildExecutiveSummaryHtml(project, projectId);
            case CHARTER -> buildCharterHtml(project, projectId);
            case DELIVERABLE_STATUS -> buildDeliverableStatusHtml(project, projectId);
            case RISK_MATRIX -> buildRiskMatrixHtml(project, projectId);
        };

        return renderPdf(html);
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private String buildExecutiveSummaryHtml(Project project, String projectId) {
        List<Task> tasks = taskRepo.findByProjectId(projectId);
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() != null && "Done".equalsIgnoreCase(t.getStatus().getName())).count();
        double pct = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;

        List<Deliverable> deliverables = deliverableRepo.findByProjectId(projectId);
        List<RiskRegisterEntry> risks = riskRepo.findByProjectId(projectId);

        return wrapHtml("Executive Summary — " + esc(project.getName()),
            "<h1>" + esc(project.getName()) + "</h1>" +
            "<p><strong>Date:</strong> " + LocalDate.now() + "</p>" +
            "<h2>Task Progress</h2>" +
            "<p>Total: " + totalTasks + " | Completed: " + completedTasks + " (" + String.format("%.1f", pct) + "%)</p>" +
            "<h2>Deliverables</h2>" +
            "<p>Total: " + deliverables.size() + "</p>" +
            "<h2>Risks</h2>" +
            "<p>Total: " + risks.size() + "</p>"
        );
    }

    private String buildCharterHtml(Project project, String projectId) {
        var charter = charterRepo.findByProjectId(projectId).orElse(null);
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Project Charter — ").append(esc(project.getName())).append("</h1>");
        if (charter != null) {
            if (charter.getObjectives() != null) sb.append("<h2>Objectives</h2><p>").append(esc(charter.getObjectives())).append("</p>");
            if (charter.getProblemStatement() != null) sb.append("<h2>Problem Statement</h2><p>").append(esc(charter.getProblemStatement())).append("</p>");
            if (charter.getBusinessCase() != null) sb.append("<h2>Business Case</h2><p>").append(esc(charter.getBusinessCase())).append("</p>");
            if (charter.getSponsor() != null) sb.append("<h2>Sponsor</h2><p>").append(esc(charter.getSponsor())).append("</p>");
            if (charter.getProjectManager() != null) sb.append("<h2>Project Manager</h2><p>").append(esc(charter.getProjectManager())).append("</p>");
        } else {
            sb.append("<p><em>No charter defined.</em></p>");
        }
        return wrapHtml("Charter — " + esc(project.getName()), sb.toString());
    }

    private String buildDeliverableStatusHtml(Project project, String projectId) {
        List<Deliverable> deliverables = deliverableRepo.findByProjectId(projectId);
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Deliverable Status — ").append(esc(project.getName())).append("</h1>");
        sb.append("<table><thead><tr><th>Name</th><th>Risk Status</th></tr></thead><tbody>");
        for (Deliverable d : deliverables) {
            sb.append("<tr><td>").append(esc(d.getName())).append("</td><td>")
              .append(d.getRiskStatus() != null ? d.getRiskStatus().name() : "-").append("</td></tr>");
        }
        sb.append("</tbody></table>");
        return wrapHtml("Deliverables — " + esc(project.getName()), sb.toString());
    }

    private String buildRiskMatrixHtml(Project project, String projectId) {
        List<RiskRegisterEntry> risks = riskRepo.findByProjectId(projectId);
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Risk Matrix — ").append(esc(project.getName())).append("</h1>");
        sb.append("<table><thead><tr><th>Description</th><th>Impact</th><th>Probability</th><th>Mitigation</th></tr></thead><tbody>");
        for (RiskRegisterEntry r : risks) {
            sb.append("<tr>")
              .append("<td>").append(esc(r.getDescription())).append("</td>")
              .append("<td>").append(r.getImpact() != null ? r.getImpact().name() : "-").append("</td>")
              .append("<td>").append(r.getProbability() != null ? r.getProbability().name() : "-").append("</td>")
              .append("<td>").append(r.getMitigationStrategy() != null ? esc(r.getMitigationStrategy()) : "-").append("</td>")
              .append("</tr>");
        }
        sb.append("</tbody></table>");
        return wrapHtml("Risk Matrix — " + esc(project.getName()), sb.toString());
    }

    private String wrapHtml(String title, String body) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8"/>
                <title>%s</title>
                <style>
                    body { font-family: sans-serif; margin: 40px; font-size: 12px; color: #333; }
                    h1 { color: #1a5276; border-bottom: 2px solid #1a5276; padding-bottom: 8px; }
                    h2 { color: #2e86c1; margin-top: 24px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f2; font-weight: bold; }
                    tr:nth-child(even) { background-color: #fafafa; }
                    p { line-height: 1.6; }
                </style>
            </head>
            <body>%s</body>
            </html>
            """.formatted(title, body);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
