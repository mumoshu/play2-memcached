package test;

import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class JavaSampleTest {

    @Test
    public void cachingIntValue() {
        Map<String, String> additionalConfiguration = new HashMap<String, String>();
        additionalConfiguration.put("ehcacheplugin", "disabled");
        additionalConfiguration.put("memcached.host", "127.0.0.1:11211");
        running(fakeApplication(additionalConfiguration), new Runnable() {
            public void run() {
                Result result = routeAndCall(fakeRequest(GET, "/cacheInt"));
                assertThat(contentAsString(result)).isEqualTo("123 java.lang.Integer");
            }
        });
    }
}
