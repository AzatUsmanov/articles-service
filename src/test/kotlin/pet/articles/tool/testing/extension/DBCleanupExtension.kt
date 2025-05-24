package pet.articles.tool.testing.extension

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

import org.koin.test.KoinTest

import pet.articles.tool.db.DBCleaner

class DBCleanupExtension : AfterEachCallback {
    override fun afterEach(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance
        if (testInstance is KoinTest) {
            testInstance
                .getKoin()
                .get<DBCleaner>()
                .cleanUp()
        }
    }
}