/*
 * Copyright (c) 2010-2018 by Oliver Boehm
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
 * (c)reated 18.01.2010 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.jmx;

import java.lang.annotation.*;

/**
 * With this annotation you can add a description to a MBean. This description
 * will than appear in the 'jconsole' as DescriptorKey. Originally this
 * annotation was part of the PatternTesting project.
 *
 * @author oliver
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

	/**
	 * The descripiton of a MBean, attribute or operation.
	 *
	 * @return the description
	 */
	String value();

}
