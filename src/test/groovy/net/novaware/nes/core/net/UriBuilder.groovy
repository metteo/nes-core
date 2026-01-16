package net.novaware.nes.core.net;

import net.novaware.nes.core.test.TestDataBuilder

class UriBuilder implements TestDataBuilder<URI> {

    private String uri

    static UriBuilder localFile(String fileName) {
        String userHome = System.getProperty("user.home")
        return new UriBuilder()
                .uri("file://" + userHome + File.separator + fileName)
    }

    static UriBuilder marioBros() {
        return localFile("Mario_Bros.nes")
    }

    UriBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    URI build() {
        return URI.create(uri);
    }
}
