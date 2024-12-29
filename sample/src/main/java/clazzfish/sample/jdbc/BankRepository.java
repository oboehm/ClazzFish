/*
 * Copyright (c) 2019 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 01.07.2019 by oliver (ob@oasd.de)
 */

package clazzfish.sample.jdbc;

import clazzfish.jdbc.ProxyDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents the repository (or DB) of a bank. As default the
 * dates are stored in a in-memory DB (jdbc:hsqldb:mem:testd). If you want to
 * use another DB set the system property "JDBC_URL", e.g as
 * <pre>
 * java -DJDBC_URL="jdbc:proxy:hsqldb:file:/tmp/oli" ...
 * </pre>
 *
 * @author oboehm
 * @since 1.0 (01.07.2019)
 */
public final class BankRepository {

    private static final Logger log = LoggerFactory.getLogger(BankRepository.class);
    private static final String JDBC_URL = System.getProperty("JDBC_URL", "jdbc:proxy:hsqldb:mem:testdb");

    static {
        try {
            loadDbDriver();
        } catch (ClassNotFoundException cnfe) {
            throw new ExceptionInInitializerError(cnfe);
        }
    }

    /**
     * This method can be used to set up the DB.
     *
     * @throws SQLException e.g. if the DB was set up already
     */
    public static void setUpDB() throws SQLException {
        Connection connection = getConnection();
        String autoIncrement = "AUTO_INCREMENT";
        if (JDBC_URL.contains(":hsqldb:")) {
            autoIncrement = "IDENTITY";
        }
        String sql = "CREATE TABLE accounts (number INTEGER " + autoIncrement
                + " PRIMARY KEY, balance DECIMAL(10,2), name VARCHAR(50))";
        executeUpdate(sql, connection);
        connection.close();
    }

    private static void loadDbDriver() throws ClassNotFoundException {
        String driverName = "org.hsqldb.jdbcDriver";
        if (JDBC_URL.startsWith("jdbc:proxy")) {
            driverName = ProxyDriver.class.getName();
        }
        Class.forName(driverName);
        log.info("{} loaded.", driverName);
    }

    private static void executeUpdate(final String sql, final Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    /** Only static methods - no need to instantiate it. */
    private BankRepository() {}

    /**
     * Gets the account.
     *
     * @param number the number
     * @return the account
     * @throws SQLException the sQL exception
     */
    public static Account getAccount(final int number) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement stmt = connection
                .prepareStatement("SELECT balance, name FROM accounts where number = ?")) {
            stmt.setInt(1, number);
            stmt.execute();
            try (ResultSet rs = stmt.getResultSet()) {
                if (rs.next()) {
                    User user = new User(rs.getString("name"));
                    Account account = new Account(number, user);
                    account.setBalance(rs.getBigDecimal("balance"));
                    return account;
                } else {
                    throw new SQLException("account " + number + " not found");
                }
            }
        }
    }

    /**
     * Gets a list of all accounts.
     *
     * @return a list of accounts
     * @throws SQLException in case of DB problems
     */
    public static Collection<Account> getAccounts() throws SQLException {
        Collection<Account> userAccounts = new ArrayList<>();
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM accounts")
        ) {
            while (rs.next()) {
                User user = new User(rs.getString("name"));
                Account account = new Account(rs.getInt("number"), user);
                account.setBalance(rs.getBigDecimal("balance"));
                userAccounts.add(account);
            }
        }
        log.info("{} account(s) found.", userAccounts.size());
        return userAccounts;
    }

    /**
     * Gets the accounts for.
     *
     * @param user the user
     * @return the accounts for
     * @throws SQLException the sQL exception
     */
    public static Collection<Account> getAccountsFor(final User user) throws SQLException {
        Collection<Account> userAccounts = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM accounts WHERE name = ?")) {
            stmt.setString(1, user.getName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account(rs.getInt("number"), user);
                    account.setBalance(rs.getBigDecimal("balance"));
                    userAccounts.add(account);
                }
            }
            log.info("{} account(s) found for {}.", userAccounts.size(), user);
            return userAccounts;
        }
    }

    /**
     * Creates the account for.
     *
     * @param user the user
     * @return the account
     * @throws SQLException the sQL exception
     */
    public static synchronized Account createAccountFor(final User user) throws SQLException {
        Account account = createAccount();
        try (Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement("UPDATE accounts SET name = ? WHERE number = ?")) {
            stmt.setString(1, user.getName());
            stmt.setInt(2, account.getId());
            if (stmt.executeUpdate() < 1) {
                throw new SQLWarning("cannot create account for " + user);
            }
            return getAccount(account.getId());
        }
    }

    private static Account createAccount() throws SQLException {
        User nobody = new User("nobody");
        try (Connection connection = getConnection(); PreparedStatement stmt = connection
                .prepareStatement("INSERT INTO accounts (balance, name) VALUES (0.00, ?)")) {
            stmt.setString(1, nobody.getName());
            if (stmt.executeUpdate() < 1) {
                throw new SQLWarning("cannot create account for " + nobody);
            }
            return getAccountsFor(nobody).iterator().next();
        }
    }

    /**
     * Delete acount.
     *
     * @param account the account
     * @throws SQLException the SQL exception
     */
    public static void deleteAccount(final Account account) throws SQLException {
        try (Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement("DELETE FROM accounts WHERE number = ?")) {
            stmt.setInt(1, account.getId());
            if (stmt.executeUpdate() < 1) {
                throw new SQLWarning("cannot delete " + account);
            }
            log.info("{} was deleted.", account);
        }
    }

    /**
     * Save.
     *
     * @param account the account
     * @throws SQLException the SQL exception
     */
    public static void save(final Account account) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement stmt = connection
                .prepareStatement("UPDATE accounts SET balance = ? WHERE number = ?")) {
            stmt.setBigDecimal(1, account.getBalance());
            stmt.setInt(2, account.getId());
            if (stmt.executeUpdate() < 1) {
                throw new SQLWarning("cannot update " + account);
            }
            log.info("{} was saved.", account);
        }
    }

    /**
     * Transfer money from one account to another account.
     *
     * @param from the from
     * @param toNumber the to number
     * @param amount the amount
     * @throws SQLException the sQL exception
     */
    public static void transfer(final Account from, final int toNumber, final BigDecimal amount) throws SQLException {
        Account to = getAccount(toNumber);
        transfer(from, to, amount);
    }

    /**
     * Transfer money from one account to another account.
     * <p>
     * <em>NOTE</em>:
     * Remember, it is only a very simple demo. Normally we should put a
     * transaction bracket around the transfer logic.
     * </p>
     *
     * @param from the from
     * @param to the to
     * @param amount the amount
     * @throws SQLException the SQL exception
     */
    public static void transfer(final Account from, final Account to, final BigDecimal amount)
            throws SQLException {
        from.transfer(amount, to);
        save(from);
        save(to);
    }

}

