package io.github.ciderale.tiq.sample.db

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.QuerySpec
import io.github.ciderale.tiq.core.jooq.JooqQueryTranslatorImpl
import io.github.ciderale.tiq.core.jooq.addClassicFetcher
import io.github.ciderale.tiq.core.jooq.fetch
import io.github.ciderale.tiq.sample.UserRepository
import io.github.ciderale.tiq.sample.domain.UserDetail
import io.github.ciderale.tiq.sample.domain.UserSummary
import org.jooq.DSLContext
import org.jooq.impl.DSL

class JooqUserRepository(
    private val ctx: DSLContext,
) : UserRepository {
    init {
        JooqQueryTranslatorImpl.addQuery<UserRepository.Query> {
            DSL.and(
                it.id?.let(USER.ID::eq),
                it.activeOnly?.let(USER.ACTIVE::eq),
            )
        }
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
        JooqQueryTranslatorImpl.addClassicFetcher()
    }

    override fun <T, R> fetch(
        query: UserRepository.Query,
        mode: Fetcher<T, R>,
        projection: UserRepository.UP<T>,
    ): R = ctx.fetch(QuerySpec(query, projection, mode), JooqQueryTranslatorImpl)
}
