package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.core.QuerySpec
import org.jooq.Condition
import org.jooq.Record
import kotlin.reflect.KClass

object JooqQueryTranslatorImpl : JooqQueryTranslator {
    override fun <Q, T, R> translate(spec: QuerySpec<Q, T, R>): JooqQueryComponents<Record, T, R> =
        JooqQueryComponents<Record, T, R>(
            makeCondition(spec.query),
            select = makeSelect(spec.projection),
            mapper = makeMapper(spec.projection),
            fetch = makeFetcher(spec.fetcher),
        )

    val mapCondition = mutableMapOf<KClass<*>, (Any) -> Condition>()

    inline fun <reified Q : Any> addQuery(toCondition: (Q) -> Condition) {
        mapCondition[Q::class] = { toCondition(it as Q) }
    }

    private fun <Q> makeCondition(query: Q): Condition =
        checkNotNull(mapCondition[query!!::class], {
            "Missing Condition Builder for $query"
        })(query)

    val mapSelect = mutableMapOf<Projection<*, *>, JooqQueryComponents.Selector<*>>()
    val mapMapper = mutableMapOf<Projection<*, *>, JooqQueryComponents.Mapper<*, *>>()

    fun <Q, T, X : Record> addProjection(
        projection: Projection<Q, T>,
        selector: JooqQueryComponents.Selector<X>,
        mapper: JooqQueryComponents.Mapper<X, T>,
    ) {
        mapSelect[projection] = selector
        mapMapper[projection] = mapper
    }

    private fun <Q, T> makeSelect(projection: Projection<Q, T>): JooqQueryComponents.Selector<Record> =
        checkNotNull(mapSelect[projection], { "Missing selector for $projection" }) as JooqQueryComponents.Selector<Record>

    private fun <Q, T> makeMapper(projection: Projection<Q, T>): JooqQueryComponents.Mapper<Record, T> =
        checkNotNull(
            mapMapper[projection],
            { "Missing mapper for projection $projection" },
        ) as JooqQueryComponents.Mapper<Record, T>

    val mapFetcher = mutableMapOf<KClass<*>, (Any) -> JooqQueryComponents.Fetcher<*, *, *>>()

    inline fun <T, R, reified M : Fetcher<T, R>> addFetcher(noinline fetcher: (M) -> JooqQueryComponents.Fetcher<Record, T, R>) {
        mapFetcher[M::class] = fetcher as (Any) -> JooqQueryComponents.Fetcher<*, *, *>
    }

    private fun <T, R> makeFetcher(fetcher: Fetcher<T, R>): JooqQueryComponents.Fetcher<Record, T, R> {
        val factory =
            checkNotNull(mapFetcher[fetcher::class], { "Missing Fetcher for $fetcher" })
                as (Fetcher<T, R>) -> JooqQueryComponents.Fetcher<Record, T, R>
        return factory(fetcher)
    }
}
