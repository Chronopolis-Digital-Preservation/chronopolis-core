package org.chronopolis.rest.api

import org.chronopolis.rest.api.Paths.REPLICATION_ROOT
import org.chronopolis.rest.models.Replication
import org.chronopolis.rest.models.enums.ReplicationStatus;
import org.chronopolis.rest.models.create.ReplicationCreate
import org.chronopolis.rest.models.page.SpringPage
import org.chronopolis.rest.models.update.FixityUpdate
import org.chronopolis.rest.models.update.ReplicationStatusUpdate
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import retrofit2.http.Query

/**
 * Service to interact with the Replication API in the ingest server
 *
 * @author shake
 */
interface ReplicationService {

    /**
     * Get a replication by its id
     *
     * @param id the id of the replication
     * @return the replication
     */
    @GET("$REPLICATION_ROOT/{id}")
    fun get(@Path("id") id: Long): Call<Replication>

    /**
     * Get all replications, optionally filtered with the query parameters
     *
     * available query parameters:
     * - createdAfter
     * - createdBefore
     * - updatedAfter
     * - updatedBefore
     * - nodeUsername
     * - status
     *
     * @param params the map of parameters to filter on
     * @return all replications which match the query
     */
    @GET(REPLICATION_ROOT)
    fun get(@QueryMap params: Map<String, String>): Call<SpringPage<Replication>>

    /**
     * Get all replications, filtered by the query parameters provided
     *
     * @param page The page to retrieve
     * @param page_size Page size
     * @param node User name
     * @param status List of ReplicationStatus
     * @return all replications which match the query
     */
    @GET(REPLICATION_ROOT)
    fun get(@Query("page") page: Int,
            @Query("page_size") page_size: Int,
            @Query("node") node: String,
            @Query("status") status: List<String>): Call<SpringPage<Replication>>

    /**
     * Create a Replication request
     *
     * @param body information for which bag to create the replication for and
     *             where the replication should go
     * @return the newly created replication
     */
    @POST(REPLICATION_ROOT)
    fun create(@Body body: ReplicationCreate): Call<Replication>

    /**
     * Update the fixity value for a received token store
     *
     * @param id the id of the replication
     * @param update the new fixity value
     * @return the updated replication
     */
    @PUT("$REPLICATION_ROOT/{id}/tokenstore")
    fun updateTokenStoreFixity(@Path("id") id: Long, @Body update: FixityUpdate): Call<Replication>

    /**
     * Update the fixity value for a received tag manifest
     *
     * @param id the id of the replication
     * @param update the new fixity value
     * @return the updated replication
     */
    @PUT("$REPLICATION_ROOT/{id}/tagmanifest")
    fun updateTagManifestFixity(@Path("id") id: Long, @Body update: FixityUpdate): Call<Replication>

    /**
     * Update the status of a replication
     *
     * @param id the id of the replication
     * @param update the status to set
     * @return the updated replication
     */
    @PUT("$REPLICATION_ROOT/{id}/status")
    fun updateStatus(@Path("id") id: Long,
                     @Body update: ReplicationStatusUpdate): Call<Replication>

    /**
     * Fail a replication
     *
     * @param id the id of the replication
     * @return the updated replication
     */
    @PUT("$REPLICATION_ROOT/{id}/failure")
    fun fail(@Path("id") id: Long): Call<Replication>

}