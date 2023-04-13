package builders.kong

import io.ktor.server.application.*
import builders.kong.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureHTTP()
    configureRouting()
}
