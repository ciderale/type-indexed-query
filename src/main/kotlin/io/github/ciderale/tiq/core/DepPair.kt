package io.github.ciderale.tiq.core

import io.github.ciderale.tiq.sample.domain.PagedList
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectJoinStep

class DepPair<T> private constructor(
    private val run: (Condition, DSLContext, ResultMode<*, *>) -> Any,
) {
    @Suppress("UNCHECKED_CAST")
    fun <R> fetch(
        mode: ResultMode<*, R>,
        cond: Condition,
        ctx: DSLContext,
    ): R = run(cond, ctx, mode) as R

    companion object {
        fun <X : Record, T> of(
            select: SelectJoinStep<X>,
            mapper: (X) -> T,
        ): DepPair<T> =
            DepPair { cond, ctx, mode ->
                val q = select.where(cond)

                when (mode) {
                    is ResultMode.One<*> -> {
                        q.fetchSingle(mapper)
                    }

                    is ResultMode.Many -> {
                        q.fetch(mapper)
                    }

                    is ResultMode.Paged -> {
                        val total = ctx.fetchCount(q)
                        val items =
                            q
                                .offset(mode.offset)
                                .limit(mode.limit)
                                .fetch(mapper)

                        PagedList(items, total, mode.offset)
                    }
                }
            }
    }
}
