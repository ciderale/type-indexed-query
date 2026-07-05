package io.github.ciderale.tiq.sample.db

import io.github.ciderale.tiq.core.QuerySpec
import io.github.ciderale.tiq.core.ResultMode
import io.github.ciderale.tiq.core.jooq.FetchCount
import io.github.ciderale.tiq.core.jooq.FetchMany
import io.github.ciderale.tiq.core.jooq.FetchOne
import io.github.ciderale.tiq.core.jooq.FetchPaged
import io.github.ciderale.tiq.core.jooq.JooqQueryTranslatorImpl
import io.github.ciderale.tiq.core.jooq.fetch
import io.github.ciderale.tiq.sample.UserRepository
import io.github.ciderale.tiq.sample.domain.PagedList
import io.github.ciderale.tiq.sample.domain.UserDetail
import io.github.ciderale.tiq.sample.domain.UserSummary
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL

class JooqUserRepository(
    private val ctx: DSLContext,
) : UserRepository {
    init {
        JooqQueryTranslatorImpl.addQuery<UserRepository.Query>({ it.toCondition() })
        JooqQueryTranslatorImpl.addProjection(
            UserRepository.Summary,
            { ctx -> ctx.select(USER.ID, USER.NAME).from(USER) },
            { UserSummary(it[USER.ID], it[USER.NAME]) },
        )
        JooqQueryTranslatorImpl.addProjection(
            UserRepository.Detail,
            { ctx -> ctx.select(USER.ID, USER.NAME, USER.EMAIL).from(USER) },
            { UserDetail(id = it[USER.ID]!!, name = it[USER.NAME]!!, email = it[USER.EMAIL]!!) },
        )

        JooqQueryTranslatorImpl.addFetcher<Any, Int, ResultMode.Count<Any>> { FetchCount() }
        JooqQueryTranslatorImpl.addFetcher<Any, Any, ResultMode.One<Any>> { FetchOne() }
        JooqQueryTranslatorImpl.addFetcher<Any, List<Any>, ResultMode.Many<Any>> { FetchMany() }
        JooqQueryTranslatorImpl.addFetcher<Any, PagedList<Any>, ResultMode.Paged<Any>>(::FetchPaged)
    }

    override fun <T, R> fetch(
        query: UserRepository.Query,
        mode: ResultMode<T, R>,
        projection: UserRepository.UP<T>,
    ): R = ctx.fetch(QuerySpec(query, projection, mode), JooqQueryTranslatorImpl)
}

fun UserRepository.Query.toCondition(): Condition =
    DSL.and(
        id?.let(USER.ID::eq),
        activeOnly?.let(USER.ACTIVE::eq),
    )
