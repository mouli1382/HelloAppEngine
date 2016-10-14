package chandramouli.goru.helloworld;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String phoneNumber;

    public User(String username, String email, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
}
