package io.github.ciderale.tiq.core

class SelectMappingRegistry private constructor(
    private val map: Map<Projection<*>, Any>,
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: Projection<T>): SelectMappingPair<T> = map.getValue(key) as SelectMappingPair<T>

    companion object {
        fun build(block: Builder.() -> Unit): SelectMappingRegistry {
            val builder = Builder()
            builder.block()
            return SelectMappingRegistry(builder.map.toMap())
        }
    }

    class Builder internal constructor() {
        internal val map = mutableMapOf<Projection<*>, Any>()

        infix fun <T> Projection<T>.put(value: SelectMappingPair<T>) {
            map[this] = value
        }
    }
}
