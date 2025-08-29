import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder(toBuilder = true)
@Accessors(fluent = true)
public class User {

    // Vallum
    String PROJECT_CLIENT_ID;
    String PROJECT_CLIENT_SECRET;

    public void clearVallumUser() {
        this.PROJECT_CLIENT_ID = null;
        this.PROJECT_CLIENT_SECRET = null;
    }
    
    // User id token (Used for storing scenarios for example)
    String userIdToken;

}