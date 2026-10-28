package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.service.AssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * At startup, copies into the database the attachments that so far existed only in this
 * PC's assets folder, so collaborators on other PCs can open them too. Runs in the
 * background to keep startup fast; downloads backfill lazily in the meantime.
 */
@Component
@ConditionalOnProperty(name = "librepm.assets.backfill-on-startup", havingValue = "true", matchIfMissing = true)
public class AssetContentBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AssetContentBackfillRunner.class);

    private final AssetService assetService;

    public AssetContentBackfillRunner(AssetService assetService) {
        this.assetService = assetService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Thread worker = new Thread(() -> {
            try {
                int copied = assetService.backfillLocalContents();
                if (copied > 0) log.info("Shared {} local attachment(s) through the database", copied);
            } catch (RuntimeException error) {
                log.warn("Attachment backfill skipped: {}", error.getMessage());
            }
        }, "librepm-asset-backfill");
        worker.setDaemon(true);
        worker.start();
    }
}
