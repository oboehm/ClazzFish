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

import org.apache.commons.lang3.StringUtils;


/**
 * A simple user class.
 *
 * @author oliver
 * @since 1.0 (01.07.2019)
 */
public final class User {

    private String name;

    /**
     * Instantiates a new user.
     *
     * @param name the name
     */
    public User(final String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    @Deprecated
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * To string.
     *
     * @return the string
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Hash code.
     *
     * @return the int
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (name == null) ? 0 : name.hashCode();
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        return StringUtils.equals(this.name, other.name);
    }

}

