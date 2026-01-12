package net.novaware.nes.core.test;

/**
 * @see <a href="http://www.natpryce.com/articles/000714.html">Test Data Builders: an alternative to the Object Mother pattern</a>
 * @param <T>
 */
interface TestDataBuilder<T> {

    T build();
}
