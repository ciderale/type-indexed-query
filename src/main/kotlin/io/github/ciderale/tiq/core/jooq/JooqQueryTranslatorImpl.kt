package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Ordering
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.core.QuerySpec
import org.jooq.Condition
import org.jooq.Record
import org.jooq.impl.DSL
import kotlin.reflect.KClass

object JooqQueryTranslatorImpl : JooqQueryTranslator {
    override fun <Q, T, R> translate(spec: QuerySpec<Q, T, R>): JooqQueryComponents<Record, T, R> =
        JooqQueryComponents<Record, T, R>(
            makeCondition(spec.query),
            select = makeSelect(spec.projection),
            sorter = makeSorter(spec.ordering),
            mapper = makeMapper(spec.projection),
            fetch = makeFetcher(spec.fetcher),
        )

    private fun <Q> makeCondition(query: Q): Condition = checkCast<(Q) -> Condition>(mapCondition, query!!::class)(query)

    private fun <Q, T> makeSelect(projection: Projection<Q, T>): JooqQueryComponents.Selector<Record> =
        checkCast<JooqQueryComponents.Selector<Record>>(mapSelect, projection)

    private fun <Q> makeSorter(ordering: Ordering<Q>?): JooqQueryComponents.Sorter<Record> =
        if (ordering == null) {
            { it.orderBy(DSL.noField()) }
        } else {
            checkCast<JooqQueryComponents.Sorter<Record>>(mapSorter, ordering)
        }

    private fun <Q, T> makeMapper(projection: Projection<Q, T>): JooqQueryComponents.Mapper<Record, T> =
        checkCast<JooqQueryComponents.Mapper<Record, T>>(mapMapper, projection)

    private fun <T, R> makeFetcher(fetcher: Fetcher<T, R>): JooqQueryComponents.Fetcher<Record, T, R> {
        val factory =
            checkCast<(Fetcher<T, R>) -> JooqQueryComponents.Fetcher<Record, T, R>>(mapFetcher, fetcher::class)
        return factory(fetcher)
    }

    private fun <T : Any> checkCast(
        map: MutableMap<*, *>,
        key: Any,
    ): T = checkNotNull(map[key]) { "Missing definition for $key" } as T

    val mapCondition = mutableMapOf<KClass<*>, (Any) -> Condition>()
    val mapSelect = mutableMapOf<Projection<*, *>, JooqQueryComponents.Selector<*>>()
    val mapMapper = mutableMapOf<Projection<*, *>, JooqQueryComponents.Mapper<*, *>>()
    val mapSorter = mutableMapOf<Ordering<*>, JooqQueryComponents.Sorter<*>>()
    val mapFetcher = mutableMapOf<KClass<*>, (Any) -> JooqQueryComponents.Fetcher<*, *, *>>()

    // ---------------- populating the lookup table  ------------------------
    inline fun <reified Q : Any> addQuery(noinline toCondition: (Q) -> Condition) {
        mapCondition[Q::class] = { toCondition(it as Q) }
    }

    fun <Q> addOrdering(
        ordering: Ordering<Q>,
        sorter: JooqQueryComponents.Sorter<*>,
    ) {
        mapSorter[ordering] = sorter
    }

    fun <Q, T, X : Record> addProjection(
        projection: Projection<Q, T>,
        selector: JooqQueryComponents.Selector<X>,
        mapper: JooqQueryComponents.Mapper<X, T>,
    ) {
        mapSelect[projection] = selector
        mapMapper[projection] = mapper
    }

    inline fun <T, R, reified M : Fetcher<T, R>> addFetcher(noinline fetcher: (M) -> JooqQueryComponents.Fetcher<Record, T, R>) {
        mapFetcher[M::class] = fetcher as (Any) -> JooqQueryComponents.Fetcher<*, *, *>
    }
}
