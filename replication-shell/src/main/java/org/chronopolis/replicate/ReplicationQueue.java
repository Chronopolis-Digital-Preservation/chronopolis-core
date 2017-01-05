/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.replicate;

import org.chronopolis.common.exception.FileTransferException;
import org.chronopolis.common.transfer.FileTransfer;
import org.chronopolis.common.transfer.HttpsTransfer;
import org.chronopolis.common.transfer.RSyncTransfer;
import org.chronopolis.replicate.config.ReplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class which will fire off downloads
 * Do we want the queue to be static so that all threads can poll from it?
 *
 * @author shake
 */
public class ReplicationQueue implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ReplicationQueue.class);
    private static final String SLASH =  "/"; // Not the guitarist
    private static LinkedBlockingQueue<ReplicationDownload> downloadQueue = new LinkedBlockingQueue<>();
    private static HttpsTransfer xfer = new HttpsTransfer();
    private static ReplicationSettings settings;

    public static void setSettings(ReplicationSettings settings) {
        ReplicationQueue.settings = settings;
    }

    private static String buildURL(String base, 
                                   String collection, 
                                   String group, 
                                   String file) {
        StringBuilder uriBuilder = new StringBuilder(base);

        if (group != null && !group.isEmpty()) {
            uriBuilder.append(group).append(SLASH);
        }
        if (collection != null && !collection.isEmpty()) {
            uriBuilder.append(collection).append(SLASH);
        }
        if (file != null && !file.isEmpty()) {
            uriBuilder.append(file);
        }

        return uriBuilder.toString();
    }
    
    public static Path getFileImmediate(String url,
                                        Path stage,
                                        String protocol) throws IOException,
                                                                FileTransferException {
        // Check our preconditions
        if (url == null) {
            System.out.println("Null url");
            throw new IllegalArgumentException("Url cannot be null");
        } else if (stage == null) {
            System.out.println("Null stage");
            throw new IllegalArgumentException("Stage cannot be null");
        } /* else if (xfer == null) {
            System.out.println("This shouldn't happen");
            throw new IllegalArgumentException("Transfer object is null, aborting");
        }*/


        FileTransfer transfer;

        if (protocol.equalsIgnoreCase("rsync")) {
            transfer = new RSyncTransfer(url);
        } else if (protocol.equalsIgnoreCase("https")) {
            transfer = new HttpsTransfer();
        } else {
            log.error("Invalid transfer type {}", protocol);
            // probably throw exception here
            return null;
        }

        return transfer.getFile(url, stage);
    }

    public static void getFileAsync(String base, 
                                    String collection, 
                                    String group, 
                                    String file,
                                    String protocol) {

        ReplicationDownload rFile = new ReplicationDownload(base, collection, group, file, protocol);
        try {
            log.info("Queueing {}", file);
            downloadQueue.put(rFile);
        } catch (InterruptedException ex) {
            log.error("Thread interrupted " + ex);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Path file = null;
            ReplicationDownload dl = downloadQueue.poll();
            try {
                Path collPath = Paths.get(settings.getPreservation(), dl.getGroup(), dl.getCollection());
                String url = buildURL(dl.getBase(), dl.getCollection(), dl.getGroup(), dl.getFile());
                file = xfer.getFile(url, collPath);
            } catch (FileTransferException e) {
                log.error("{}", e);
            } finally {
                // Requeue if necessary
                if ( file == null ) {
                    log.info("Requeuing " + dl.getFile());
                    getFileAsync(dl.getBase(), dl.getCollection(), dl.getGroup(), dl.getFile(), dl.getProtocol());
                }
            }
        }
    }

    // Small class to encapsulate the variables needed for a download
    private static class ReplicationDownload {
        private String collection;
        private String group;
        private String file;
        private String base;
        private String protocol;

        private ReplicationDownload(String base, 
                                    String collection, 
                                    String group, 
                                    String file,
                                    String protocol) {
            this.collection = collection;
            this.group = group;
            this.file = file;
            this.base = base;
            this.protocol = protocol;
        }

        public String getCollection() {
            return collection;
        }

        public String getGroup() {
            return group;
        }

        public String getFile() {
            return file;
        }

        public String getBase() {
            return base;
        }

        public String getProtocol() {
            return protocol;
        }
    }
} 