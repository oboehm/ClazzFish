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

import java.math.BigDecimal;

/**
 * A simple bank account.
 *
 * @author oboehm
 * @since 1.3.1 (16.07.2013)
 */
public final class Account {

    private final int id;
    private final User holder;
    private BigDecimal balance = new BigDecimal(0);

    /**
     * Instantiates a new account.
     *
     * @param id the account number
     * @param user the user
     */
    public Account(final int id, final User user) {
        this.id = id;
        this.holder = user;
    }

    /**
     * Gets the number.
     *
     * @return the number
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the holder.
     *
     * @return the holder
     */
    public User getHolder() {
        return this.holder;
    }

    /**
     * Sets the balance.
     *
     * @param balance the new balance
     */
    public void setBalance(final BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Gets the balance.
     *
     * @return the balance
     */
    public BigDecimal getBalance() {
        return this.balance;
    }

    /**
     * Pay in some money.
     *
     * @param amount the amount
     */
    public void deposit(final BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    /**
     * Deposit.
     *
     * @param amount the amount
     */
    public void deposit(final String amount) {
        this.deposit(new BigDecimal(amount));
    }

    /**
     * Pay off some money.
     *
     * @param amount the amount
     */
    public void debit(final BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Pay off some money.
     *
     * @param amount the amount
     */
    public void debit(final String amount) {
        this.debit(new BigDecimal(amount));
    }

    /**
     * Transfer an amount from one accout to another account.
     *
     * @param amount the amount
     * @param to the account where the amount is transferred to.
     */
    public void transfer(final BigDecimal amount, final Account to) {
        this.debit(amount);
        to.deposit(amount);
    }

    /**
     * Transfer an amount from one accout to another account.
     *
     * @param amount the amount
     * @param to the account where the amount is transferred to.
     */
    public void transfer(final String amount, final Account to) {
        this.debit(amount);
        to.deposit(amount);
    }

    /**
     * To string.
     *
     * @return the string
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "-" + this.id;
    }

}

