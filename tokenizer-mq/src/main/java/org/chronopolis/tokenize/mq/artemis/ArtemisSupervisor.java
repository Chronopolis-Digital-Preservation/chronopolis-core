package org.chronopolis.tokenize.mq.artemis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umiacs.ace.ims.ws.TokenResponse;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.tokenize.ManifestEntry;
import org.chronopolis.tokenize.mq.RegisterMessage;
import org.chronopolis.tokenize.supervisor.TokenWorkSupervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * TokenWorkSupervisor which connects to an Artemis Broker to control flow for operations
 *
 * @author shake
 */
@SuppressWarnings("WeakerAccess")
public class ArtemisSupervisor implements TokenWorkSupervisor, Closeable {
    private final Logger log = LoggerFactory.getLogger(ArtemisSupervisor.class);

    public static final String REQUEST_TOPIC = "request";
    public static final String REGISTER_TOPIC = "register";

    private final ObjectMapper mapper = new ObjectMapper();
    private ClientSession clientSession;
    private ClientProducer requestProducer;
    private ClientProducer registerProducer;

    public ArtemisSupervisor(ClientSessionFactory sessionFactory) throws ActiveMQException {
        this.clientSession = sessionFactory.createSession();
        this.requestProducer = clientSession.createProducer(REQUEST_TOPIC);
        this.registerProducer = clientSession.createProducer(REGISTER_TOPIC);
    }

    /**
     * Close ALL resources used by the Supervisor. This must be called when removing a Supervisor
     * in order to clean up any lingering MQ connections.
     */
    @Override
    public void close() {
        if (requestProducer != null) {
            try {
                requestProducer.close();
            } catch (ActiveMQException ignored) {
            }
        }

        if (registerProducer != null) {
            try {
                registerProducer.close();
            } catch (ActiveMQException ignored) {
            }
        }

        if (clientSession != null) {
            try {
                clientSession.close();
            } catch (ActiveMQException ignored) {
            }
        }
    }

    @Override
    public boolean start(ManifestEntry entry) {
        log.info("[{}] Starting", entry.tokenName());
        ClientMessage clMessage = clientSession.createMessage(true);
        // It would be nice to have duplicate detection here but for now we'll leave it out as
        // the ids are still associated until a limit (default 2k) is reached
        clMessage.putStringProperty("id", entry.tokenName());
        clMessage.putLongProperty("bagId", entry.getBag().getId());
        try {
            clMessage.writeBodyBufferString(mapper.writeValueAsString(entry));
            requestProducer.send(clMessage);
            clientSession.commit();
        } catch (ActiveMQException | JsonProcessingException e) {
            log.error("Error sending token request message", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean retryTokenize(ManifestEntry entry) {
        // could do the rollback here but it doesn't make sense imo
        return false;
    }

    @Override
    public boolean retryRegister(ManifestEntry entry) {
        return false;
    }

    @Override
    public boolean associate(ManifestEntry entry, TokenResponse response) {
        final String id = response.getName();
        log.trace("Sending TokenRegister for {}", id);
        RegisterMessage message = new RegisterMessage(entry.getBag(), response);
        ClientMessage clMessage = clientSession.createMessage(true);
        clMessage.putStringProperty("id", id);
        clMessage.putLongProperty("bagId", entry.getBag().getId());

        try {
            clMessage.writeBodyBufferString(mapper.writeValueAsString(message));
            registerProducer.send(clMessage);
            clientSession.commit();
        } catch (ActiveMQException | JsonProcessingException e) {
            log.error("Unable to send token register message", e);
            return false;
        }

        return true;
    }

    @Override
    public void complete(ManifestEntry entry) {
        // no real use for this here
    }

    @Override
    public Set<ManifestEntry> queuedEntries(int size, long timeout, TimeUnit timeUnit) {
        return Collections.emptySet();
    }

    @Override
    public Map<ManifestEntry, TokenResponse> tokenizedEntries(int size,
                                                              long timeout,
                                                              TimeUnit timeUnit) {
        return Collections.emptyMap();
    }

    @Override
    public boolean isProcessing() {
        boolean processing = false;

        try (ClientConsumer requestQueue = clientSession.createConsumer(REQUEST_TOPIC, true);
             ClientConsumer registerQueue = clientSession.createConsumer(REGISTER_TOPIC, true)) {
            processing = requestQueue.receiveImmediate() != null
                    || registerQueue.receiveImmediate() != null;
        } catch (ActiveMQException e) {
            log.warn("Exception while trying to browse queue!", e);
        }

        return processing;
    }

    public boolean isProcessing(Bag bag) {
        String filter = "bagId = " + bag.getId();
        return isProcessing(filter);
    }

    @Override
    public boolean isProcessing(ManifestEntry entry) {
        String filter = "id = '" + entry.getPath() + "'";
        return isProcessing(filter);
    }

    private boolean isProcessing(String filter) {
        boolean processing = false;

        try (ClientConsumer requestQueue = clientSession.createConsumer(
                REQUEST_TOPIC, filter, true);
             ClientConsumer registerQueue = clientSession.createConsumer(
                     REGISTER_TOPIC, filter, true)) {
            processing = requestQueue.receiveImmediate() != null
                    || registerQueue.receiveImmediate() != null;
        } catch (ActiveMQException e) {
            log.warn("Exception while trying to browse queue!", e);
        }

        return processing;
    }
}
