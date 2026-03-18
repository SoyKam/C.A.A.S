package com.caas.app.domain.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.data.model.BusinessMember

interface BusinessRepository {
    suspend fun createBusinessWithOwner(
        business: Business,
        ownerMember: BusinessMember
    ): Result<Business>
}