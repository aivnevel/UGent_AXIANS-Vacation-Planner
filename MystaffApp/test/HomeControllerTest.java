import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;

public class HomeControllerTest extends WithApplication {

    @Test
    public void myTest2() {
        assert(false);
    }

}
