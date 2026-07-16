package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Ordering
import io.github.ciderale.tiq.core.OrderingDirection
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.core.QuerySpec
import org.jooq.Condition
import org.jooq.Record
import org.jooq.SortField
import kotlin.reflect.KClass

object JooqQueryTranslatorImpl : JooqQueryTranslator {
    override fun <Q, T, R> translate(spec: QuerySpec<Q, T, R>): JooqQueryComponents<Record, T, R> {
        val (selector, mapper) = makeSelectMappingPair(spec.projection)
        return JooqQueryComponents<Record, T, R>(
            makeCondition(spec.query),
            select = selector,
            ordering =
                spec.ordering?.let {
                    makeOrdering(spec.ordering).map { sortField -> sortField(spec.direction) }
                } ?: listOf(),
            mapper = mapper,
            fetch = makeFetcher(spec.fetcher),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> checkCast(
        map: MutableMap<*, *>,
        key: Any,
    ): T = checkNotNull(map[key]) { "Missing definition for $key" } as T

    // ################### Query => Condition ########################################
    typealias ConditionFactory<Q> = (Q) -> Condition
    typealias ConditionFactoryErased = ConditionFactory<*>

    val conditionFactoryRegistry = mutableMapOf<KClass<*>, ConditionFactoryErased>()

    inline fun <reified Q : Any> addQuery(noinline toCondition: ConditionFactory<Q>) {
        conditionFactoryRegistry[Q::class] = toCondition
    }

    private fun <Q> makeCondition(query: Q): Condition = checkCast<ConditionFactory<Q>>(conditionFactoryRegistry, query!!::class)(query)

    // ################### Ordering/Sorting ########################################
    typealias SortFieldFactory = (OrderingDirection) -> SortField<*>

    val orderingRegistry = mutableMapOf<KClass<out Ordering<*>>, List<SortFieldFactory>>()

    private fun <Q> makeOrdering(ordering: Ordering<Q>): List<SortFieldFactory> = checkCast(orderingRegistry, ordering::class)

    fun <Q> addOrdering(
        ordering: Ordering<Q>,
        vararg sorter: SortFieldFactory,
    ) {
        orderingRegistry[ordering::class] = sorter.toList()
    }

    // ################### SelectMapping #########################################

    data class SelectMappingPair<X : Record, T>(
        val selector: JooqQueryComponents.Selector<X>,
        val mapper: JooqQueryComponents.Mapper<X, T>,
    )

    val mapSelectMapping = mutableMapOf<KClass<out Projection<*, *>>, SelectMappingPair<*, *>>()

    fun <Q, T, X : Record> addProjection(
        projection: Projection<Q, T>,
        selector: JooqQueryComponents.Selector<X>,
        mapper: JooqQueryComponents.Mapper<X, T>,
    ) {
        mapSelectMapping[projection::class] = SelectMappingPair(selector, mapper)
    }

    private fun <Q, T> makeSelectMappingPair(projection: Projection<Q, T>) =
        checkCast<SelectMappingPair<Record, T>>(mapSelectMapping, projection::class)

    // ################### Fetcher ###########################################################
    // typealias to make the lookup consistency more explicit
    typealias FetcherFactory<T, R, M> = (M) -> JooqQueryComponents.Fetcher<Record, T, R>
    typealias FetcherFactoryErased = FetcherFactory<*, *, *>

    val mapFetcher = mutableMapOf<KClass<out Fetcher<*, *>>, FetcherFactoryErased>()

    inline fun <T, R, reified M : Fetcher<T, R>> addFetcher(noinline fetcher: FetcherFactory<T, R, M>) {
        @Suppress("UNCHECKED_CAST") // addFetcher type signature verifies consistency
        mapFetcher[M::class] = fetcher as FetcherFactoryErased
    }

    private fun <T, R, M : Fetcher<T, R>> makeFetcher(fetcher: M): JooqQueryComponents.Fetcher<Record, T, R> =
        checkCast<FetcherFactory<T, R, M>>(mapFetcher, fetcher::class)(fetcher)
}
