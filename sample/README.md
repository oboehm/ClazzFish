# Sample

Here in this module you find some exsample how to monitor the classpath and SQL statements.


## [BankRepositoryTest](src/test/java/clazzfish/sample/jdbc/BankRepositoryTest.java)

Start the unit tests in the [src/test](src/test) directory.
You should see the executed statements in the log output:

    ...
    16:10:50 DEBUG [main|clazzfish.jdbc.SqlStatistic] "CREATE TABLE accounts (number INTEGER IDENTITY PRIMARY KEY, balance DECIMAL(10,2), name VARCHAR(50))" returned with 0 after 1 ms.
    16:10:50 DEBUG [main|clazzfish.jdbc.SqlStatistic] "SELECT * FROM accounts WHERE name = 'Tom'" returned with open JDBCResultSet after 21 ms.
    16:10:50  INFO [main|sample.jdbc.BankRepository ] 0 account(s) found for Tom.
    16:10:50 DEBUG [main|clazzfish.jdbc.SqlStatistic] "INSERT INTO accounts (balance, name) VALUES (0.00, 'nobody')" returned with 1 after 1 ms.
    16:10:50 DEBUG [main|clazzfish.jdbc.SqlStatistic] "SELECT * FROM accounts WHERE name = 'nobody'" returned with open JDBCResultSet after 1 ms.
    16:10:50  INFO [main|sample.jdbc.BankRepository ] 1 account(s) found for nobody.
    16:10:50 DEBUG [main|clazzfish.jdbc.SqlStatistic] "UPDATE accounts SET name = 'Tom' WHERE number = 0" returned with 1 after 3 ms.
    16:10:50 DEBUG [main|clazzfish.jdbc.SqlStatistic] "SELECT balance, name FROM accounts where number = 0" returned with true after 1 ms.
    16:10:50 DEBUG [main|clazzfish.jdbc.SqlStatistic] "SELECT * FROM accounts WHERE name = 'Jim'" returned with open JDBCResultSet after 1 ms.
    16:10:50  INFO [main|sample.jdbc.BankRepository ] 0 account(s) found for Jim.
    ...


## [Bank](src/main/java/clazzfish/sample/Bank.java)

The [Bank](src/main/java/clazzfish/sample/Bank.java) class has a main method and you can start it from terminal or within you IDE.
You should then see a little menu you can play with.

If you start a JMX console like `jconsole` from your JDK you can access the different MBeans like

* ClasspathMonitor
* ResourcepathMonitor
* ClazzStatistic
* ConnectionMonitor
* SqlStatistic



