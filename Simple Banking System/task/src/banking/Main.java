package banking;

import java.util.Scanner;


public class Main {
    private static boolean isLogged = false;
    private static User currentUser = null;
    protected static String databaseName;
    private static DatabaseHandler dbHandler;


    public static void main(String[] args) {
        databaseName = "banking";

        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].contains("-fileName")) {
                databaseName = args[i + 1];
                break;
            }
        }

        new Main().start();
    }

    public void start() {
        dbHandler = new DatabaseHandler(databaseName);
        dbHandler.createDatabase();

        while (true) {
            showMenu();
            callMethods(readOption());
        }
    }



    private void showMenu() {
        if (currentUser == null) {
            System.out.println("1. Create an account\n" +
                                "2. Log into account\n" +
                                "0. Exit");
        } else {
            System.out.println("1. Balance\n" +
                                "2. Add income\n" +
                                "3. Do transfer\n" +
                                "4. Close account\n" +
                                "5. Log out\n" +
                                "0. Exit");
        }
    }


    private int readOption() {
        System.out.print("> ");
        int opt = 0;
        try {
            opt = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println("Invalid option. ");
        }
        return opt;
    }

    private void callMethods(int opt) {
        if (currentUser == null) {
            switch (opt) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    logIn();
                    break;
                case 0:
                    existApplication();
                default:
                    System.out.println("Invalid option");
            }
        } else {
            switch (opt) {
                case 1:
                    showBalance();
                    break;
                case 2:
                    addIncome();
                    break;
                case 3:
                    doTransfer();
                    break;
                case 4:
                    closeAccount();
                    break;
                case 5:
                    logOut();
                    break;
                case 0:
                    existApplication();
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void closeAccount() {
        System.out.println("The account has been clossed!");
        currentUser.deleteAccount(dbHandler);
        logOut();
    }

    private void addIncome() {
        System.out.print("Enter income:\n>");
        int money = new Scanner(System.in).nextInt();


        System.out.println( "acc" + currentUser.getAccountIndentifier());
        System.out.println( "bal" + currentUser.getBalance());


        currentUser.addIncome(currentUser, money, dbHandler);

        System.out.println("Income was added!");
    }

    private void doTransfer() {
        System.out.print("Transfer\nEnter card number\n>");
        String cardNum = new Scanner(System.in).next();


        try {
            currentUser.transfer(cardNum, dbHandler);
            System.out.println("Succes");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void logOut() {
        currentUser = null;
    }

    private void showBalance() {
        System.out.println("Balance: " + currentUser.getBalance());
    }

    private void createAccount() {
        User user = new User(dbHandler);
        System.out.println("Your card has been created\n" +
                "Your card number:\n" +
                user.getCardNumber() +
                "\nYour card PIN:\n" +
                user.getPIN());

    }

    private void logIn() {
        System.out.print("Enter your card number:\n>");
        String cardNumber = new Scanner(System.in).next();
        System.out.print("Enter your PIN:\n>");
        String PIN = new Scanner(System.in).next();


        User currentUser = dbHandler.getUser(cardNumber, PIN);    //User.existsCardNumber(cardNumber, PIN, dbHandler);
        if (currentUser == null) {
            System.out.println("\nWrong card number or PIN!\n");
        } else {
            System.out.println("You have successfully logged in!");
            Main.currentUser = currentUser;

        }

    }

    private void existApplication() {
        System.out.println("\nBye!\n");
        System.exit(0);
    }

}


