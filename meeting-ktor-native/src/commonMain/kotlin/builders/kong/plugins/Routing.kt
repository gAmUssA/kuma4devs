package builders.kong.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

fun Application.configureRouting() {

    routing {
        get("/meet") {

            call.application.log.info("Client Headers ${call.request.headers.toMap()}")

            call.application.log.info("\uD83D\uDC68\u200D\uD83C\uDFED gone meeting...")

            //Thread.sleep but in Kotlin
            delay(250L.milliseconds)

            call.response.header(HttpHeaders.Server, "Ktor/2.3.4")
            call.respondText("✅ done...")

        }
    }
}

