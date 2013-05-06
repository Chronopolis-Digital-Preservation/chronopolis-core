/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.messaging.file;

import org.chronopolis.messaging.MessageType;
import org.chronopolis.messaging.base.ChronBody;
import org.chronopolis.messaging.base.ChronHeader;
import org.chronopolis.messaging.base.ChronMessage2;

/**
 * Used by the Distribution Service to ask other nodes the status of 
 * a particular file[s]
 *
 * @author shake
 */
public class FileQueryMessage extends ChronMessage2 {
    private final MessageType type = MessageType.FILE_QUERY;
    private final String DEPOSITOR_KEY = "depositor";
    private final String PROTOCOL_KEY = "protocol";
    private final String LOCATION_KEY = "location";
    private final String FILENAME_KEY = "filename";

    public FileQueryMessage() {
        this.body = new ChronBody(type);
        this.header = new ChronHeader();
    }

    @Override
    public void processMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
