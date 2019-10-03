package org.chronopolis.ingest.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.querydsl.core.types.Predicate;
import org.chronopolis.ingest.repository.dao.BagFileDao;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.BagFile;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.QBagFile;
import org.chronopolis.rest.entities.QDataFile;
import org.chronopolis.rest.entities.TokenStore;
import org.chronopolis.rest.entities.storage.Fixity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Simple tests for the FileController
 * <p>
 * note: if a controller method simply delegates to the dao, we don't test here
 * todo: check json serialization
 *
 * @author shake
 */
@RunWith(SpringRunner.class)
@SuppressWarnings("Duplicates")
@WebMvcTest(controllers = FileController.class)
public class FileControllerTest extends ControllerTest {

    private final Bag THE_BAG = new Bag();

    @MockBean
    BagFileDao dao;

    @Before
    public void setup() {
        FileController controller = new FileController(dao);
        setupMvc(controller);
    }

    @Test
    public void getFiles() throws Exception {
        BagFile bf = new BagFile();
        bf.setId(1L);
        bf.setSize(1L);
        bf.setBag(THE_BAG);
        bf.setDtype("BAG");
        bf.setFilename("/bag-file");
        bf.setFixities(ImmutableSet.of());

        Pageable page = PageRequest.of(5, 5);

        when(dao.findPage(eq(QBagFile.bagFile), any()))
                .thenReturn(PageableExecutionUtils.getPage(ImmutableList.of(bf), page, () -> 1));

        mvc.perform(get("/api/files")
                .principal(authorizedPrincipal))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getBagFiles() throws Exception {
        BagFile bf = new BagFile();
        bf.setId(1L);
        bf.setSize(1L);
        bf.setBag(THE_BAG);
        bf.setDtype("BAG");
        bf.setFilename("/bag-file");

        Pageable page = PageRequest.of(5, 5);
        when(dao.findPage(eq(QBagFile.bagFile), any()))
                .thenReturn(PageableExecutionUtils.getPage(ImmutableList.of(bf), page, () -> 1));

        mvc.perform(get("/api/bags/{id}/files", 1L)
                .principal(authorizedPrincipal))
                .andExpect(status().isOk());
    }

    @Test
    public void getFile() throws Exception {
        TokenStore ts = new TokenStore();
        ts.setId(2L);
        ts.setSize(1L);
        ts.setBag(THE_BAG);
        ts.setFilename("/tokens");

        when(dao.findOne(eq(QDataFile.dataFile), any(Predicate.class))).thenReturn(ts);

        mvc.perform(get("/api/files/{id}", 2L)
                .principal(authorizedPrincipal))
                .andDo(print())
                .andExpect(status().isOk());

        verify(dao, times(1)).findOne(eq(QDataFile.dataFile), any(Predicate.class));
    }

    @Test
    public void getFileNotFound() throws Exception {
        when(dao.findOne(eq(QDataFile.dataFile), any(Predicate.class))).thenReturn(null);

        mvc.perform(get("/api/files/{id}", 2L)
                .principal(authorizedPrincipal))
                .andExpect(status().isNotFound());

        verify(dao, times(1)).findOne(eq(QDataFile.dataFile), any(Predicate.class));
    }

    @Test
    public void getBagFile() throws Exception {
        BagFile bf = new BagFile();
        bf.setSize(1L);
        bf.setBag(THE_BAG);
        bf.setFilename("/bag-file");
        when(dao.findOne(eq(QBag.bag), any(Predicate.class))).thenReturn(THE_BAG);
        when(dao.findOne(eq(QBagFile.bagFile), any(Predicate.class))).thenReturn(bf);

        mvc.perform(get("/api/bags/{bag_id}/files/{file_id}", 1L, 2L)
                .principal(authorizedPrincipal))
                .andExpect(status().isOk());

        verify(dao, times(1)).findOne(eq(QBag.bag), any(Predicate.class));
        verify(dao, times(1)).findOne(eq(QBagFile.bagFile), any(Predicate.class));
    }

    @Test
    public void getBagFileBadRequest() throws Exception {
        BagFile bf = new BagFile();
        when(dao.findOne(eq(QBag.bag), any(Predicate.class))).thenReturn(null);
        when(dao.findOne(eq(QBagFile.bagFile), any(Predicate.class))).thenReturn(bf);

        mvc.perform(get("/api/bags/{bag_id}/files/{file_id}", 1L, 2L)
                .principal(authorizedPrincipal))
                .andExpect(status().isBadRequest());

        verify(dao, times(1)).findOne(eq(QBag.bag), any(Predicate.class));
        verify(dao, times(1)).findOne(eq(QBagFile.bagFile), any(Predicate.class));
    }

    @Test
    public void getBagFileNotFound() throws Exception {
        when(dao.findOne(eq(QBag.bag), any(Predicate.class))).thenReturn(THE_BAG);
        when(dao.findOne(eq(QBagFile.bagFile), any(Predicate.class))).thenReturn(null);

        mvc.perform(get("/api/bags/{bag_id}/files/{file_id}", 1L, 2L)
                .principal(authorizedPrincipal))
                .andExpect(status().isNotFound());

        verify(dao, times(1)).findOne(eq(QBag.bag), any(Predicate.class));
        verify(dao, times(1)).findOne(eq(QBagFile.bagFile), any(Predicate.class));
    }


    @Test
    public void getBagFileFixities() throws Exception {
        Fixity fixity = new Fixity();
        fixity.setAlgorithm("SHA-256");
        fixity.setCreatedAt(ZonedDateTime.now());
        fixity.setValue("that-one-empty-value");
        BagFile bagFile = new BagFile();
        bagFile.setFixities(ImmutableSet.of(fixity));

        when(dao.findOne(eq(QBag.bag), any(Predicate.class))).thenReturn(THE_BAG);
        when(dao.findOne(eq(QBagFile.bagFile), any(Predicate.class))).thenReturn(bagFile);

        mvc.perform(get("/api/bags/{bag_id}/files/{file_id}/fixity", 1L, 2L)
                .principal(authorizedPrincipal))
                .andExpect(status().isOk());

        verify(dao, times(1)).findOne(eq(QBag.bag), any(Predicate.class));
        verify(dao, times(1)).findOne(eq(QBagFile.bagFile), any(Predicate.class));
    }

    @Test
    public void getBagFileFixitiesBagIsNull() throws Exception {
        BagFile bagFile = new BagFile();
        bagFile.setFixities(ImmutableSet.of());

        when(dao.findOne(eq(QBag.bag), any(Predicate.class))).thenReturn(null);
        when(dao.findOne(eq(QBagFile.bagFile), any(Predicate.class))).thenReturn(bagFile);

        mvc.perform(get("/api/bags/{bag_id}/files/{file_id}/fixity", 1L, 2L)
                .principal(authorizedPrincipal))
                .andExpect(status().isBadRequest());

        verify(dao, times(1)).findOne(eq(QBag.bag), any(Predicate.class));
        verify(dao, times(1)).findOne(eq(QBagFile.bagFile), any(Predicate.class));
    }

    @Test
    public void getBagFileFixitiesFileIsNull() throws Exception {
        when(dao.findOne(eq(QBag.bag), any(Predicate.class))).thenReturn(THE_BAG);
        when(dao.findOne(eq(QBagFile.bagFile), any(Predicate.class))).thenReturn(null);

        mvc.perform(get("/api/bags/{bag_id}/files/{file_id}/fixity", 1L, 2L)
                .principal(authorizedPrincipal))
                .andExpect(status().isBadRequest());

        verify(dao, times(1)).findOne(eq(QBag.bag), any(Predicate.class));
        verify(dao, times(1)).findOne(eq(QBagFile.bagFile), any(Predicate.class));
    }
}