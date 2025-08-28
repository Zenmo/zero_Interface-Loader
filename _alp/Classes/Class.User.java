import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder(toBuilder = true)
@Accessors(fluent = true)
public class User {

    // Vallum
    String PROJECT_CLIENT_ID;
    String PROJECT_CLIENT_SECRET;

    public User clearVallumUser() {
        return this.toBuilder()
                   .PROJECT_CLIENT_ID(null)
                   .PROJECT_CLIENT_SECRET(null)
                   .build();
    }
    
    // User id token (Used for storing scenarios for example)
    String userIdToken;

}