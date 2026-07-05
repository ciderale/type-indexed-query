package io.github.ciderale.tiq

import io.github.ciderale.tiq.core.ClassicFetcher
import io.github.ciderale.tiq.core.OrderingDirection
import io.github.ciderale.tiq.core.PagedList
import io.github.ciderale.tiq.sample.UserRepository
import io.github.ciderale.tiq.sample.db.JooqUserRepository
import io.github.ciderale.tiq.sample.domain.UserDetail
import io.github.ciderale.tiq.sample.domain.UserSummary
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertEquals

class JooqTest {
    lateinit var connection: Connection

    @BeforeEach
    fun setup() {
        connection =
            DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/appdb",
                "app",
                "app",
            )
    }

    @AfterEach
    fun teardown() {
        connection.close()
    }

    @Test
    fun testSelectUsers() {
        val ctx = DSL.using(connection, SQLDialect.POSTGRES)
        val repo = JooqUserRepository(ctx)

        val id = "1"
        val oneUser: UserSummary = repo.get(id, UserRepository.Summary)
        assertEquals(id, oneUser.id)

        val allDetails: List<UserDetail> =
            repo
                .fetch(
                    UserRepository.Query(),
                    UserRepository.Detail,
                    fetcher = ClassicFetcher.Many(),
                    ordering = UserRepository.OrderBy.NAME,
                    direction = OrderingDirection.DESC,
                ).also(::println)
        assertEquals(
            allDetails.reversed(),
            repo
                .fetch(
                    UserRepository.Query(),
                    UserRepository.Detail,
                    fetcher = ClassicFetcher.Many(),
                    ordering = UserRepository.OrderBy.NAME,
                    direction = OrderingDirection.ASC,
                ).also(::println),
        )

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

        val activeUser =
            repo
                .fetch(
                    UserRepository.Query(activeOnly = true),
                    UserRepository.Summary,
                    ClassicFetcher.Many(),
                )
        assertEquals(countActive, activeUser.size)
    }
}
