package com.snuti.exparchiveserver.user.dto

import jakarta.validation.constraints.Size

data class UserInterestUpdateRequest(
    @field:Size(max = 30)
    val tagIds: List<Long>
)