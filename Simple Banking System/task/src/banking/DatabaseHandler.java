package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;

public class DatabaseHandler {
    public String connectionUrl;
    private SQLiteDataSource dataSource = new SQLiteDataSource();

    private String createTableCard = "CREATE TABLE IF NOT EXISTS card (\n"
                                    + "	id INTEGER PRIMARY KEY,\n"
                                    + "	number TEXT NOT NULL,\n"
                                    + " pin TEXT NOT NULL,\n"
                                    + " balance INTEGER DEFAULT 0\n"
                                    + ");";

    public DatabaseHandler(String databaseName) {
        this.connectionUrl = "jdbc:sqlite:" + databaseName;
        dataSource.setUrl(connectionUrl);
        createDatabase();
    }


    /**
     * Return a new Connection to database.
     * @return Connection
     * */
    private Connection getConnection() {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            if (!con.isValid(5)) {
                return null;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return con;
    }

    /**
     * Create table card in database indicated in path if talbe don't exist arleady
     * */
    public void createDatabase() {
        Connection con = getConnection();
        if (con != null) {
            try {
                Statement stmt = con.createStatement();
                stmt.execute(createTableCard);

                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }


    /**
     * Return all cards with specified params. If params are empty ( == ""), all cards will returned
     * @param accountIdentifier table id
     * @param cardNumber number of card
     * @param pin pin code for card
     * */
    public ResultSet getCards(String accountIdentifier, String cardNumber, String pin) {
        String selectAllCards = "SELECT id, number, pin, balance FROM card";
        Connection con = getConnection();

        if (accountIdentifier != "") {
            selectAllCards = "SELECT id, number, pin, balance FROM card WHERE id = ?";
        } else if (cardNumber != "") {
            selectAllCards = "SELECT id, number, pin, balance FROM card WHERE number = ? AND pin = ?";
        }

        ResultSet resSet = null;
        try {
            PreparedStatement prst = con.prepareStatement(selectAllCards);

            if(accountIdentifier != "") {
                prst.setString(1, accountIdentifier);
            } else if (cardNumber != "") {
                prst.setString(1, cardNumber);
                prst.setString(2, pin);
            }


            resSet = prst.executeQuery();
            con.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return resSet;
    }


    public User getUser(String cardNumber, String pin) {
        String selectUser = "SELECT id, number, pin, balance FROM card WHERE number = ? AND pin = ?";
        Connection con = getConnection();

        try {
            PreparedStatement prSt = con.prepareStatement(selectUser);
            prSt.setString(1, cardNumber);
            prSt.setString(2, pin);

            ResultSet resSet = prSt.executeQuery();

            if (resSet.next()) {
                User user = new User(
                        String.valueOf(resSet.getInt(1)),
                        resSet.getString(2),
                        resSet.getString(3),
                        resSet.getLong(4)
                );


                con.close();
                return user;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try { con.close(); } catch (SQLException throwables) {}

        return null;
    }



    /**
     * Verify if exist card with cardNumber in db
     * @param cardNumber
     * @return true/false
     * */
    public boolean existCard(String cardNumber) {
        String selectAllCards = "SELECT id FROM card WHERE number = ?";
        Connection con = getConnection();


        try {
            PreparedStatement prst = con.prepareStatement(selectAllCards);
            prst.setString(1, cardNumber);

            ResultSet resSet = prst.executeQuery();
            con.close();

            if (resSet.next()) {
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }


    /**
     * Add card to database.
     * @param user current user wich will saved into database
     * @return 0 if error
     * */
    public int addCard(User user) {
        String insertNewCard = "INSERT INTO card(id, number, pin, balance) VALUES(?, ?, ?, ?)";
        Connection con = getConnection();

        try {
            PreparedStatement prSt = con.prepareStatement(insertNewCard);
            prSt.setString(1, user.getAccountIndentifier());
            prSt.setString(2, user.getCardNumber());
            prSt.setString(3, user.getPIN());
            prSt.setLong(4, user.getBalance());

           int res = prSt.executeUpdate();

           con.close();

           return res;

        } catch (SQLException throwables) {
            throwables.printStackTrace();

            return 0;
        }

    }

    /**
     * Update card balace for a user.
     * @param user
     * @return An int that indicates the number of rows affected
     * */
    public int updateBalance(User user) {
        String updBalance = "UPDATE card SET balance = ? WHERE id = ?";
        Connection con = getConnection();

        try{
            PreparedStatement prSt = con.prepareStatement(updBalance);
            prSt.setLong(1, user.getBalance());
            prSt.setString(2, user.getAccountIndentifier());


            int res = prSt.executeUpdate();

            con.close();

            return res;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    public boolean transferMoney (User from, String cardNumberTo, int money) {
        String withdrawMoney = "UPDATE card SET balance = balance - ? WHERE number = ?";
        String addMoney = "UPDATE card SET balance = balance + ? WHERE number = ?";

        Connection con = getConnection();
        try {
            con.setAutoCommit(false);

            //Withdraw money from first card
            PreparedStatement prStWithdraw = con.prepareStatement(withdrawMoney);
            prStWithdraw.setInt(1, money);
            prStWithdraw.setString(2, from.getCardNumber());
            prStWithdraw.executeUpdate();


            //Add money to second card
            PreparedStatement prStAdd = con.prepareStatement(addMoney);
            prStAdd.setInt(1, money);
            prStAdd.setString(2, cardNumberTo);
            prStAdd.executeUpdate();


            //Save changes to db
            con.commit();

            con.setAutoCommit(true);

            con.close();

            return true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }

    public boolean dropCard(User user) {
        String delete = "DELETE FROM card WHERE number = ?";

        Connection con = getConnection();
        try {
            PreparedStatement prSt = con.prepareStatement(delete);
            prSt.setString(1, user.getCardNumber());

            prSt.executeUpdate();
            con.close();

            return true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }
}
