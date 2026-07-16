package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Ordering
import io.github.ciderale.tiq.core.OrderingDirection
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.core.QuerySpec
import io.github.ciderale.tiq.core.TypeIndexedFactoryRegistry
import org.jooq.Condition
import org.jooq.Record
import org.jooq.SortField

/** JooqQueryTranslatorImpl translates a QuerySpec to concrete JooqQueryComponents
 * and is responsible to ensure the type-index consistencies of Q, T, R.
 */
object JooqQueryTranslatorImpl : JooqQueryTranslator {
    override fun <Q : Any, T, R> translate(spec: QuerySpec<Q, T, R>): JooqQueryComponents<Record, T, R> {
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

    // ################### Query => Condition ########################################
    val conditionRegistry = TypeIndexedFactoryRegistry<Any>()

    inline fun <reified Q : Any> addQuery(noinline factory: (Q) -> Condition) {
        conditionRegistry.add(factory)
    }

    private fun <Q : Any> makeCondition(query: Q): Condition = conditionRegistry.make(query)

    // ################### Ordering<Q> => List<SortFieldFactory> ########################################
    typealias SortFieldFactory = (OrderingDirection) -> SortField<*>

    val orderingRegistry = TypeIndexedFactoryRegistry<Ordering<*>>()

    inline fun <Q, reified O : Ordering<Q>> addOrdering(
        ordering: O,
        vararg sorter: SortFieldFactory,
    ) {
        orderingRegistry.add<O, List<SortFieldFactory>> { sorter.toList() }
    }

    private fun <Q> makeOrdering(ordering: Ordering<Q>): List<SortFieldFactory> = orderingRegistry.make(ordering)

    // ################### Projection<Q,T> => SelectMappingPair<*,T> #########################################
    data class SelectMappingPair<X : Record, T>(
        val selector: JooqQueryComponents.Selector<X>,
        val mapper: JooqQueryComponents.Mapper<X, T>,
    )

    val selectMappingRegistry = TypeIndexedFactoryRegistry<Projection<*, *>>()

    inline fun <Q, T, X : Record, reified P : Projection<Q, T>> addProjection(
        projection: P,
        noinline selector: JooqQueryComponents.Selector<X>,
        mapper: JooqQueryComponents.Mapper<X, T>,
    ) {
        selectMappingRegistry.add<P, SelectMappingPair<X, T>> { SelectMappingPair(selector, mapper) }
    }

    private fun <Q, T> makeSelectMappingPair(projection: Projection<Q, T>): SelectMappingPair<Record, T> =
        selectMappingRegistry.make(projection)

    // ################### Fetcher<T,R> => JooqQueyrComponents.Fetcher<*, T,R> ##############################
    val fetcherRegistry = TypeIndexedFactoryRegistry<Fetcher<*, *>>()

    inline fun <T, R, reified M : Fetcher<T, R>> addFetcher(noinline factory: (M) -> JooqQueryComponents.Fetcher<Record, T, R>) {
        fetcherRegistry.add(factory)
    }

    private fun <T, R, M : Fetcher<T, R>> makeFetcher(fetcher: M): JooqQueryComponents.Fetcher<Record, T, R> = fetcherRegistry.make(fetcher)
}
