package io.github.ciderale.tik

import io.github.ciderale.tiq.core.Mode
import io.github.ciderale.tiq.core.of
import io.github.ciderale.tiq.sample.UserRepository
import io.github.ciderale.tiq.sample.db.JooqUserRepository
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test
import java.sql.DriverManager

class JooqTest {
    private fun dsl(): DSLContext {
        val conn =
            DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/appdb",
                "app",
                "app",
            )

        return DSL.using(conn, SQLDialect.POSTGRES)
    }

    @Test
    fun testSelectUsers() {
        val ctx = dsl()
        val repo = JooqUserRepository(ctx)

        repo
            .fetch(
                UserRepository.Query(id = "1"),
                Mode.One.of(UserRepository.Summary),
            ).also(::println)

        repo
            .fetch(
                UserRepository.Query(),
                Mode.Many.of(UserRepository.Detail),
            ).also(::println)

        repo
            .fetch(
                UserRepository.Query(),
                Mode.Paged(1, 1).of(UserRepository.Detail),
            ).also(::println)

        repo
            .fetch(
                UserRepository.Query(activeOnly = true),
                Mode.Count.of(UserRepository.Summary),
            ).also(::println)
    }
}
