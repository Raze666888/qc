import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class generate_md5 {
    public static void main(String[] args) {
        String password = "aa111111";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            System.out.println("Password: " + password);
            System.out.println("MD5: " + hexString.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
