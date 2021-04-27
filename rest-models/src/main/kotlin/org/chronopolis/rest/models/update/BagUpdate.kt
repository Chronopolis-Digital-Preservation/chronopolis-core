package org.chronopolis.rest.models.update

import org.chronopolis.rest.models.enums.BagStatus

/**
 * Possible updates to a [Bag]
 * @property location the storage location of the [Bag]
 * @property status the [BagStatus] to update
 */
data class BagUpdate(var location: String?,
                     var status: BagStatus)
