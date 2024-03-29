package builders.kong

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import builders.kong.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/meet").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("✅ done...", bodyAsText())
        }
    }
}
