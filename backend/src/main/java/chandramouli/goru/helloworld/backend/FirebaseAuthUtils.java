package chandramouli.goru.helloworld.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FirebaseAuthUtils {

    public FirebaseAuthUtils() {
        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setServiceAccount(new FileInputStream("HelloWorld-21d28fd813ed.json"))
                    .setDatabaseUrl("https://helloworld-146218.firebaseio.com/")
                    .build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FirebaseApp.initializeApp(options);
    }
}
