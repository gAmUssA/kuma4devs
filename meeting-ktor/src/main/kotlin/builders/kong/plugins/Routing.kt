package builders.kong.plugins

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

fun Application.configureRouting() {
    val log = KotlinLogging.logger {}

    routing {
        get("/meet") {

            log.info { "Client Headers ${call.request.headers.toMap()}" }

            log.info { "\uD83D\uDC68\u200D\uD83C\uDFED gone meeting..." }

            delay(250L.milliseconds)

            call.respondText("✅ done...")
            
        }
    }
}

