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
 * (c)reated 19.02.25 by oboehm
 */
package clazzfish.monitor.spi;

/**
 * Die Klasse FileXPorterProvider ...
 *
 * @author oboehm
 * @since x.x (19.02.25)
 */
public class FileXPorterProvider implements CsvXPorterProvider {

    @Override
    public CsvXPorter create() {
        return new FileXPorter();
    }

    @Override
    public boolean supports(String protocol) {
        return "file".equalsIgnoreCase(protocol);
    }

}
