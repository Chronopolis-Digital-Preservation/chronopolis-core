package org.chronopolis.rest.models

import java.time.ZonedDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class AceToken(val id: Long,
                    val bagId: Long,

                    @NotNull
                    val round: Long,

                    @NotBlank
                    val proof: String,

                    @NotBlank
                    val imsHost: String,

                    @NotBlank
                    val filename: String,

                    @NotBlank
                    val algorithm: String,

                    @NotBlank
                    val imsService: String,

                    @NotNull
                    val createDate: ZonedDateTime)
