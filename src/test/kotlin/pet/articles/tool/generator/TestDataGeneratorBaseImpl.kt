package pet.articles.tool.generator

open class TestDataGeneratorBaseImpl<T>(
    protected val generate: () -> T,
    protected val create: (T) -> T,
    protected val toInvalidState: T.() -> T
) : TestDataGenerator<T> {

    override fun generateInvalidData(): T =
        toInvalidState(generateUnsavedData())

    override fun generateSavedData(dataSize: Int): List<T> =
        generateUnsavedData(dataSize).map(create)

    override fun generateUnsavedData(dataSize: Int): List<T> =
        (1..dataSize).map{ generate() }
}