package builders.kong.plugins

import builders.kong.util.logger
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.microseconds

fun Application.configureRouting() {
    val log = logger<Application>()

    routing {
        get("/") {

            log.info("Client Headers {}", call.request.headers.toMap())

            log.info("\uD83D\uDC68\u200D\uD83C\uDFED gone meeting...")
            delay(250L.microseconds)

            call.respondText("✅ done...")
        }
    }
}

