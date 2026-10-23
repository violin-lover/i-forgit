import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.util.*;

public class Git {
    public static void main(String[] args) throws IOException {
        // initialize repo
        // Git.init();

        // // create two files
        // Files.writeString(Paths.get("test.txt"), "original");
        // Files.createDirectories(Paths.get("testFolder"));
        // Files.writeString(Paths.get("testFolder/test2.txt"), "hello");

        // // add files to index
        // Git.createBlob("test.txt");
        // Git.updateIndex("test.txt");

        // Git.createBlob("testFolder/test2.txt");
        // Git.updateIndex("testFolder/test2.txt");

        // // commit A
        // String commitA = printCommit("author", "1st commit");
        // System.out.println("\n\nCommit A: " + commitA);

        // // change one file then stage again
        // Files.writeString(Paths.get("test.txt"), "changed");
        // Git.createBlob("test.txt");
        // Git.updateIndex("test.txt");

        // // commit B
        // String commitB = printCommit("author", "2nd commit");
        // System.out.println("\n\nCommit B: " + commitB);

        // // sout parent (it should be commit A)
        // System.out.println(Files.readString(Paths.get("./git/objects/" + commitB)));

        // // checkout
        // checkout(commitA);
        // System.out.println(Files.readString(Paths.get("testFolder/test2.txt")));
    }

    public static void init() {
        File gitDir = new File("./git/");
        if (!gitDir.mkdir()) {
            System.out.println("Git Repository Already Exists");
        }

        File objDir = new File("./git/objects/");
        File branchesDir = new File("./git/refs/heads/");
        File index = new File("./git/index");
        File head = new File("./git/HEAD");

        try {
            objDir.mkdirs();
            branchesDir.mkdirs();
            index.createNewFile();

            if (head.createNewFile()) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(head));
                bw.write("ref: refs/heads/main");
                bw.close();
            }

        } catch (IOException e) {
            System.out.println("couldn't to initialize Git repo");
            return;
        }

        System.out.println("Git Repository Created");

    }

    public static String hashFile(String filePath) throws IOException {
        StringBuilder stuffInFile = new StringBuilder();

        // gets stuffInFile
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                stuffInFile.append(line);
            }
        }

        // turn stuffInFile to bytes

        return hashString(stuffInFile.toString());

    }

    private static String hashString(String input) {
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.toString().getBytes(StandardCharsets.UTF_8));

            // bytes to hash
            for (byte b : bytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException: file couldn't be hashed :(");
        }
        return null;
    }

    public static void createBlob(String filePath) throws IOException {
        // get hash of the file
        String hashedFile = Git.hashFile(filePath);

        // write original file content into the obj
        Path sourcePath = Paths.get(filePath);
        Path destinationPath = Paths.get("./git/objects/" + hashedFile);

        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully");
        }

        catch (IOException e) {
            System.out.println("Error during file copying: " + e);
        }

    }

    public static void updateIndex(String filePath) throws IOException {
        String hashedFile = Git.hashFile(filePath);
        File index = new File("./git/index");
        String root = new File("..").getAbsoluteFile().getParentFile().getName();

        // getting path of filePath
        Path path = Paths.get(filePath);
        String relativePath = path.toString();
        String entry = hashedFile + " " + root + "/" + relativePath;

        // checking if needs newLine or index is empty
        ArrayList<String> lines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = new BufferedReader(new FileReader(index))) {
            String line = br.readLine();

            // reading the existing index file (going line by line)
            while (line != null) {
                // add same filePath, but with diff hash
                if (samePath(line, root + "/" + relativePath)) {
                    lines.add(entry);
                    updated = true;
                }

                // add existing line (different filePath)
                else {
                    lines.add(line);
                }
                line = br.readLine();
            }
        }

        // if filePath doesn't exist yet
        if (!updated) {
            lines.add(entry);
        }

        // rewrite index with the updated stuff
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(index))) {
            int count = 0;
            for (String line : lines) {
                bw.write(line);

                // making sure no extra new line at the end
                if (count < lines.size() - 1) {
                    bw.newLine();
                }

                count++;
            }
        }

        catch (Exception e) {
            System.out.println("couldn't to update ./git/index: " + e);
        }

    }

    // helper method to check whether a line in index file has the same file path
    public static boolean samePath(String line, String filePath) {
        int spaceInd = line.indexOf(" ");

        if (spaceInd == -1) {
            return false;
        }

        String existingPath = line.substring(spaceInd + 1);
        return existingPath.equals(filePath);
    }

    public static String createTree(ArrayList<String> workingList, String dirPath)
            throws IOException {
        StringBuilder treeContents = new StringBuilder();
        boolean firstLine = true;

        for (String line : workingList) {
            String[] parts = line.split(" ", 3);

            String type = parts[0];
            String hash = parts[1];
            String path = parts[2];

            int lastSlash = path.lastIndexOf("/");

            if (lastSlash != -1) {
                String folder = path.substring(0, lastSlash);

                if (folder.equals(dirPath)) {
                    String name = path.substring(lastSlash + 1);

                    if (!firstLine) {
                        treeContents.append("\n");
                    }

                    treeContents.append(type + " " + hash + " " + name);
                    firstLine = false;
                }
            }
        }
        String contents = treeContents.toString();

        String hashed = hashString(contents);

        File treeFile = new File("git/objects", hashed);

        try {
            treeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(treeFile))) {
            writer.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hashed;
    }

    public static String treeIndex() {
        try {
            File indexFile = new File("./git/index");
            StringBuilder fileString = new StringBuilder();
            BufferedReader brFile = new BufferedReader(new FileReader(indexFile));
            String line = "";
            while ((line = brFile.readLine()) != null) {
                fileString.append(line);
                fileString.append('\n');
            }
            brFile.close();
            ArrayList<String> indexArray = new ArrayList<>(
                    Arrays.asList(fileString.substring(0, fileString.length() - 1).split("\n")));
            for (int i = 0; i < indexArray.size(); i++) {
                indexArray.set(i, "blob " + indexArray.get(i));
            }
            // String index = "";
            String deepPath = "/";
            String root = new File("..").getAbsoluteFile().getParentFile().getName();

            while (!deepPath.equals(root) && deepPath.length() != 0) {
                deepPath = "";
                for (String string : indexArray) {
                    if ((deepPath.length() - deepPath.replace("/", "").length()) < (string.length()
                            - string.replace("/", "").length())) {
                        deepPath = new File(string.substring(string.indexOf(' ', string.indexOf(' ') + 1) + 1))
                                .getParent();
                    }
                }
                ArrayList<Integer> remove = new ArrayList<>();
                for (int i = 0; i < indexArray.size(); i++) {
                    if (indexArray.get(i).contains(deepPath)) {
                        remove.add(i);
                    }
                }
                indexArray.add("tree " + createTree(indexArray, deepPath) + " " + deepPath);
                for (int i = 0; i < remove.size(); i++) {
                    indexArray.remove((int) remove.get(i) - i);
                }
            }
            String rootHash = hashString(indexArray.get(0));
            FileWriter fw = new FileWriter("./git/objects/" + rootHash);
            fw.write(indexArray.get(0));
            fw.close();
            createTree(indexArray, deepPath);
            return rootHash;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String printCommit(String author, String message) {
        String tree = treeIndex();
        String parent = "";
        String date = LocalDateTime.now().toString();
        StringBuilder print = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader("./git/HEAD"));) {
            String head = br.readLine();
            String branch = null;

            if (head != null && head.startsWith("ref: ")) {
                branch = head.substring(5);

                File branchFile = new File("./git/" + branch);

                if (branchFile.exists()) {
                    BufferedReader branchReader = new BufferedReader(new FileReader(branchFile));
                    parent = branchReader.readLine();
                    branchReader.close();
                } else {
                    parent = null;
                }

            } else {
                parent = head;
            }

            // writing fileContents for the commit file
            print.append("tree: ").append(tree).append("\n");
            if (parent != null)
                print.append("parent: ").append(parent).append("\n");
            print.append("author: " + author + "\n");
            print.append("date: " + date + "\n");
            print.append("message: " + message);

            // update HEAD
            String commitHash = hashString(print.toString());
            BufferedWriter bw = new BufferedWriter(new FileWriter("./git/objects/" + commitHash));
            bw.write(print.toString());
            bw.close();

            // put commit file into git/objects
            bw.write(print.toString());
            bw.close();

            if (branch != null) {
                bw = new BufferedWriter(new FileWriter("./git/" + branch));
            } else {
                bw = new BufferedWriter(new FileWriter("./git/HEAD"));
            }

            bw.write(commitHash);
            bw.close();

            return commitHash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String rebuildIndex(String commit) {
        try {
            // BufferedReader headReader = new BufferedReader(new FileReader("./git/HEAD"));
            // String head = headReader.readLine();
            // headReader.close();

            BufferedReader treeReader = new BufferedReader(new FileReader("./git/objects/" + commit));
            String tree = treeReader.readLine();
            tree = tree.substring(tree.indexOf(' ') + 1);
            treeReader.close();

            BufferedWriter indexWriter = new BufferedWriter(new FileWriter("./git/index"));
            indexWriter.write(indexRecursive(tree, ""));
            indexWriter.close();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String indexRecursive(String tree, String path) {
        try {
            BufferedReader file = new BufferedReader(new FileReader("./git/objects/" + tree));
            StringBuilder index = new StringBuilder();
            String line;
            while ((line = file.readLine()) != null) {
                if (line.startsWith("blob")) {
                    System.out.println(line);
                    index.append(line.substring(line.indexOf(' ') + 1, line.lastIndexOf(' ') + 1));
                    index.append(path.substring(1)).append('/').append(line.substring(line.lastIndexOf(' ') + 1))
                            .append('\n');
                } else {
                    index.append(indexRecursive(line.substring(line.indexOf(' ') + 1, line.lastIndexOf(' ')),
                            path + "/" + line.substring(line.lastIndexOf(' ') + 1)));
                }
            }
            file.close();
            return index.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static void checkout(String commit) {
        try {
            // check if commit is a branch
            File branchFile = new File("./git/refs/heads/" + commit);
            String head = commit;

            if (branchFile.exists()) {
                BufferedReader branchReader = new BufferedReader(new FileReader(branchFile));
                commit = branchReader.readLine();
                branchReader.close();

                head = "ref: refs/heads/" + branchFile.getName();
            }

            // check if commit exists
            File commitPath = new File("./git/objects/" + commit);
            if (!commitPath.isFile()) {
                throw new IOException("Commit or branch does not exist");
            }

            ArrayList<String> oldIndex = new ArrayList<>(Files.readAllLines(Paths.get("./git/index")));
            rebuildIndex(commit);
            ArrayList<String> newIndex = new ArrayList<>(Files.readAllLines(Paths.get("./git/index")));

            for (String line : oldIndex) {
                String oldPath = line.substring(line.indexOf(' ') + 1);
                boolean found = false;

                for (String newLine : newIndex) {
                    String newPath = newLine.substring(newLine.indexOf(' ') + 1);

                    if (oldPath.equals(newPath)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    File delPath = new File(oldPath.substring(oldPath.indexOf('/') + 1));
                    delPath.delete();
                }
            }

            // restore files from blobs
            for (String line : newIndex) {
                String[] parts = line.split(" ", 2);

                String hash = parts[0];
                String filePath = parts[1];

                filePath = filePath.substring(filePath.indexOf('/') + 1);

                File file = new File(filePath);

                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                Files.copy(Paths.get("./git/objects/" + hash), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // update HEAD
            BufferedWriter headWriter = new BufferedWriter(new FileWriter("./git/HEAD"));
            headWriter.write(head);
            headWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createBranch(String name) {
        try {
            File branchFile = new File("./git/refs/heads/" + name);
            if (branchFile.exists()) {
                throw new IOException("branch already exists");
            }

            // get current commit
            BufferedReader headReader = new BufferedReader(new FileReader("./git/HEAD"));
            String head = headReader.readLine();
            headReader.close();

            if (head == null) {
                throw new IOException("no commits yet");
            }

            String commit = head;

            if (head.startsWith("ref: ")) {
                BufferedReader branchReader = new BufferedReader(new FileReader("./git/" + head.substring(5)));
                commit = branchReader.readLine();
                branchReader.close();
            }

            if (commit == null) {
                throw new IOException("No commits yet");
            }

            // create new branch
            BufferedWriter bw = new BufferedWriter(new FileWriter(branchFile));
            bw.write(commit);
            bw.close();

        } catch (Exception e) {
            throw new RuntimeException("couldn't create branch", e);
        }
    }
}
