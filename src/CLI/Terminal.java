package CLI;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

class Terminal {
    private String currDirectory;
    private int lsSize;

    Terminal() {
        currDirectory = "C:\\";
    }

    String setFilePath(String FileName) {
        File file = new File(FileName);
        if (FileName.equals(file.getAbsolutePath())) return FileName;
        else return currDirectory + FileName;
    }

    String clear() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < 25; i++) ret.append("\n");
        return ret.toString();
    }

    String pwd() {
        return (currDirectory);
    }

    String cd(String[] args) throws Exception {
        File file = new File(args[0]);
        if (file.exists() && file.isDirectory()) currDirectory = args[0];
        else throw new Exception(args[0] + " is not a valid directory.");
        return currDirectory;
    }

    String ls(String dirName) throws IOException {
        AtomicInteger ret = new AtomicInteger();
        StringBuilder sb = new StringBuilder();
        Files.list(new File(dirName).toPath())
                .forEach(path -> {
                    sb.append(path).append("\n");
                    ret.getAndIncrement();
                });
        lsSize = ret.intValue();
        return sb.toString();
    }

    String ls() throws IOException {
        return ls(currDirectory);
    }

    String cp(String SourcePath, String DestinationPath) throws Exception {
        SourcePath = setFilePath(SourcePath);
        DestinationPath = setFilePath(DestinationPath);

        File file = new File(SourcePath);

        if (!file.exists()) {
            System.out.println(SourcePath);
            System.out.println(DestinationPath);
            throw new Exception("Couldn't find this file/directory");
        }

        file = new File(DestinationPath);

        if (!file.exists()) {
            if (file.createNewFile()) {
                System.out.println("Second file didn't exist, creating file now");
            } else {
                throw new Exception("Failed to create file");
            }
        }

        FileReader fr = new FileReader(SourcePath);
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(DestinationPath, true);
        String s;

        while ((s = br.readLine()) != null) {
            fw.write(s);
            fw.flush();
        }

        fr.close();
        fw.close();
        return DestinationPath;
    }

    String cp(String[] args) throws Exception {
        String dir = args[args.length - 1];
        for (int i = 0; i < args.length - 1; i++) {
            cp(args[i], dir + args[i]);
        }
        return dir;
    }

    String rm(String[] args) {
        for (String arg : args) {
            arg = setFilePath(arg);
            File file = new File(arg);
            if (!file.delete()) return ("Error deleting file.");
        }
        return args[args.length - 1];
    }

    String mv(String SourcePath, String DestinationPath) throws Exception {
        cp(SourcePath, DestinationPath);
        rm(new String[]{SourcePath});
        return DestinationPath;
    }

    String mkdir(String[] args) {
        for (String arg : args) {
            arg = setFilePath(arg);
            File file = new File(arg);
            if (!file.mkdir()) return ("Error making directory " + arg);
        }
        return args[args.length - 1];
    }

    String rmdir(String[] args) {
        for (String arg : args) {
            arg = setFilePath(arg);
            Integer directoryFiles = lsSize;
            if (directoryFiles > 0)
                return ("Error removing directory \"" + arg + "\", the directory is not empty.");
            else rm(new String[]{arg});
        }
        return args[args.length - 1];
    }

    void more(String input) {

        Scanner scanner = new Scanner(input);
        Scanner sysScanner = new Scanner(System.in);

        do {
            for (int i = 0; i < 10; i++) if (scanner.hasNextLine()) System.out.println(scanner.nextLine());
            sysScanner.nextLine();
        } while (scanner.hasNextLine());
    }

    void more(File file) throws IOException {
        Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
        StringBuilder fileContents = new StringBuilder();
        stream.forEach(s -> fileContents.append(s).append("\n"));

        more(fileContents.toString());
    }

    String cat(String[] args) throws Exception {
        Boolean check = false;
        List<String> left = new ArrayList<>();
        List<String> right = new ArrayList<>();

        int i;
        for (i = 0; i < args.length; ++i) {
            if (args[i].charAt(0) == '>') {
                check = true;
                break;
            }
        }

        if (check) {
            left.addAll(Arrays.asList(args).subList(0, i));
            right.addAll(Arrays.asList(args).subList(i, args.length));
        }


        if (left.size() == 0 && right.size() == 1) {         // aka create new file
            args[0] = setFilePath(args[0].substring(1, args[0].length()));
            File file = new File(args[0]);

            if (!(file.exists())) file.createNewFile();

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));

            String line;
            while (!(line = in.readLine()).equals("\\0")) {
                writer.write(line);
            }

            writer.close();
            return file.getAbsolutePath();
        } else if (left.size() > 0 && right.size() == 1) {      // Copy contents of multiple files to one file
            StringBuilder temp = new StringBuilder();
            String line;

            for (i = 0; i < left.size(); ++i) {
                left.set(i, setFilePath(left.get(i)));
                File e = new File(left.get(i));

                if (!(e.exists())) continue;

                BufferedReader r = new BufferedReader(new FileReader(left.get(i)));
                line = r.readLine();

                while (line != null) {
                    temp.append(line);
                    temp.append('\n');
                    line = r.readLine();
                }

                r.close();
            }
            /*right.set(0,setFilePath(right.get(0)));*/
            File f = new File(right.get(0));
            if (!(f.exists())) f.createNewFile();

            BufferedWriter w = new BufferedWriter(new FileWriter(right.get(0)));
            w.write(temp.toString());
            w.close();

            return f.getAbsolutePath();
        } else if (!check) {          // aka view contents of file
            StringBuilder sb = new StringBuilder();

            for (i = 0; i < args.length; ++i) {
                args[i] = setFilePath(args[i]);
                File f = new File(args[i]);

                if (!(f.exists())) {
                    throw new Exception("File " + f.getAbsolutePath() + " Not Found");
                }

                BufferedReader r = new BufferedReader(new FileReader(args[i]));
                String line;
                line = r.readLine();

                while (line != null) {
                    sb.append(line).append('\n');
                    line = r.readLine();
                }
                r.close();

            }
            return sb.toString();
        } else {
            throw new Exception("Invalid arguments");
        }
    }

    String Args(String cmd) throws Exception {
        switch (cmd) {
            case "cp":
                return ("arg1: Source Path\narg2: Destination Path\n");
            case "cd":
                return ("[arg1: Directory = Current Directory\n]");
            case "ls":
                return ("[arg1: Destination Directory = Current Directory]\n");
            case "mv":
                return ("arg1: Source Path\narg2: Destination Path\n");
            case "rm":
                return ("args: File1 [File2] [File3]...\n");
            case "mkdir":
                return ("args: Dir1 [Dir2] [Dir3]...\n");
            case "rmdir":
                return ("args: Dir1 [Dir2] [Dir3]...\n");
            case "more":
                return ("args: fileName/Text\n");
            case "cat":
                return ("args: File1 [File2] [File3]...\n");
            case "pwd":
                return ("pwd command doesn't take any arguments\n");
            case "clear":
                return ("pwd command doesn't take any arguments\n");
            case "date":
                return ("pwd command doesn't take any arguments\n");
            case "help":
                return ("pwd command doesn't take any arguments\n");
            case "args":
                return ("arg1: cmd\n");
            default:
                throw new Exception("argument not valid.");
        }
    }

    String Help() {

        return "args: prints syntax of arguments of a command\n" +
                "cat: reads and outputs contents of file(s)\n" +
                "cd: changes current directory\n" +
                "clear: clears console window\n" +
                "cp: copies contents of a file(s) into another file/directory\n" +
                "date: prints current date and time\n" +
                "exit: Stop all\n" +
                "help: prints this help message\n" +
                "ls: lists contents of directory\n" +
                "mkdir: makes a directory(s)\n" +
                "more: outputs contents of input/file 10 lines at a time\n" +
                "mv: moves a file to a directory\n" +
                "pwd: prints current directory\n" +
                "rm: removes a file\n" +
                "rmdir: removes a directory(s)\n";
    }

    String Date() {
        return (LocalDateTime.now().toString());
    }
}