/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.common.ace;

import edu.umiacs.ace.ims.api.IMSService;
import edu.umiacs.ace.ims.api.TokenRequestBatch;
import edu.umiacs.ace.ims.ws.TokenRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Ace Tokens for a bag
 *
 * @author shake
 */
public class BagTokenizer {
    private final Logger log = LoggerFactory.getLogger(BagTokenizer.class);
    private final ExecutorService manifestRunner = Executors.newCachedThreadPool();
    private final Path bag;
    private TokenWriterCallback callback = null;
    private TokenRequestBatch batch = null;


    public BagTokenizer(Path bag) {
        this.bag = bag;
        callback = new TokenWriterCallback(this.bag.getFileName().toString());
    }
    // Maybe we should push this into something else as well
    // Since it doesn't have to do much with validation, only runs after

    public Path getAceManifest(Path stage) throws InterruptedException,
            IOException,
            ExecutionException {
        // temp while I figure out what to do
        HashMap<Path, String> validDigests = new HashMap<>();
        /*
        if (!validManifest.isDone()) {
            throw new RuntimeException("Not finished validating manifest for bag");
        }
        */

        if (stage == null) {
            throw new RuntimeException("Stage cannot be null");
        }

        createIMSConnection();
        callback.setStage(stage);
        Future<Path> manifestPath = manifestRunner.submit(callback);

        log.info("Have {} entries", validDigests.entrySet().size());
        for (Map.Entry<Path, String> entry : validDigests.entrySet()) {
            TokenRequest req = new TokenRequest();
            // We want the relative path for ACE so let's get it
            Path full = entry.getKey();
            Path relative = full.subpath(bag.getNameCount(), full.getNameCount());

            req.setName(relative.toString());
            req.setHashValue(entry.getValue());
            batch.add(req);
        }

        return manifestPath.get();
    }

    private void createIMSConnection() {
        IMSService ims;
        // TODO: Unhardcode
        ims = IMSService.connect("ims.umiacs.umd.edu", 443, true);
        batch = ims.createImmediateTokenRequestBatch("SHA-256",
                callback,
                1000,
                5000);
    } 
}
