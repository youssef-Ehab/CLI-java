package CLI;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Parser parser = new Parser();
        while (!parser.isExit()) {
            String input;
            if (scanner.hasNextLine()) {
                input = scanner.nextLine();
                try {
                    parser.parseWrap(input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
