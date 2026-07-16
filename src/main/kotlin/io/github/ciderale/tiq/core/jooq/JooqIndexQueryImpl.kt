package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.QuerySpec
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.SelectJoinStep
import org.jooq.SelectQuery
import org.jooq.SortField

fun <Q, T, R> DSLContext.fetch(
    spec: QuerySpec<Q, T, R>,
    translator: JooqQueryTranslator,
) = translator.translate(spec).execute(this)

interface JooqQueryTranslator {
    fun <Q, T, R> translate(spec: QuerySpec<Q, T, R>): JooqQueryComponents<*, T, R>
}

data class JooqQueryComponents<X : Record, T, R>(
    val condition: Condition,
    val select: Selector<X>,
    val ordering: Ordering,
    val mapper: Mapper<X, T>,
    val fetch: Fetcher<X, T, R>,
) {
    typealias Selector<X> = (DSLContext) -> SelectJoinStep<X>
    typealias Ordering = List<SortField<*>>
    typealias Mapper<X, T> = RecordMapper<X, T>
    typealias Fetcher<X, T, R> = (DSLContext, SelectQuery<X>, RecordMapper<X, T>) -> R

    fun execute(ctx: DSLContext): R {
        val sqlQuery = select(ctx).where(condition).orderBy(ordering)
        return fetch(ctx, sqlQuery.query, mapper)
    }
}
