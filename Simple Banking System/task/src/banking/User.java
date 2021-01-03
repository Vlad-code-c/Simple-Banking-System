package banking;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class User {
    //4_00000_493832089_5
    //4         - Major Industry Identifier
    //400000    - Bank Identification Number
    //493832089 - Account Identifier
    //5         - checksum

    private String cardNumber;
    private String PIN;
    private long balance;
    private String accountIndentifier;

//    private static ArrayList<User> users = new ArrayList<>();
//    protected static DatabaseHandler db;

    public User(DatabaseHandler db) {
                //CardNumber with Issuer Identification Number
        String cardNumber = "400000";

        //Account Identifier
        accountIndentifier = getUniqueAccountIdentifier(db);
        cardNumber += accountIndentifier;

        //Checksum
        try {
            cardNumber += luhnChecksum(cardNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.cardNumber = cardNumber;
        this.PIN = generatePIN();

        this.balance = 0;

        addUser(this, db);
    }

    public User(String accountIndentifier, String cardNumber, String PIN, long balance) {
        this.cardNumber = cardNumber;
        this.PIN = PIN;
        this.balance = balance;
        this.accountIndentifier = accountIndentifier;
    }

    private void addUser(User user, DatabaseHandler db) {
        db.addCard(user);
    }

    private String generatePIN() {
        return "" + (new Random().nextInt(9999 - 1001) + 1000);
    }


    private String getUniqueAccountIdentifier(DatabaseHandler db) {
        Random random = new Random();
        String accountIdentifier = "";

        do {
            accountIdentifier = "";

            accountIdentifier += random.nextInt(999 - 101) + 100;
            accountIdentifier += random.nextInt(999 - 101) + 100;
            accountIdentifier += random.nextInt(999 - 101) + 100;
        } while (containsUser(accountIdentifier, db) || accountIdentifier.length() != 9);

        return accountIdentifier;
    }



    private boolean containsUser(String accountIdentifier, DatabaseHandler db) {
        ResultSet resSet = db.getCards(accountIdentifier, "", "");

        try {
            while (resSet.next()) {
                if (String.valueOf(resSet.getInt("id")).equals(accountIdentifier)) {
                    return true;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }

    /**
     * Step 1: Drop Last Digit
     * Step 2: Multiply Odd digits by 2
     * Step 3: Substract 9 to numbers over 9
     * Step 4: Add all numbers
     * Step 5: Divide by 10
     * */
    public int luhnChecksum(String incompleteCardNumber) throws Exception {
        int[] digits = new int[15];
        int sum = 0;
        int checkSum = 0;

        if (incompleteCardNumber.length() != 15) {
            throw new Exception("Incompatible cardNumber length. Length should be 15. Your length: " + incompleteCardNumber.length());
        }

        //0. Get digits from incompleteCardNumber
        for (int i = 0; i < incompleteCardNumber.length(); i++) {
            digits[i] = Integer.parseInt(String.valueOf(incompleteCardNumber.charAt(i)));
        }

        //2. Multiply Odd digits by 2
        for (int i = 0; i < digits.length; i++) {
            if (i % 2 == 0) {
                digits[i] = digits[i] * 2;
            }
        }

        //3. Substract 9 to numbers over 9
        for (int i = 0; i < digits.length; i++) {
            if (digits[i] > 9) {
                digits[i] = digits[i] - 9;
            }
        }

        //4. Add all numbers
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }

        //5. Divide by 10
        if (sum % 10 == 0) {
            checkSum = 0;
        } else {
            checkSum = 10 - (sum % 10);
        }

        return checkSum;
    }


    public boolean transfer(String cardNumberToTransfer, DatabaseHandler db) throws Exception {
        //If the user tries to transfer money to the same account
        if (getCardNumber().equals(cardNumberToTransfer)) {
            throw new Exception("You can't transfer money to the same account!");
        }


        //If the receiver's card number doesn’t pass the Luhn algorithm
        if (!String.valueOf(
                luhnChecksum(cardNumberToTransfer.substring(0, 15)))
                .equals(String.valueOf(
                        cardNumberToTransfer.charAt(cardNumberToTransfer.length()-1)))) {
            throw new Exception("Probably you made mistake in the card number. Please try again!");
        }

        //If the receiver's card number doesn’t exist
        if (!db.existCard(cardNumberToTransfer)) {
            throw new Exception("Such a card does not exist.");
        }


        System.out.print("Enter how much money you want to transfer\n>");
        int money = new Scanner(System.in).nextInt();


        //If the user tries to transfer more money than he/she has
        if (getBalance() < money) {
            throw new Exception("Not enough money!");
        }


        //If there is no error, make the transaction.
        db.transferMoney(this, cardNumberToTransfer, money);


        return true;
    }

    protected void deleteAccount(DatabaseHandler db) {
        db.dropCard(this);
    }

    private ResultSet getCards(DatabaseHandler db) {
        return db.getCards("", "", "");
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }


    public void addIncome(User user, int income, DatabaseHandler db) {

        System.out.println( "acc" + user.getAccountIndentifier());
        System.out.println( "bal" + user.balance);

        setBalance(user, balance + income, db);

    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(User user, long balance, DatabaseHandler db) {

        user.balance = balance;
        db.updateBalance(user);

    }


    public String getAccountIndentifier() {
        return accountIndentifier;
    }

    public void setAccountIndentifier(String accountIndentifier) {
        this.accountIndentifier = accountIndentifier;
    }

    @Override
    public String toString() {
        return "User{" +
                "cardNumber='" + cardNumber + '\'' +
                ", PIN='" + PIN + '\'' +
                ", balance=" + balance +
                ", accountIndentifier='" + accountIndentifier + '\'' +
                '}';
    }
}
