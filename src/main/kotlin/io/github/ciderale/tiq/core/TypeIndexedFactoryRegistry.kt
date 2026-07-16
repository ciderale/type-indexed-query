package io.github.ciderale.tiq.core

import kotlin.reflect.KClass

/**
 * The TypedIndexedFactoryRegistry allows to create an instance `T` for
 * a provided value `M`. The specialty is that depending on the concrete
 * subtype of M a different subtype of T is generated.
 *
 * Unfortunately, the type-system is not strong enough to fully ensure the
 * type-level dependency of this generic factory function and a cast is needed.
 * By design, the factory function registered for a given key is applicable
 * to a value `spec` provided to `make(spec)`. However, the consistency of
 * the return value `T` of `make(spec): T` cannot be guaranteed by this class
 * and must be ensured by the caller to avoid runtime inconsistencies.
 *
 * Specifically, the caller must ensure that the (requested) return type `T`
 * of `make(M): T` is consistent with the factory function registered using
 * `add(factory: (M) -> T)`. The goal of this class is to reduce the boilerplate
 * at the caller. That should make the (manual) verification of the return
 * value type consistency more obvious.
 */
class TypeIndexedFactoryRegistry<M : Any> {
    val map: MutableMap<KClass<out M>, (M) -> Any> = mutableMapOf()

    inline fun <reified MR : M, T> add(noinline factory: (MR) -> T) {
        @Suppress("UNCHECKED_CAST")
        map[MR::class] = factory as (M) -> Any
    }

    fun <T> make(spec: M): T = checkCast<(M) -> T>(spec::class)(spec)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> checkCast(key: KClass<out M>): T = checkNotNull(map[key]) { "Missing definition for $key" } as T
}
