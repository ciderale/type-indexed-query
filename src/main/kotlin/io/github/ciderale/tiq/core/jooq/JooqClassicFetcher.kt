package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.ClassicFetcher
import io.github.ciderale.tiq.core.PagedList
import io.github.ciderale.tiq.core.jooq.JooqQueryComponents
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectConditionStep

fun JooqQueryTranslatorImpl.addClassicFetcher() {
    JooqQueryTranslatorImpl.addFetcher<Any, Int, ClassicFetcher.Count<Any>> { FetchCount() }
    JooqQueryTranslatorImpl.addFetcher<Any, Any, ClassicFetcher.One<Any>> { FetchOne() }
    JooqQueryTranslatorImpl.addFetcher<Any, List<Any>, ClassicFetcher.Many<Any>> { FetchMany() }
    JooqQueryTranslatorImpl.addFetcher<Any, PagedList<Any>, ClassicFetcher.Paged<Any>>(::FetchPaged)
}

class FetchCount<T> : JooqQueryComponents.Fetcher<Record, T, Int> {
    override fun invoke(
        ctx: DSLContext,
        sqlQuery: SelectConditionStep<Record>,
        mapper: (Record) -> T,
    ): Int = ctx.fetchCount(sqlQuery)
}

class FetchOne<T> : JooqQueryComponents.Fetcher<Record, T, T> {
    override fun invoke(
        ctx: DSLContext,
        sqlQuery: SelectConditionStep<Record>,
        mapper: (Record) -> T,
    ): T = sqlQuery.fetchSingle(mapper)
}

class FetchMany<T> : JooqQueryComponents.Fetcher<Record, T, List<T>> {
    override fun invoke(
        ctx: DSLContext,
        sqlQuery: SelectConditionStep<Record>,
        mapper: (Record) -> T,
    ): List<T> = sqlQuery.fetch(mapper)
}

class FetchPaged<T>(
    val mode: ClassicFetcher.Paged<T>,
) : JooqQueryComponents.Fetcher<Record, T, PagedList<T>> {
    override fun invoke(
        ctx: DSLContext,
        sqlQuery: SelectConditionStep<Record>,
        mapper: (Record) -> T,
    ): PagedList<T> {
        val total = ctx.fetchCount(sqlQuery)
        val items =
            sqlQuery
                .offset(mode.offset)
                .limit(mode.limit)
                .fetch(mapper)

        return PagedList(items, offset = mode.offset, total = total)
    }
}
