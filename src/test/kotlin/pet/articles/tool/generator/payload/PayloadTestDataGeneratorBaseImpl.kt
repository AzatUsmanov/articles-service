package pet.articles.tool.generator.payload

import pet.articles.tool.generator.TestDataGenerator

open class PayloadTestDataGeneratorBaseImpl<T, M>(
    protected val convertToPayload: (M) -> T,
    protected val modelTestDataGenerator: TestDataGenerator<M>
) : TestDataGenerator<T> {

    override fun generateInvalidData(): T =
        convertToPayload(modelTestDataGenerator.generateInvalidData())

    override fun generateSavedData(dataSize: Int): List<T> =
        modelTestDataGenerator
            .generateUnsavedData(dataSize)
            .map(convertToPayload)

    override fun generateUnsavedData(dataSize: Int): List<T> =
        modelTestDataGenerator
            .generateUnsavedData(dataSize)
            .map(convertToPayload)
}