package builders.kong.plugins

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

inline fun <reified T> logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

fun Application.configureRouting() {
    var log = logger<Application>()

    routing {
        val meetServerUrl = environment!!.config.property("ktor.meet.server.url").getString()
        log.info("💉 Got meeting server url from environment: {}", meetServerUrl)

        get("/work") {
            val client = HttpClient(CIO)
            val start = currentTimeMillis()
            var count = 0
            for (i in (1..4)) {
                var response = client.request("$meetServerUrl/meet") {
                    method = HttpMethod.Get
                    headers {
                        append(HttpHeaders.UserAgent, "Ktor Client 🤘")
                    }
                }
                if (response.status.isSuccess()) {
                    log.info("\uD83D\uDEB6 Going to meeting: {}", i)
                    count++
                }
            }
            val end = currentTimeMillis()
            var message = "\uD83D\uDCC6 worked for ${end - start} ms, and went to $count meetings"
            call.respond(HttpStatusCode.OK, message)
            client.close()
        }
        get("/") {
            call.respond("RUOK")
        }
    }
}
