package org.chronopolis.rest.api

import org.chronopolis.rest.models.Bag
import org.chronopolis.rest.models.create.BagCreate
import org.chronopolis.rest.models.page.SpringPage
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap
import retrofit2.http.PUT
import org.chronopolis.rest.models.update.BagUpdate

interface BagService {

    /**
     * Get a bag by its id
     *
     * @param bag the id of the bag
     * @return the bag
     */
    @GET("${Paths.BAG_ROOT}/{id}")
    fun get(@Path("id") bag: Long): Call<Bag>

    /**
     * Get all bags from the ingest server, optionally using
     * query parameters to filter the results
     *
     * available query parameters:
     * - createdAfter
     * - createdBefore
     * - updatedAfter
     * - updatedBefore
     * - depositor
     * - name
     * - status
     * @param params the query parameters to filter on
     * @return all bags matching the query
     */
    @GET(Paths.BAG_ROOT)
    fun get(@QueryMap params: Map<String, String>): Call<SpringPage<Bag>>

    /**
     * Deposit a bag to the ingest server
     *
     * @param body the request containing information about the bag
     * @return the newly created Bag
     */
    @POST(Paths.BAG_ROOT)
    fun deposit(@Body body: BagCreate): Call<Bag>

    /**
     * Get a bag by its id
     *
     * @param bag the id of the bag
     * @return the bag
     */
    @PUT("${Paths.BAG_ROOT}/{id}")
    fun update(@Path("id") bag: Long, @Body body: BagUpdate): Call<Bag>
}