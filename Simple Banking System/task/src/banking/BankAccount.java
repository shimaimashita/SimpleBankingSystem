package banking;

import java.util.Arrays;

public class BankAccount {
    private static final int BIN = 400000;
    private int[] cardNumber;
    private int balance;
    private int[] pin;
    private int id;
    private static int lastAccount = 0;

    private BankAccount(int id, int[] cardNumber) {
        this(id, cardNumber, genPin(), 0);
    }

    private BankAccount(int id, int[] cardNumber, int balance) {
        this(id, cardNumber, genPin(), balance);
    }

    private BankAccount(int id, int[] cardNumber, int[] pin, int balance) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.balance = balance;
        this.pin = pin;
    }

    // Factory method that create a new account with a pre-generated pin and a new
    // account number;
    static public BankAccount getNewAccount() {
        // generate account identifier (9 digits after BIN and before checksum:
        int accId = lastAccount++;
        String strId = String.format("%09d", lastAccount++);
        int[] cardNumber = new int[] {4, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0};
        char[] tempArray = strId.toCharArray();
        for (int i = 6; i < cardNumber.length - 1; i++) {
            cardNumber[i] = Character.digit(tempArray[i-6], 10);
        }

        cardNumber[cardNumber.length - 1] = genCheckSum(Arrays.copyOf(cardNumber, cardNumber.length));
        return new BankAccount(accId, cardNumber); // add leading zeroes
    }

    public static BankAccount getExistAccount(int id, String cardNumber, String pin, int balance) {
        char[] cardArray = cardNumber.toCharArray();
        int[] cardRes = new int[cardArray.length];
        char[] pinArray = pin.toCharArray();
        int[] pinRes = new int[pinArray.length];
        for (int i = 0; i < cardArray.length; i++) {
            cardRes[i] = Character.digit(cardArray[i], 10);
        }
        for (int i = 0; i < pinArray.length; i++) {
            pinRes[i] = Character.digit(pinArray[i], 10);
        }

        return new BankAccount(id, cardRes, pinRes, balance);
    }

    private static int genCheckSum(int[] copyOf) {
        int[] temp = Arrays.copyOf(copyOf, copyOf.length);
        int sum = 0;
        for (int i = 0; i < copyOf.length - 1; i++) {
            if ((i + 1) % 2 != 0) {
                copyOf[i] *= 2;
            }
            if (copyOf[i] > 9) {
                copyOf[i] -= 9;
            }
            sum += copyOf[i];
        }
        int checkSum = 10 - sum % 10;
        if (checkSum == 10) {
            checkSum = 0;
        }
        temp[temp.length - 1] = checkSum;
        return checkSum;
    }

    public static boolean checkCheckSum(String cardNumber) {
        char[] cardArray = cardNumber.toCharArray();
        int[] intArrayCard = new int[cardArray.length];
        for (int i = 0; i < cardArray.length; i++) {
            intArrayCard[i] = Character.digit(cardArray[i], 10);
        }
        int sum = 0;
        for (int i = 0; i < intArrayCard.length - 1; i++) {
            if ((i + 1) % 2 != 0) {
                intArrayCard[i] *= 2;
            }
            if (intArrayCard[i] > 9) {
                intArrayCard[i] -= 9;
            }
            sum += intArrayCard[i];
        }
        if (((sum + intArrayCard[intArrayCard.length - 1]) % 10) == 0) {
            return true;
        } else {
            return false;
        }
    }

    // Returns an array of length 4 that keep pin-code
    static public int[] genPin() {//If the received number is divisible by 10 with the remainder equal to zero, then this number is valid;
        int[] pin = new int[4];
        for (int i = 0; i < 4; i++) {
            pin[i] = (int) (Math.random() * 10);
        }
        return pin;
    }

    public static void setLastAccount(int lastAccount)  {
        BankAccount.lastAccount = lastAccount;
    }

    public String getPin() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j : this.pin) {
            stringBuilder.append(Integer.toString(j));
        }
        return stringBuilder.toString();
    }

    public String getCardNumber() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j : this.cardNumber) {
            stringBuilder.append(Integer.toString(j));
        }
        return stringBuilder.toString();
    }

    public int getBalance() {
        return balance;
    }

    public void addIncome(int addingBalance) {
       this.balance = this.balance + addingBalance;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return getCardNumber();
    }
}
