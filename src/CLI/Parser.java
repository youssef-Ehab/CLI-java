package CLI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class Parser {
    private String[] args = new String[]{};
    private String cmd;
    private Terminal terminal = new Terminal();
    private boolean Exit = false;

    private String parse(String input, String output) throws Exception {
        Vector<String> splittedVector = new Vector<>();
        boolean skipws = true;

        StringBuilder sb = new StringBuilder();
        for (char ch :
                input.toCharArray()) {
            if (ch == '\"') {
                skipws = !skipws;
                continue;
            }
            if (ch == ' ' && skipws) {
                splittedVector.add(sb.toString());
                sb = new StringBuilder();
                continue;
            }

            sb.append(ch);
        }

        if (sb.toString().length() > 0) splittedVector.add(sb.toString());

        String[] splitted = new String[splittedVector.size()];
        splittedVector.copyInto(splitted);

        cmd = splitted[0];
        args = new String[splitted.length - 1];
        System.arraycopy(splitted, 1, args, 0, splitted.length - 1);

        if (output != null) {
            cmd = args[0];
            args[0] = output;
        }

        if (cmd.equals("|")) {
            cmd = args[0];

            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);

            args = newArgs.clone();
        }

        if (isCommand()) {
            switch (cmd) {
                case "clear":
                    return handleClear();
                case "pwd":
                    return handlePwd();
                case "cd":
                    return handleCd();
                case "ls":
                    return handleLs();
                case "cp":
                    return handleCp();
                case "cat":
                    return handleCat();
                case "rm":
                    return handleRm();
                case "more":
                    return handleMore();
                case "mv":
                    return handleMv();
                case "mkdir":
                    return handleMkdir();
                case "rmdir":
                    return handleRmdir();
                case "date":
                    return handleDate();
                case "args":
                    return handleArgs();
                case "help":
                    return handleHelp();
                case "exit":
                    Exit = true;
                    return "";
            }
        }
        return "";
    }

    void parseWrap(String input) throws Exception {
        String[] splitted = input.split(" ");

        StringBuilder sb = new StringBuilder();
        boolean pipe = false, redirect = false, app = false;
        String output = null;
        for (String word :
                splitted) {
            switch (word) {
                case ">":
                    if (pipe) {
                        output = parse(sb.toString(), output);
                        pipe = false;
                    } else {
                        output = parse(sb.toString(), null);
                    }
                    redirect = true;
                    app = false;
                    break;
                case ">>":
                    if (pipe) {
                        output = parse(sb.toString(), output);
                        pipe = false;
                    } else {
                        output = parse(sb.toString(), null);
                    }
                    redirect = true;
                    app = true;
                    break;
                case "|":
                    if (pipe) {
                        output = parse(sb.toString(), output);
                    } else if (redirect) {
                        output = redirect(sb.toString(), output, app);
                        redirect = false;
                        app = false;
                    } else output = parse(sb.toString(), null);
                    pipe = true;
                    sb = new StringBuilder();
                    sb.append(word).append(" ");
                    break;
                default:
                    sb.append(word).append(" ");
                    break;
            }
        }
        if (pipe) {
            System.out.println(parse(sb.toString(), output));
        } else if (redirect) {
            redirect(sb.toString(), output, app);
        } else {
            System.out.println(parse(sb.toString(), null));
        }
    }

    private String redirect(String file, String output, boolean app) throws IOException {
        FileWriter fw = new FileWriter(terminal.setFilePath(file), app);
        fw.write(output);
        fw.close();
        return terminal.setFilePath(file);
    }

    private boolean isCommand() {
        return cmd.equals("cd") || cmd.equals("ls") || cmd.equals("cp") || cmd.equals("cat") || cmd.equals("more") ||
                cmd.equals("mkdir") || cmd.equals("rmdir") || cmd.equals("mv") || cmd.equals("rm") ||
                cmd.equals("args") || cmd.equals("date") || cmd.equals("help")
                || cmd.equals("pwd") || cmd.equals("clear") || cmd.equals("exit");
    }


    private String handleClear() {
        if (args.length > 0) {
            System.out.println("clear command doesn't take any arguments");
            return null;
        }

        return terminal.clear();
    }

    private String handlePwd() {
        if (args.length > 0) {
            System.out.println("pwd command doesn't take any arguments.");
            return null;
        }

        return terminal.pwd();
    }

    private String handleCd() throws Exception {
        if (args.length > 1) {
            System.out.println("cd command accepts at most one parameter");
            return null;
        } else {
            if(args.length == 0) {
                args = new String[1];
                args[0] = "C:\\";
                return terminal.cd(args);
            }

            terminal.cd(args);
            return null;
        }
    }


    private String handleLs() throws IOException {
        if(args.length > 1) {
            System.out.println("ERROR: command \"ls\" takes at most 1 argument.");
            return null;
        }

        if (args.length == 1) return terminal.ls(args[0]);
        else return terminal.ls();
    }

    private String handleCat() throws Exception {
        if(args.length==0){
            System.out.println("cat command takes at least 1 argument");
            return null;
        }

        return terminal.cat(args);
    }

    private String handleCp() throws Exception {
        if (args.length < 2) {
            System.out.println("cp command takes at least two arguments");
            return null;
        }

        if (args.length == 2)
            return terminal.cp(args[0], args[1]);
        else
            return terminal.cp(args);
    }

    private String handleRm() {
        if (args.length == 0) {
            System.out.println("rm command takes at least one argument");
            return null;
        }

        return terminal.rm(args);
    }

    private String handleMore() throws IOException {
        if (args.length != 1) {
            System.out.println("more command takes 1 argument.");
            return null;
        }

        File file = new File(terminal.setFilePath(args[0]));
        if (file.exists()) terminal.more(file);
        else terminal.more(args[0]);

        return "";
    }

    private String handleMv() throws Exception {
        if (args.length != 2) {
            System.out.println("mv command takes at two arguments");
            return null;
        }

        return terminal.mv(args[0], args[1]);
    }

    private String handleMkdir() {
        if (args.length == 0) {
            System.out.println("mkdir command takes at least one argument");
            return null;
        }

        return terminal.mkdir(args);
    }

    private String handleRmdir() {
        if (args.length == 0) {
            System.out.println("rmdir command takes at least one argument");
            return null;
        }

        return terminal.rmdir(args);
    }

    private String handleDate() {
        if (args.length > 0) {
            System.out.println("date command doesn't take any arguments.");
            return null;
        }

        return terminal.Date();
    }

    private String handleArgs() throws Exception {
        if (args.length != 1) {
            System.out.println("args command takes 1 argument.");
            return null;
        }

        return terminal.Args(args[0]);
    }

    private String handleHelp() {
        if (args.length > 0) {
            System.out.println("help command doesn't take any arguments.");
            return null;
        }

        return terminal.Help();
    }

    public String getCmd() { return cmd; }
    public String[] getArgs() { return args; }

    boolean isExit() {
        return Exit;
    }
}
