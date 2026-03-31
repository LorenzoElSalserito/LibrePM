package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.AssetResponse;
import com.lorenzodm.librepm.api.dto.response.TagResponse;
import com.lorenzodm.librepm.api.dto.response.TaskChecklistItemResponse;
import com.lorenzodm.librepm.api.dto.response.TaskResponse;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.TaskChecklistItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Task Mapper v0.5.0
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @version 0.5.0
 */
@Component
public class TaskMapper {

    private final TagMapper tagMapper;
    private final AssetMapper assetMapper;

    public TaskMapper(TagMapper tagMapper, AssetMapper assetMapper) {
        this.tagMapper = tagMapper;
        this.assetMapper = assetMapper;
    }

    public TaskResponse toResponse(Task t) {
        List<TagResponse> tags = t.getTags() != null
                ? t.getTags().stream().map(tagMapper::toResponseLight).collect(Collectors.toList())
                : List.of();

        List<AssetResponse> assets = t.getAssets() != null
                ? t.getAssets().stream().map(assetMapper::toResponse).collect(Collectors.toList())
                : List.of();

        List<TaskChecklistItemResponse> checklist = t.getChecklistItems() != null
                ? t.getChecklistItems().stream().map(this::toChecklistResponse).collect(Collectors.toList())
                : List.of();

        List<String> blockerIds = t.getBlockers() != null
                ? t.getBlockers().stream().map(Task::getId).collect(Collectors.toList())
                : List.of();

        List<String> childTaskIds = t.getChildTasks() != null
                ? t.getChildTasks().stream().map(Task::getId).collect(Collectors.toList())
                : List.of();

        return buildResponse(t, tags, assets, checklist, blockerIds, childTaskIds);
    }

    public TaskResponse toResponseLight(Task t) {
        return buildResponse(t, null, null, null, null, null);
    }

    private TaskResponse buildResponse(Task t, List<TagResponse> tags, List<AssetResponse> assets,
                                       List<TaskChecklistItemResponse> checklist, List<String> blockerIds,
                                       List<String> childTaskIds) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus() != null ? t.getStatus().getId() : null,
                t.getStatus() != null ? t.getStatus().getName() : null,
                t.getStatus() != null ? t.getStatus().getColor() : null,
                t.getPriority() != null ? t.getPriority().getId() : null,
                t.getPriority() != null ? t.getPriority().getName() : null,
                t.getPriority() != null ? t.getPriority().getColor() : null,
                t.getPriority() != null ? t.getPriority().getLevel() : 0,
                t.getDeadline(),
                t.getOwner(),
                t.getNotes(),
                t.getMarkdownNotes(),
                t.isArchived(),
                t.getSortOrder(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getLastSyncedAt(),
                t.getSyncStatus().name(),
                t.getProject() != null ? t.getProject().getId() : null,
                t.getAssignedTo() != null ? t.getAssignedTo().getId() : null,
                t.getReminderDate(),
                t.isReminderEnabled(),
                t.isNotificationSent(),
                t.getEstimatedEffort(),
                t.getActualEffort(),
                t.getAssetPath(),
                t.getAssetFileName(),
                t.getAssetMimeType(),
                t.getAssetSizeBytes(),
                tags,
                assets,
                checklist,
                t.getTotalFocusTimeMs(),
                t.isOverdue(),
                t.getType().name(),
                t.getPlannedStart(),
                t.getPlannedFinish(),
                t.isBlocked(),
                blockerIds,
                t.getParentTask() != null ? t.getParentTask().getId() : null,
                childTaskIds,
                t.isInbox(),
                t.getPhaseId(),
                t.getDeliverableId(),
                t.getStakeholderId(),
                t.getFundingContext(),
                t.isPlanningEnabled(),
                t.isFinanceEnabled(),
                t.getActualStart() != null ? t.getActualStart().toString() : null,
                t.getActualEnd() != null ? t.getActualEnd().toString() : null,
                t.getEstimatedEffortHours(),
                t.getActualEffortHours()
        );
    }

    private TaskChecklistItemResponse toChecklistResponse(TaskChecklistItem item) {
        return new TaskChecklistItemResponse(
                item.getId(),
                item.getText(),
                item.isDone(),
                item.getSortOrder(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
