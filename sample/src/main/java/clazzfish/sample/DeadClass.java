/*
 * Copyright (c) 2025 by Oli B.
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
 * (c)reated 19.01.25 by oboehm
 */
package clazzfish.sample;

/**
 * This class is an example of a class which is never loaded.1
 *
 * @author oboehm
 * @since 2.5 (19.01.25)
 */
abstract class DeadClass {

    static {
        System.err.println("You should never see this message!");
        System.err.println("You should never see this result:" + (1 / 0));
    }

}
