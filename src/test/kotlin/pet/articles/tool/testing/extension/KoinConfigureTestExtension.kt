package pet.articles.tool.testing.extension

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

import pet.articles.config.testConfigure


class KoinConfigureTestExtension : BeforeAllCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        startKoin {
            testConfigure()
        }
    }

    override fun afterAll(context: ExtensionContext) {
        stopKoin()
    }
}
