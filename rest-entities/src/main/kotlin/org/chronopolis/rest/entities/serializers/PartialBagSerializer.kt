package org.chronopolis.rest.entities.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.chronopolis.rest.entities.projections.PartialBag
import org.chronopolis.rest.models.ProjectBag

class PartialBagSerializer : JsonSerializer<PartialBag>() {
    override fun serialize(part: PartialBag, gen: JsonGenerator, provider: SerializerProvider?) {
        gen.writeObject(part.toModel())
    }
}

private fun PartialBag.toModel(): ProjectBag {
    return ProjectBag(
            id,
            name,
            creator,
            size,
            totalFiles,
            status,
            createdAt,
            updatedAt,
            depositor,
            replicatingNodes
    )
}