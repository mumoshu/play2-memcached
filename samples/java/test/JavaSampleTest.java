package test;

import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import java.util.Map;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class JavaSampleTest {

    @Test
    public void cachingIntValue() {
        Map<String, String> additionalConfiguration = inMemoryDatabase();
        additionalConfiguration.put("ehcacheplugin", "disabled");
        running(fakeApplication(additionalConfiguration), new Runnable() {
            public void run() {
                Result result = routeAndCall(fakeRequest(GET, "/cache/set"));
                assertThat(contentAsString(result)).isEqualTo("123: java.lang.Integer");
            }
        });
    }
}