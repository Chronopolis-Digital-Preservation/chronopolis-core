/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import org.chronopolis.amqp.ChronMessageListener;
import org.chronopolis.messaging.MessageType;
import org.chronopolis.messaging.base.ChronProcessor;

/**
 *
 * @author shake 
 */
public class IngestMessageListener extends ChronMessageListener {
	private ChronProcessor packageIngestStatusQueryProcessor;
	private ChronProcessor packageReadyProcessor;
	private ChronProcessor collectionInitCompleteProcessor;

	public IngestMessageListener(ChronProcessor packageIngestStatusQueryProcessor,
								 ChronProcessor packageReadyProcessor,
                                 ChronProcessor collectionInitCompleteProcessor) {
		this.packageIngestStatusQueryProcessor = packageIngestStatusQueryProcessor;
		this.packageReadyProcessor = packageReadyProcessor;
        this.collectionInitCompleteProcessor = collectionInitCompleteProcessor;
	}

	@Override
	public ChronProcessor getProcessor(MessageType type) {
        switch (type) {
            case PACKAGE_INGEST_READY:
                return packageReadyProcessor;
            case PACKAGE_INGEST_STATUS_QUERY:
                return packageIngestStatusQueryProcessor;
            case COLLECTION_INIT_COMPLETE:
                return collectionInitCompleteProcessor;
            default:
                throw new RuntimeException("Unexpected MessageType: " + type.name());
        }
	}
	
}