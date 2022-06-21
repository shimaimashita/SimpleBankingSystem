package banking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String fileName = "bank.db";
        if (args.length >= 2) {
            for (int i = 0; i < args.length - 1; i++) {
                if ("-fileName".equals(args[i])) {
                    if (i < args.length -1) {
                        fileName = args[i + 1];
                        break;
                    }
                }
            }
        }
        Menu.startGeneralMenu(fileName);
    }
}