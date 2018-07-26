import java.util.HashMap;

public class Global {
    private static final Global ourInstance = new Global();

    private String hostURL;
    private String phoneNumber;
    private String publicKey;
    private String privateKey;
    private String userName;
    private HashMap<String, String> contactMap;


    public static Global getInstance() {
        return ourInstance;
    }

    private Global() {
    }
}
