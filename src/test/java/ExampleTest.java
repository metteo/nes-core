import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ExampleTest {

    @Test
    @DisplayName("should work")
    void shouldWork() {

        assertThat(4, equalTo(2 + 2));
    }
}
