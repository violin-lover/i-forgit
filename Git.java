import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Git {
    public static void main(String[] args) throws IOException {
        Git.init();
        System.out.println(Git.hashFile("test.txt"));
    }

    public static void init () {
        File gitDir = new File("./git/");
        if (!gitDir.mkdir()) {
            System.out.println("Git Repository Already Exists");
        }

        File objDir = new File("./git/objects/");
        File index = new File("./git/index");
        File head = new File ("./git/HEAD");

        try {
            if (!objDir.mkdir() || !index.createNewFile() || !head.createNewFile()) {
                System.out.println("Git Repository Already Exists");
                return;
            }
        } catch (IOException e) {
            System.out.println("Failed to initialize Git repo");
            return;
        }

        System.out.println("Git Repository Created");
    }

    public static String hashFile(String filePath) throws IOException {
        StringBuilder stuffInFile = new StringBuilder();
        StringBuilder hexString = new StringBuilder();

        // gets stuffInFile
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                stuffInFile.append(line);
            }
        }
        
        // turn stuffInFile to bytes
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(stuffInFile.toString().getBytes(StandardCharsets.UTF_8));

            // bytes to hash
            for (byte b : bytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException: file couldn't be hashed :(");
        }

        return hexString.toString();

    }
}