package io.github.ciderale.tiq.core

import io.github.ciderale.tiq.sample.domain.PagedList
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectJoinStep

interface SelectMappingPair<T> {
    fun <R> fetch(
        mode: ResultMode<T, R>,
        cond: Condition,
        ctx: DSLContext,
    ): R

    companion object {
        fun <X : Record, T> of(
            select: (DSLContext) -> SelectJoinStep<X>,
            mapper: (X) -> T,
        ): SelectMappingPair<T> =
            object : SelectMappingPair<T> {
                override fun <R> fetch(
                    mode: ResultMode<T, R>,
                    cond: Condition,
                    ctx: DSLContext,
                ): R {
                    val sqlQuery = select(ctx).where(cond)

                    return when (mode) {
                        is ResultMode.Count<*> -> {
                            ctx.fetchCount(sqlQuery)
                        }

                        is ResultMode.One<*> -> {
                            sqlQuery.fetchSingle(mapper)
                        }

                        is ResultMode.Many -> {
                            sqlQuery.fetch(mapper)
                        }

                        is ResultMode.Paged -> {
                            val total = ctx.fetchCount(sqlQuery)
                            val items =
                                sqlQuery
                                    .offset(mode.offset)
                                    .limit(mode.limit)
                                    .fetch(mapper)

                            PagedList(items, offset = mode.offset, total = total)
                        }
                    } as R
                }
            }
    }
}
