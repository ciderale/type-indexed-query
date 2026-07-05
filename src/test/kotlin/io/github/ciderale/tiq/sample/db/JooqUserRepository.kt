package io.github.ciderale.tiq.sample.db

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.OrderingDirection
import io.github.ciderale.tiq.core.QuerySpec
import io.github.ciderale.tiq.core.jooq.JooqQueryTranslatorImpl
import io.github.ciderale.tiq.core.jooq.JooqQueryTranslatorImpl.addOrdering
import io.github.ciderale.tiq.core.jooq.addClassicFetcher
import io.github.ciderale.tiq.core.jooq.ascending
import io.github.ciderale.tiq.core.jooq.descending
import io.github.ciderale.tiq.core.jooq.fetch
import io.github.ciderale.tiq.sample.UserRepository
import io.github.ciderale.tiq.sample.domain.UserDetail
import io.github.ciderale.tiq.sample.domain.UserSummary
import org.jooq.DSLContext
import org.jooq.Records.mapping
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
        addOrdering(UserRepository.OrderBy.NAME, USER.NAME::descending, USER.ID::ascending)
        addOrdering(UserRepository.OrderBy.ID, USER.ID::ascending)
        JooqQueryTranslatorImpl.addProjection(
            UserRepository.Summary,
            { ctx -> ctx.select(USER.ID, USER.NAME).from(USER) },
            mapping(::UserSummary),
        )
        JooqQueryTranslatorImpl.addProjection(
            UserRepository.Detail,
            { ctx -> ctx.select(USER.ID, USER.NAME, USER.EMAIL).from(USER) },
            mapping(::UserDetail),
        )
        JooqQueryTranslatorImpl.addClassicFetcher()
    }

    override fun <T, R> fetch(
        query: UserRepository.Query,
        projection: UserRepository.UP<T>,
        fetcher: Fetcher<T, R>,
        ordering: UserRepository.OrderBy,
        direction: OrderingDirection,
    ): R =
        ctx.fetch(
            QuerySpec(query = query, ordering = ordering, direction = direction, projection, fetcher),
            JooqQueryTranslatorImpl,
        )
}
