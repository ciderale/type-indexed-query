package io.github.ciderale.tik

import io.github.ciderale.tiq.core.ClassicFetcher
import io.github.ciderale.tiq.core.PagedList
import io.github.ciderale.tiq.sample.UserRepository
import io.github.ciderale.tiq.sample.db.JooqUserRepository
import io.github.ciderale.tiq.sample.domain.UserDetail
import io.github.ciderale.tiq.sample.domain.UserSummary
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test
import java.sql.DriverManager
import kotlin.test.assertEquals

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

        val oneUser: UserSummary =
            repo
                .fetch(
                    UserRepository.Query(id = "1"),
                    UserRepository.Summary,
                    ClassicFetcher.One(),
                ).also(::println)
        assertEquals("1", oneUser.id)

        val allDetails: List<UserDetail> =
            repo
                .fetch(
                    UserRepository.Query(),
                    UserRepository.Detail,
                    ClassicFetcher.Many(),
                ).also(::println)

        val pagedDetails: PagedList<UserDetail> =
            repo
                .fetch(
                    UserRepository.Query(),
                    UserRepository.Detail,
                    ClassicFetcher.Paged(1, 1),
                ).also(::println)
        assertEquals(3, pagedDetails.total)

        val countActive: Int =
            repo
                .fetch(
                    UserRepository.Query(activeOnly = true),
                    UserRepository.Summary,
                    ClassicFetcher.Count(),
                ).also(::println)
    }
}
