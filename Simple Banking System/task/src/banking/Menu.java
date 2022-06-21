package banking;

import java.sql.*;
import java.util.Scanner;

public class Menu {
    private final static String GENERAL_MENU = "1. Create an account\n"
            + "2. Log into account\n"
            + "0. Exit\n";
    private final static String LOG_MENU = "1. Balance\n"
            + "2. Add income\n"
            + "3. Do transfer\n"
            + "4. Close account\n"
            + "5. Log out\n"
            + "0. Exit\n";
    public static void startGeneralMenu(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS card (\n" +
                "id INTEGER,\n" +
                "number TEXT,\n" +
                "pin TEXT,\n" +
                "balance INTEGER DEFAULT 0\n" +
                ");";

        String  sqlCountEntry = "SELECT COUNT(id) FROM card;";

        try (Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement()) {
            // Create a table if it not exists
            stmt.execute(sqlCreateTable);
            try (ResultSet trs = stmt.executeQuery(sqlCountEntry)) {
                BankAccount.setLastAccount(trs.getInt(1));
            } catch (SQLException e) {
                e.getMessage();
            }
            boolean isActiveFlag = true;
            while (isActiveFlag) {
                System.out.println(GENERAL_MENU);
                Scanner scanner = new Scanner(System.in);
                try {
                    int choose = Integer.parseInt(scanner.next());
                    switch (choose) {
                        case 1: {
                            BankAccount newAcc = BankAccount.getNewAccount();
                            System.out.println("Your card has been created");
                            System.out.println("Your card number:\n" +  newAcc);
                            System.out.println("Your card PIN:\n" + newAcc.getPin());
                            String sqlAddNewAcc = "INSERT INTO card(id,number, pin, balance)\n" +
                                    "VALUES(?, ?, ?, ?)";
                            try (PreparedStatement pstmt = conn.prepareStatement(sqlAddNewAcc)) {
                                pstmt.setInt(1, newAcc.getId());
                                pstmt.setString(2, newAcc.getCardNumber());
                                pstmt.setString(3, newAcc.getPin());
                                pstmt.setInt(4, newAcc.getBalance());
                                pstmt.execute();
                            } catch (SQLException e) {
                                System.out.println(e.getMessage());
                            }
                            break;
                        }
                        case 2: {
                            System.out.println("Enter your card number:");
                            String userCardNumber = scanner.next();
                            String sql = "SELECT * FROM card WHERE number = " + userCardNumber + ";";
                            try (ResultSet rs = stmt.executeQuery(sql)) {
                                while (rs.next()) {
                                    int id = rs.getInt(1);
                                    String cardNumber = rs.getString(2);
                                    String pin = rs.getString(3);
                                    int balance = rs.getInt(4);
                                    System.out.println("Enter your PIN:");
                                    String userPin = scanner.next();
                                    if (!pin.equals(userPin)) {
                                        System.out.println("Wrong card number or PIN!");
                                        break;
                                    } else {
                                        BankAccount activeBankAccount = BankAccount.getExistAccount(id, cardNumber, pin, balance);
                                        System.out.println("You have successfully logged in!");
                                        isActiveFlag = startLogMenu(activeBankAccount, conn, url);
                                    }
                                }
                            } catch (SQLException e) {
                                System.out.println(e.getMessage());
                            } catch (Exception e) {
                                System.out.println("Wrong card number or PIN!");
                                continue;
                            }
                            break;
                        }
                        case 0: {
                            System.out.println("Bye!");
                            isActiveFlag = false;
                            break;
                        }
                        default: System.out.println("Invalid value");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid value, please repeat");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean startLogMenu(BankAccount bankAccount, Connection conn, String url) throws SQLException {
        boolean isActiveFlag = true;
        while (isActiveFlag) {
            System.out.println(LOG_MENU);
            Scanner scanner = new Scanner(System.in);
            int choose = Integer.parseInt(scanner.next());
            switch (choose) {
                case 1: {
                    System.out.println("Balance: " + bankAccount.getBalance());
                    break;
                }

                case 2: {
                    System.out.println("Enter income: ");
                    String addIncomeSQL = "UPDATE card SET balance = balance + ? WHERE id = ?";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(addIncomeSQL)) {
                        int addingBalance = scanner.nextInt();
                        preparedStatement.setInt(1, addingBalance);
                        preparedStatement.setLong(2, bankAccount.getId());
                        preparedStatement.execute();
                        bankAccount.addIncome(addingBalance);
                        System.out.println("Income added!");
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }

                case 3: {
                    System.out.println("Transfer\nEnter card number:");
                    String transferCardNumber = scanner.next();
                    if (!BankAccount.checkCheckSum(transferCardNumber)) {
                        System.out.println("Probably you made a mistake in the card number. Please try again!");
                        break;
                    } else if (bankAccount.getCardNumber().equals(transferCardNumber)) {
                        System.out.println("You can't transfer money to the same account!");
                        break;
                    } else {
                        conn.setAutoCommit(false);
                        {
                            String checkCardExistSQL = "SELECT * FROM card WHERE number = ?";
                            try (PreparedStatement preparedStatement = conn.prepareStatement(checkCardExistSQL)) {
                                preparedStatement.setString(1, transferCardNumber);
                                preparedStatement.execute();
                                try (ResultSet cards = preparedStatement.getResultSet()) {
                                    if (!cards.next()) {
                                        System.out.println("Such a card does not exist.");
                                        break;
                                    }
                                }
                            }
                            System.out.println("Enter how much money you want to transfer:");
                            int money = scanner.nextInt();
                            if (money > bankAccount.getBalance()) {
                                System.out.println("Not enough money!");
                                break;
                            } else {
                                conn.setAutoCommit(false);
                                String getMoneyFromSQL = "UPDATE card SET balance = balance - ? WHERE number = ?";
                                String putMoneyToSQL = "UPDATE card SET balance = balance + ? WHERE number = ?";
                                try (PreparedStatement getMoney = conn.prepareStatement(getMoneyFromSQL);
                                     PreparedStatement putMoney = conn.prepareStatement(putMoneyToSQL)) {
                                    Savepoint savepoint1 = conn.setSavepoint();
                                    getMoney.setInt(1, money);
                                    getMoney.setString(2, bankAccount.getCardNumber());
                                    getMoney.execute();

                                    Savepoint savepoint2 = conn.setSavepoint();
                                    putMoney.setInt(1, money);
                                    putMoney.setString(2, transferCardNumber);
                                    putMoney.execute();

                                    conn.commit();
                                    bankAccount.addIncome(-money);
                                    System.out.println("Success!");

                                }
                            }
                        }
                        conn.setAutoCommit(true);

                    }

                    break;
                }

                case 4: {
                    System.out.println("The account has been closed!");
                    String removeSQL = "DELETE FROM card WHERE id=?";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(removeSQL)) {
                        preparedStatement.setLong(1, bankAccount.getId());
                        preparedStatement.execute();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    isActiveFlag = false;
                    return true;
                }

                case 5: {
                    System.out.println("You have successfully logged out!");
                    isActiveFlag = false;
                    return true;
                }

                case 0: {
                    System.out.println("Bye!");
                    isActiveFlag = false;
                    return false;
                }
                default: {
                    System.out.println("Invalid value!");
                }
            }
        }
        return true;
    }
}
