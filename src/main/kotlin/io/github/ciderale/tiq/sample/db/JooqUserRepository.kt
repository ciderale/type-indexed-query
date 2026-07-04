package io.github.ciderale.tiq.sample.db

import io.github.ciderale.tiq.core.DepPair
import io.github.ciderale.tiq.core.FetchSpec
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
    ): R {
        val cond = condition(query)
        val pair: DepPair<T> = mappingPair(spec.projection)
        return pair.fetch(spec.mode, cond, ctx)
    }

    private fun condition(query: UserRepository.Query): Condition =
        DSL.and(
            query.id?.let(USER.ID::eq),
            query.activeOnly?.let(USER.ACTIVE::eq),
        )
}

private fun <T> mappingPair(projection: UserRepository.UP<T>): DepPair<T> =
    when (projection) {
        UserRepository.Summary -> {
            DepPair.of(
                { ctx -> ctx.select(USER.ID, USER.NAME).from(USER) },
            ) { UserSummary(it[USER.ID], it[USER.NAME]) as T }
        }

        UserRepository.Detail -> {
            DepPair.of(
                { ctx -> ctx.select(USER.ID, USER.NAME, USER.EMAIL).from(USER) },
            ) {
                UserDetail(
                    id = it[USER.ID]!!,
                    name = it[USER.NAME]!!,
                    email = it[USER.EMAIL]!!,
                ) as T
            }
        }
    }
