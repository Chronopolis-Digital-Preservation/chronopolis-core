package org.chronopolis.rest.models

import com.google.common.collect.ComparisonChain
import org.chronopolis.rest.models.enums.BagStatus
import java.time.ZonedDateTime

data class ProjectBag(val id: Long,
        val name: String,
        val creator: String,
        val size: Long,
        val totalFiles: Long,
        val status: BagStatus,
        val createdAt: ZonedDateTime,
        val updatedAt: ZonedDateTime,
        val depositor: String)
