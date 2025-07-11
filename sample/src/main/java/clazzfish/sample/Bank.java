/*
 * Copyright (c) 2024 by Oli B.
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
 * (c)reated 04.12.24 by oboehm
 */
package clazzfish.sample;

import clazzfish.monitor.Starter;
import clazzfish.sample.jdbc.Account;
import clazzfish.sample.jdbc.BankRepository;
import clazzfish.sample.jdbc.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Scanner;

/**
 * The Bank class has a simple main method so that you can run it and start
 * the 'jconsole' (or another JMX console) to access the different MBeans
 * for monitoring and statistics.
 *
 * @author oboehm
 * @since 2.4 (04.12.24)
 */
public class Bank {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    static {
        // log and store SQL statements at the end
        //JdbcStarter.recordAll(new File("target", "statistic").toURI());
        //JdbcStarter.recordAll();
        Starter.record(new File("target", "statistic").toURI());
        //Starter.record(URI.create("ssh://git@github.com/oboehm/ClazzFishTest.git"));
        //Starter.record(URI.create("print://localhost"));
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nWhat do you want to do?\n");
            System.out.println("1. Create bank");
            System.out.println("2. Create account");
            System.out.println("3. List accounts");
            System.out.println("9. Quit");
            System.out.print("\nEnter your choice: ");
            System.out.flush();
            Scanner console = new Scanner(System.in);
            String choice = console.nextLine().trim();
            switch (choice) {
                case "1":
                    createBank();
                    break;
                case "2":
                    createAccount();
                    break;
                case "3":
                    listAccounts();
                    break;
            }
            if (choice.equals("9")) {
                break;
            }
        }
        log.info("Good bye - shutting down...");
    }

    private static void createBank() {
        try {
            BankRepository.setUpDB();
            log.info("Bank created.");
        } catch (SQLException ex) {
            log.error("Bank cannot be created:", ex);
        }
    }

    private static void createAccount() {
        System.out.print("\n2. Create account - enter user: ");
        System.out.flush();
        Scanner console = new Scanner(System.in);
        String name = console.nextLine().trim();
        User user = new User(name);
        try {
            BankRepository.createAccountFor(user);
            log.info("User '{}' created.", user);
        } catch (SQLException ex) {
            log.error("User '{}' cannot be created:", user, ex);
        }
    }

    private static void listAccounts() {
        System.out.println("\n3. List accounts\n");
        try {
            Collection<Account> accounts = BankRepository.getAccounts();
            for (Account a : accounts) {
                System.out.println(a);
            }
        } catch (SQLException ex) {
            log.error("Cannot get accounts:", ex);
        }
    }

}
