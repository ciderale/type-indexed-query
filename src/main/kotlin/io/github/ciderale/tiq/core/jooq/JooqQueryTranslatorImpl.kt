package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Ordering
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
            sorter = makeSorter(spec.ordering),
            direction = spec.direction,
            mapper = makeMapper(spec.projection),
            fetch = makeFetcher(spec.fetcher),
        )

    private fun <Q> makeCondition(query: Q): Condition = checkCast<(Q) -> Condition>(mapCondition, query!!::class)(query)

    private fun <Q, T> makeSelect(projection: Projection<Q, T>): JooqQueryComponents.Selector<Record> =
        checkCast<JooqQueryComponents.Selector<Record>>(mapSelect, projection)

    private fun <Q> makeSorter(ordering: Ordering<Q>?): JooqQueryComponents.Sorter =
        if (ordering == null) {
            listOf()
        } else {
            checkCast(mapSorter, ordering)
        }

    private fun <Q, T> makeMapper(projection: Projection<Q, T>): JooqQueryComponents.Mapper<Record, T> =
        checkCast<JooqQueryComponents.Mapper<Record, T>>(mapMapper, projection)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> checkCast(
        map: MutableMap<*, *>,
        key: Any,
    ): T = checkNotNull(map[key]) { "Missing definition for $key" } as T

    val mapCondition = mutableMapOf<KClass<*>, (Any) -> Condition>()
    val mapSelect = mutableMapOf<Projection<*, *>, JooqQueryComponents.Selector<*>>()
    val mapMapper = mutableMapOf<Projection<*, *>, JooqQueryComponents.Mapper<*, *>>()
    val mapSorter = mutableMapOf<Ordering<*>, JooqQueryComponents.Sorter>()

    // ---------------- populating the lookup table  ------------------------
    inline fun <reified Q : Any> addQuery(noinline toCondition: (Q) -> Condition) {
        mapCondition[Q::class] = { toCondition(it as Q) }
    }

    fun <Q> addOrdering(
        ordering: Ordering<Q>,
        vararg sorter: JooqQueryComponents.SortFieldFactory,
    ) {
        mapSorter[ordering] = sorter.toList()
    }

    fun <Q, T, X : Record> addProjection(
        projection: Projection<Q, T>,
        selector: JooqQueryComponents.Selector<X>,
        mapper: JooqQueryComponents.Mapper<X, T>,
    ) {
        mapSelect[projection] = selector
        mapMapper[projection] = mapper
    }

    // typealias to make the lookup consistency more explicit
    typealias FetcherFactory<T, R, M> = (M) -> JooqQueryComponents.Fetcher<Record, T, R>
    typealias FetcherFactoryErased = FetcherFactory<*, *, *>

    val mapFetcher = mutableMapOf<KClass<*>, FetcherFactoryErased>()

    inline fun <T, R, reified M : Fetcher<T, R>> addFetcher(noinline fetcher: FetcherFactory<T, R, M>) {
        @Suppress("UNCHECKED_CAST") // addFetcher type signature verifies consistency
        mapFetcher[M::class] = fetcher as FetcherFactoryErased
    }

    private fun <T, R> makeFetcher(fetcher: Fetcher<T, R>): JooqQueryComponents.Fetcher<Record, T, R> =
        checkCast<FetcherFactory<T, R, Fetcher<T, R>>>(mapFetcher, fetcher::class)(fetcher)
}
