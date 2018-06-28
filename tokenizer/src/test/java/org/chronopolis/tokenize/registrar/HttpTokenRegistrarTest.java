package org.chronopolis.tokenize.registrar;

import com.google.common.collect.ImmutableMap;
import edu.umiacs.ace.ims.ws.TokenResponse;
import org.chronopolis.common.ace.AceConfiguration;
import org.chronopolis.rest.api.TokenService;
import org.chronopolis.rest.models.AceTokenModel;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.test.support.CallWrapper;
import org.chronopolis.test.support.ErrorCallWrapper;
import org.chronopolis.test.support.ExceptingCallWrapper;
import org.chronopolis.tokenize.ManifestEntry;
import org.chronopolis.tokenize.supervisor.TokenWorkSupervisor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpTokenRegistrarTest {
    private final Logger log = LoggerFactory.getLogger(HttpTokenRegistrarTest.class);

    // include extraneous characters?
    private final String path = "data/path/to/file.txt";
    private final String host = "test-ims-host";
    private final String name = "test-name";
    private final String digest = "digest";
    private final String service = "test-service";
    private final String provider = "test-provider";
    private final String depositor = "test-depositor";
    private final String tokenClass = "test-token-class";
    private final Long id = 1L;
    private final Long round = 1L;
    private final Integer status = 1;

    private ManifestEntry entry;
    private AceTokenModel model;
    private TokenResponse response;
    private HttpTokenRegistrar registrar;

    @Mock
    private TokenService tokens;
    @Mock
    private TokenWorkSupervisor supervisor;

    @Before
    public void setup() throws DatatypeConfigurationException {
        tokens = mock(TokenService.class);
        supervisor = mock(TokenWorkSupervisor.class);

        Bag bag = new Bag();
        bag.setId(id);
        bag.setName(name);
        bag.setDepositor(depositor);
        entry = new ManifestEntry(bag, path, digest);
        entry.setCalculatedDigest(digest);

        response = new TokenResponse();
        response.setDigestProvider(provider);
        response.setDigestService(service);
        response.setName(entry.tokenName());
        response.setRoundId(round);
        response.setStatusCode(status);

        XMLGregorianCalendar calendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.now()));
        response.setTimestamp(calendar);
        response.setTokenClassName(tokenClass);

        AceConfiguration configuration = new AceConfiguration()
                .setIms(new AceConfiguration.Ims().setEndpoint("test-ims-endpoint"));
        registrar = new HttpTokenRegistrar(tokens, supervisor, configuration);

        model = new AceTokenModel()
                .setCreateDate(ZonedDateTime.now())
                .setRound(round)
                .setImsService(service)
                .setProof("test-proof")
                .setFilename(path)
                .setBagId(id)
                .setId(id)
                .setAlgorithm(provider);
    }

    @Test
    public void get() {
        CallWrapper<AceTokenModel> success = new CallWrapper<>(model);
        when(tokens.createToken(eq(id), any(AceTokenModel.class))).thenReturn(success);

        registrar.register(ImmutableMap.of(entry, response));
        verify(tokens, times(1)).createToken(eq(id), any(AceTokenModel.class));
        verify(supervisor, times(1)).complete(eq(entry));
    }

    @Test
    public void registerFailWithException() {
        ExceptingCallWrapper<AceTokenModel> exception = new ExceptingCallWrapper<>(model);

        when(tokens.createToken(eq(id), any(AceTokenModel.class))).thenReturn(exception);

        registrar.register(ImmutableMap.of(entry, response));
        verify(tokens, times(1)).createToken(eq(id), any(AceTokenModel.class));
        verify(supervisor, times(1)).retryRegister(eq(entry));
    }

    @Test
    public void registerFail4xxError() {
        ErrorCallWrapper<AceTokenModel> error = new ErrorCallWrapper<>(model, 404, "Bag not found");
        when(tokens.createToken(eq(id), any(AceTokenModel.class))).thenReturn(error);

        registrar.register(ImmutableMap.of(entry, response));
        verify(tokens, times(1)).createToken(eq(id), any(AceTokenModel.class));
        verify(supervisor, times(1)).complete(eq(entry));
    }

    @Test
    public void register409Success() {
        ErrorCallWrapper<AceTokenModel> error = new ErrorCallWrapper<>(model, 409, "Token exists");
        when(tokens.createToken(eq(id), any(AceTokenModel.class))).thenReturn(error);

        registrar.register(ImmutableMap.of(entry, response));
        verify(tokens, times(1)).createToken(eq(id), any(AceTokenModel.class));
        verify(supervisor, times(1)).complete(eq(entry));
    }

    @Test
    public void getFilename() {
        String filename = registrar.getFilename(response);
        Assert.assertEquals(path, filename);
    }

    @Test
    public void regex() {
        Pattern pattern = Pattern.compile("\\(.*?,.*?\\)::(.*)");
        Matcher matcher = pattern.matcher(response.getName());

        boolean matches = matcher.matches();
        int groups = matcher.groupCount();
        String group = matcher.group(1);

        log.info("Matches? {}", matches);
        log.info("Groups: {}", groups);
        log.info("Group: {}", group);
        Assert.assertTrue(matches);
        Assert.assertEquals(1, groups);
        Assert.assertEquals(path, group);
    }

}