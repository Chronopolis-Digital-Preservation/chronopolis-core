/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.replicate;

import org.chronopolis.amqp.RoutingKey;
import org.chronopolis.common.properties.GenericProperties;
import org.chronopolis.common.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author shake
 */
public class ReplicationProperties extends GenericProperties {
    private final Logger log = LoggerFactory.getLogger(ReplicationProperties.class);

    public static final String PROPERTIES_ACE_FQDN = "ace.fqdn";
    public static final String PROPERTIES_ACE_PATH = "ace.path";
    public static final String PROPERTIES_ACE_USER = "ace.user";
    public static final String PROPERTIES_ACE_PASS = "ace.pass";
    public static final String PROPERTIES_ACE_PORT = "ace.port";

    private final String directQueueName;
    private final String directQueueBinding;
    private final String broadcastQueueName;
    private final String broadcastQueueBinding;

    // Lots of things related to ACE
    private String aceFqdn;
    private String acePath;
    private String aceUser;
    private String acePass;
    private int acePort = 8080;

    public ReplicationProperties(String nodeName, 
                                 String stage,
                                 String exchange,
                                 String inboundKey,
                                 String broadcastKey,
                                 String aceFqdn,
                                 String acePath, 
                                 String aceUser, 
                                 String acePass, 
                                 Integer acePort) {
        super(nodeName, stage, exchange, inboundKey, broadcastKey);
        this.aceFqdn = aceFqdn;
        this.acePath = acePath;
        this.aceUser = aceUser;
        this.acePass = acePass;
        this.acePort = acePort;

        this.directQueueName = "replicate.direct." + nodeName;
        this.broadcastQueueName = "replicate.broadcast."+nodeName;
        this.directQueueBinding = "replicate-" + nodeName + "-inbound";
        this.broadcastQueueBinding = RoutingKey.REPLICATE_BROADCAST.asRoute();

        setInboundKey(directQueueBinding);
    }

    /**
     * @return the aceFqdn
     */
    public String getAceFqdn() {
        return aceFqdn;
    }

    /**
     * @param aceFqdn the aceFqdn to set
     */
    public void setAceFqdn(String aceFqdn) {
        this.aceFqdn = aceFqdn;
    }

    /**
     * @return the acePath
     */
    public String getAcePath() {
        return acePath;
    }

    /**
     * @param acePath the acePath to set
     */
    public void setAcePath(String acePath) {
        this.acePath = acePath;
    }

    /**
     * @return the acePort
     */
    public int getAcePort() {
        return acePort;
    }

    /**
     * @param acePort the acePort to set
     */
    public void setAcePort(int acePort) {
        this.acePort = acePort;
    }

    /**
     * @return the aceUser
     */
    public String getAceUser() {
        return aceUser;
    }

    /**
     * @param aceUser the aceUser to set
     */
    public void setAceUser(String aceUser) {
        this.aceUser = aceUser;
    }

    /**
     * @return the acePass
     */
    public String getAcePass() {
        return acePass;
    }

    /**
     * @param acePass the acePass to set
     */
    public void setAcePass(String acePass) {
        this.acePass = acePass;
    }

    public String getDirectQueueName() {
        return directQueueName;
    }

    public String getDirectQueueBinding() {
        return directQueueBinding;
    }

    public String getBroadcastQueueBinding() {
        return broadcastQueueBinding;
    }

    public String getBroadcastQueueName() {
        return broadcastQueueName;
    }

    public boolean validate() {
        boolean valid = true;
        StringBuilder sb = URIUtil.buildAceUri(getAceFqdn(), getAcePort(), getAcePath());
        try {
            URL url = new URL(sb.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != 200) {
                log.error("Could not connect to ACE instance, check your properties against your tomcat deployment");
                valid = false;
            }
        } catch (IOException e) {
            log.error("Could not create URL connection to " + getAceFqdn() + ". Ensure your tomcat server is running.");
            valid = false;
        }

        return valid;
    }
}
