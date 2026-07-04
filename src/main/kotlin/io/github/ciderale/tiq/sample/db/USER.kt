package io.github.ciderale.tiq.sample.db

import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

object USER : TableImpl<Record>(DSL.name("user")) {
    val ID = field(DSL.name("id"), String::class.java)
    val NAME = field(DSL.name("name"), String::class.java)
    val EMAIL = field(DSL.name("email"), String::class.java)
    val ACTIVE = field(DSL.name("active"), String::class.java)

    override fun getSchema() = null

    override fun getPrimaryKey() = null
}
