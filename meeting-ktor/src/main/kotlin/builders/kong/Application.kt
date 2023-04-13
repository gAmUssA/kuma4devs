package builders.kong

import builders.kong.plugins.configureHTTP
import builders.kong.plugins.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>): Unit =
    io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureHTTP()
    configureRouting()
}
