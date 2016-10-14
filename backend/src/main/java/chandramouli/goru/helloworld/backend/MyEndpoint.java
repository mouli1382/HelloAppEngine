/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package chandramouli.goru.helloworld.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.helloworld.goru.chandramouli",
                ownerName = "backend.helloworld.goru.chandramouli",
                packagePath = ""
        )
)
public class MyEndpoint {
    private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());

    public MyEndpoint() {
        log.info("Starting ....");

        //Initialize firebase here.
        new FirebaseAuthUtils();
    }

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) {
        MyBean response = new MyBean();
        response.setData("Hi, " + name);

        return response;
    }

    @ApiMethod(name = "createCustomToken")
    public MyBean createCustomToken(@Named("uid") String uid, @Named("phoneNumber") String phoneNumber) {
        HashMap<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("phoneNumber", phoneNumber);
        String customToken = FirebaseAuth.getInstance().createCustomToken(uid, additionalClaims);
        MyBean response = new MyBean();
        response.setData(customToken);

        return response;
    }


}
