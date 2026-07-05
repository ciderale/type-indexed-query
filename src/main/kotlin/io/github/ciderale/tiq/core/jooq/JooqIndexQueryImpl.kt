package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.QuerySpec
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.SelectJoinStep
import org.jooq.SelectSeekStep1
import org.jooq.impl.DSL

interface JooqQueryTranslator {
    fun <Q, T, R> translate(spec: QuerySpec<Q, T, R>): JooqQueryComponents<*, T, R>
}

data class JooqQueryComponents<X : Record, T, R>(
    val condition: Condition,
    val select: Selector<X>,
    val sorter: Sorter<X>,
    val mapper: Mapper<X, T>,
    val fetch: Fetcher<X, T, R>,
) {
    typealias Selector<X> = (DSLContext) -> SelectJoinStep<X>
    typealias Sorter<X> = (SelectConditionStep<X>) -> SelectSeekStep1<X, *>
    typealias Mapper<X, T> = (X) -> T
    typealias Fetcher<X, T, R> = (DSLContext, SelectSeekStep1<X, *>, (X) -> T) -> R

    fun execute(ctx: DSLContext): R {
        val sqlQuery = select(ctx).where(condition).let(sorter)
        return fetch(ctx, sqlQuery, mapper)
    }
}

fun <Q, T, R> DSLContext.fetch(
    spec: QuerySpec<Q, T, R>,
    translator: JooqQueryTranslator,
) = translator.translate(spec).execute(this)
