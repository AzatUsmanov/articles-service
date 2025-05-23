package pet.articles

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan
class ArticlesServiceApplication

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

