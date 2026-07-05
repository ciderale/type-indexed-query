package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.core.QuerySpec
import io.github.ciderale.tiq.core.ResultMode
import org.jooq.Condition
import org.jooq.Record
import kotlin.reflect.KClass

object JooqQueryTranslatorImpl : JooqQueryTranslator {
    override fun <Q, T, R> translate(spec: QuerySpec<Q, T, R>): JooqQueryComponents<Record, T, R> =
        JooqQueryComponents<Record, T, R>(
            makeCondition(spec.query),
            select = makeSelect(spec.projection),
            mapper = makeMapper(spec.projection),
            fetch = makeFetcher(spec.mode),
        )

    val mapCondition = mutableMapOf<KClass<*>, (Any) -> Condition>()

    inline fun <reified Q : Any> addQuery(toCondition: (Q) -> Condition) {
        mapCondition[Q::class] = { toCondition(it as Q) }
    }

    private fun <Q> makeCondition(query: Q): Condition =
        checkNotNull(mapCondition[query!!::class], {
            "Missing Condition Builder for $query"
        })(query)

    val mapSelect = mutableMapOf<Projection<*>, JooqQueryComponents.Selector<*>>()
    val mapMapper = mutableMapOf<Projection<*>, JooqQueryComponents.Mapper<*, *>>()

    fun <T, X : Record> addProjection(
        projection: Projection<T>,
        selector: JooqQueryComponents.Selector<X>,
        mapper: JooqQueryComponents.Mapper<X, T>,
    ) {
        mapSelect[projection] = selector
        mapMapper[projection] = mapper
    }

    private fun <T> makeSelect(projection: Projection<T>): JooqQueryComponents.Selector<Record> =
        checkNotNull(mapSelect[projection], { "Missing selector for $projection" }) as JooqQueryComponents.Selector<Record>

    private fun <T> makeMapper(projection: Projection<T>): JooqQueryComponents.Mapper<Record, T> =
        checkNotNull(
            mapMapper[projection],
            { "Missing mapper for projection $projection" },
        ) as JooqQueryComponents.Mapper<Record, T>

    val mapFetcher = mutableMapOf<KClass<*>, (Any) -> JooqQueryComponents.Fetcher<*, *, *>>()

    inline fun <T, R, reified M : ResultMode<T, R>> addFetcher(noinline fetcher: (M) -> JooqQueryComponents.Fetcher<Record, T, R>) {
        mapFetcher[M::class] = fetcher as (Any) -> JooqQueryComponents.Fetcher<*, *, *>
    }

    private fun <T, R> makeFetcher(mode: ResultMode<T, R>): JooqQueryComponents.Fetcher<Record, T, R> {
        val factory =
            checkNotNull(mapFetcher[mode::class], { "Missing Fetcher for $mode" })
                as (ResultMode<T, R>) -> JooqQueryComponents.Fetcher<Record, T, R>
        return factory(mode)
    }
}
