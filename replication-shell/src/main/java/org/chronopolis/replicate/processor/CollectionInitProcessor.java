/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.replicate.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.chronopolis.amqp.ChronProducer;
import org.chronopolis.common.ace.GsonCollection;
import org.chronopolis.common.transfer.FileTransfer;
import org.chronopolis.common.transfer.HttpsTransfer;
import org.chronopolis.common.transfer.RSyncTransfer;
import org.chronopolis.messaging.base.ChronMessage;
import org.chronopolis.messaging.base.ChronProcessor;
import org.chronopolis.messaging.collection.CollectionInitMessage;
import org.chronopolis.messaging.factory.MessageFactory;
import org.chronopolis.replicate.ReplicationProperties;
import org.chronopolis.replicate.ReplicationQueue;
import org.chronopolis.replicate.util.URIUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: How to reply to collection init message if there is an error
 *
 * @author shake
 */
public class CollectionInitProcessor implements ChronProcessor {
    private static final Logger log = LoggerFactory.getLogger(CollectionInitProcessor.class);

    private final ChronProducer producer;
    private final MessageFactory messageFactory;
    private final ReplicationProperties props;

    public CollectionInitProcessor(ChronProducer producer,
                                   MessageFactory messageFactory,
                                   ReplicationProperties props) {
        this.producer = producer;
        this.messageFactory = messageFactory;
        this.props = props;
    }

    private HttpResponse executeRequest(HttpRequest req) throws IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpHost host = new HttpHost(props.getAceFqdn(), props.getAcePort());

        client.getCredentialsProvider().setCredentials(
                new AuthScope(host.getHostName(),host.getPort()), 
                new UsernamePasswordCredentials(props.getAceUser(), 
                                                props.getAcePass()));

        return client.execute(host, req);
    }

    // Helper to POST to ACE
    private HttpResponse doPost(String url, 
                                HttpEntity entity) throws IOException {
        log.info("Posting to " + url + " with " + entity.toString());
        HttpPost post = new HttpPost(url);
        post.setEntity(entity);
        return executeRequest(post);
    }

    // Helper to reduce a couple lines of code
    private HttpResponse getCollection(String collection,
                                       String group) throws IOException {
        String uri = URIUtil.buildACECollectionGet(props.getAceFqdn(),
                                                   props.getAcePort(),
                                                   props.getAcePath(),
                                                   collection,
                                                   group);
        HttpGet get = new HttpGet(uri);

        return executeRequest(get);
    }

    /**
     * Get the id for a collection in ACE
     *
     * @param collection The collection name
     * @param group The collection's group
     * @return The id of the collection
     * @throws IOException
     * @throws JSONException
     */
    private int getCollectionId(String collection,
                                String group) throws IOException,
                                                     JSONException {
        // Wouldn't it be cool if we could chain together method calls? Like
        // getCollection(c, g).andReturnId()
        // I guess I would need a specific object to handle that though
        HttpResponse response = getCollection(collection, group);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Could not get collection!");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        // The JSON builder is kind of broken
        // luckily we only get a single line back from ACE
        String r = reader.readLine();
        JSONObject responseJson = new JSONObject(r);
        log.debug(responseJson.toString());
        return responseJson.getInt("id");
    }

    /**
     * Check whether a collection exists in ACE
     *
     * @param collection The collection name
     * @param group The collection's group
     * @return true if ACE has the collection, false otherwise
     */
    private boolean hasCollection(String collection, String group) {
        boolean hasCollection = false;
        try {
            HttpResponse response = getCollection(collection, group);
            if (response.getStatusLine().getStatusCode() == 200) {
                hasCollection = true;
            }
        } catch (IOException e) {
            log.error("'{}'", e);
        }

        return hasCollection;
    }

    private void setAceTokenStore(String collection,
                                  String group,
                                  Path collPath,
                                  Path manifest,
                                  String fixityAlg,
                                  int auditPeriod) throws IOException {
        log.trace("Building ACE json");
        GsonCollection aceGson = new GsonCollection();
        aceGson.setDigestAlgorithm(fixityAlg);
        aceGson.setDirectory(collPath.toString());
        aceGson.setName(collection);
        aceGson.setGroup(group);
        aceGson.setStorage("local");
        aceGson.setAuditPeriod(String.valueOf(auditPeriod));
        aceGson.setAuditTokens("true");
        aceGson.setProxyData("false");

        // Build and POST our collection
        StringEntity entity = new StringEntity(aceGson.toJson(),
                ContentType.APPLICATION_JSON);
        String uri = URIUtil.buildACECollectionPost(props.getAceFqdn(),
                props.getAcePort(),
                props.getAcePath());
        HttpResponse req = doPost(uri, entity);
        log.info(req.getStatusLine().toString());
        if ( req.getStatusLine().getStatusCode() != 200 ) {
            throw new RuntimeException("Could not POST collection");
        }

        // Get the ID of the newly made collection
        int id = getCollectionId(collection, group);

        // Now let's POST the token store
        FileBody body = new FileBody(manifest.toFile());
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("tokenstore", body);
        uri = URIUtil.buildACETokenStorePost(props.getAceFqdn(),
                props.getAcePort(),
                props.getAcePath(),
                id);
        doPost(uri, mpEntity);
    }


    // TODO: Check to make sure we don't already have this collection (do a new version if we do?)
    // TODO: Reply if there is an error with the collection (ie: already registered in ace), or ack
    @Override
    public void process(ChronMessage chronMessage) {
        // TODO: Replace these with the values from the properties
        boolean checkCollection = false;
        boolean register = true;

        if(!(chronMessage instanceof CollectionInitMessage)) {
            // Error out
            log.error("Incorrect Message Type");
            return;
        }

        log.trace("Received collection init message");
        CollectionInitMessage msg = (CollectionInitMessage) chronMessage;

        Path manifest;
        Path bagPath = Paths.get(props.getStage(), msg.getDepositor());
        Path collPath = Paths.get(bagPath.toString(), msg.getCollection());
        String fixityAlg = msg.getFixityAlgorithm();
        String protocol = msg.getProtocol();
        String collection = msg.getCollection();
        String group = msg.getDepositor();
        int auditPeriod = msg.getAuditPeriod();

        if (checkCollection) {
            if (hasCollection(msg.getCollection(), msg.getDepositor())) {
                log.error("Already registered collection '{}'", msg.getCollection());
            }
        }

        try { 
            log.info("Downloading manifest " + msg.getTokenStore());
            manifest = ReplicationQueue.getFileImmediate(msg.getTokenStore(),
                                                         Paths.get(props.getStage()),
                                                         protocol);
        } catch (IOException ex) {
            log.error("Error downloading manifest \n{}", ex);
            return;
        }

        FileTransfer transfer;
        String location = msg.getBagLocation();
        // TODO: We'll probably end up just using the entire location for the
        // rsync command instead of splitting it in the future
        String[] parts = location.split("@", 2);
        if ( protocol.equalsIgnoreCase("https")) {
            transfer = new HttpsTransfer();
        } else {
            String user = parts[0];
            transfer = new RSyncTransfer(user);
        }

        transfer.getFile(parts[1], bagPath);
        /*
        TokenStoreReader reader;
        try {
            reader = new TokenStoreReader(Files.newInputStream(manifest, 
                                          StandardOpenOption.READ), 
                                          "UTF-8");
            // Will be
            // baseURL + depositor (group) + collection + file
            String url = msg.getTokenStore();
            while ( reader.hasNext()) {
                TokenStoreEntry entry = reader.next();
                for ( String identifier : entry.getIdentifiers() ) {
                    log.debug("Downloading " + identifier);
                    //Path download = Paths.get(collPath.toString(), identifier);
                    /*
                     * Let's get this to work with downloads first, then worry about
                     * multithreading later
                    ReplicationQueue.getFileAsync(url, 
                                                  msg.getCollection(), 
                                                  msg.getDepositor(), 
                                                  identifier,
                                                  protocol);
                                                  /
                }
            }
        } catch (IOException ex) {
            log.error("IO Exception while reading token store \n{}", ex);
            return;
        }
        */

        try {
            if (register) {
                setAceTokenStore(collection, group, collPath, manifest, fixityAlg, auditPeriod);
            }
        } catch (IOException ex) {
            log.error("IO Error", ex);
        }
        
        // Because I'm bad at reading - Collection Init Complete Message
        log.info("Sending response");
        ChronMessage response = messageFactory.collectionInitCompleteMessage(msg.getCorrelationId());
        producer.send(response, chronMessage.getReturnKey());
    }
    
}
