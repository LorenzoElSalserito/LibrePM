package com.lorenzodm.librepm.service;

import java.util.Set;

/**
 * Parses @username mentions from text and creates notifications.
 *
 * @author Lorenzo DM
 * @since 0.9.1
 */
public interface MentionService {

    /**
     * Extracts @username mentions from text.
     */
    Set<String> extractMentions(String text);

    /**
     * Processes mentions in text: extracts usernames, resolves to users,
     * and creates notifications for each mentioned user.
     *
     * @param text        the text containing @mentions
     * @param sender      the user who wrote the text
     * @param projectId   the project context
     * @param refType     reference type (e.g., "TASK", "NOTE")
     * @param refId       reference entity ID
     */
    void processMentions(String text, String senderId, String projectId, String refType, String refId);
}
