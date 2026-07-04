package io.github.ciderale.tiq.sample.db

import io.github.ciderale.tiq.core.FetchSpec
import io.github.ciderale.tiq.core.SelectMappingPair
import io.github.ciderale.tiq.core.SelectMappingRegistry
import io.github.ciderale.tiq.core.fetch
import io.github.ciderale.tiq.sample.UserRepository
import io.github.ciderale.tiq.sample.domain.UserDetail
import io.github.ciderale.tiq.sample.domain.UserSummary
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL

class JooqUserRepository(
    private val ctx: DSLContext,
) : UserRepository {
    override fun <T, R> fetch(
        query: UserRepository.Query,
        spec: FetchSpec<T, R, UserRepository.UP<T>>,
    ): R = ctx.fetch(query.toCondition(), spec, registry)
}

fun UserRepository.Query.toCondition(): Condition =
    DSL.and(
        id?.let(USER.ID::eq),
        activeOnly?.let(USER.ACTIVE::eq),
    )

val summaryMapping =
    SelectMappingPair.of(
        { ctx -> ctx.select(USER.ID, USER.NAME).from(USER) },
        { UserSummary(it[USER.ID], it[USER.NAME]) },
    )

val detailMapping =
    SelectMappingPair.of(
        { ctx -> ctx.select(USER.ID, USER.NAME, USER.EMAIL).from(USER) },
        { UserDetail(id = it[USER.ID]!!, name = it[USER.NAME]!!, email = it[USER.EMAIL]!!) },
    )

val registry =
    SelectMappingRegistry.build {
        UserRepository.Summary put summaryMapping
        UserRepository.Detail put detailMapping
    }
