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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Starter.start();
    }

    public static void main(String[] args) {
        while (true) {
            try {
                System.out.println("\nWhat do you want to do?\n");
                System.out.println("1. Create bank");
                System.out.println("2. Create account");
                System.out.println("3. Delete account");
                System.out.println("4. List accounts");
                System.out.println("9. Quit");
                Thread.sleep(500L);
                System.out.print("\nEnter your choice: ");
                System.out.flush();
                Scanner console = new Scanner(System.in);
                String choice = console.nextLine().trim();
                if (choice.equals("9")) {
                    break;
                }
            } catch (InterruptedException ex) {
                log.warn("Ups, interrupted: ", ex);
            }
        }
    }

}
