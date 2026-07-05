package io.github.ciderale.tiq.sample.db

import org.jooq.Record
import org.jooq.TableField
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl

object USER : TableImpl<Record>(DSL.name("user")) {
    val ID: TableField<Record, String> =
        createField(DSL.name("id"), SQLDataType.VARCHAR)

    val NAME: TableField<Record, String> =
        createField(DSL.name("name"), SQLDataType.VARCHAR)

    val EMAIL: TableField<Record, String> =
        createField(DSL.name("email"), SQLDataType.VARCHAR)

    val ACTIVE: TableField<Record, Boolean> =
        createField(DSL.name("active"), SQLDataType.BOOLEAN)

    override fun getSchema() = null

    override fun getPrimaryKey() = null
}
