package pet.articles.test.tool.generator

interface TestDataGenerator<T> {

    companion object {
        private const val SIZE_OF_ONE_ELEMENT_LIST = 1
    }

    fun generateInvalidData(): T

    fun generateSavedData(dataSize: Int): List<T>

    fun generateUnsavedData(dataSize: Int): List<T>

    fun generateSavedData(): T = generateSavedData(SIZE_OF_ONE_ELEMENT_LIST).first()

    fun generateUnsavedData(): T = generateUnsavedData(SIZE_OF_ONE_ELEMENT_LIST).first()
}