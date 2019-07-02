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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 01.07.2019 by oboehm (ob@oasd.de)
 */
package clazzfish.sample.jdbc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link BankRepository}.
 */
class BankRepositoryTest {

    private static final User TOM = new User("Tom");
    private static final User JIM = new User("Jim");
    private static int tomsAccountNumber;
    private static int jimsAccountNumber;
    private Account tomsAccount;
    private Account jimsAccount;

    @BeforeAll
    public static void setUpRepository() throws SQLException {
        Account tomsAccount = getAccountFor(TOM);
        Account jimsAccount = getAccountFor(JIM);
        assertEquals(TOM, tomsAccount.getHolder());
        assertEquals(JIM, jimsAccount.getHolder());
        tomsAccountNumber = tomsAccount.getId();
        jimsAccountNumber = jimsAccount.getId();
    }

    private static Account getAccountFor(final User user) throws SQLException {
        Collection<Account> accounts = BankRepository.getAccountsFor(user);
        if (accounts.isEmpty()) {
            return BankRepository.createAccountFor(user);
        } else {
            return new ArrayList<Account>(accounts).get(0);
        }
    }

    /**
     * Get the accounts which we use for testing.
     *
     * @throws SQLException in case of DB problems
     */
    @BeforeEach
    public void setUpAccounts() throws SQLException {
        jimsAccount = BankRepository.getAccount(jimsAccountNumber);
        tomsAccount = BankRepository.getAccount(tomsAccountNumber);
    }

    @Test
    public void testGetAccount() throws SQLException {
        Account account = BankRepository.getAccount(jimsAccountNumber);
        assertEquals(jimsAccountNumber, account.getId());
        assertEquals(JIM, account.getHolder());
    }

}
