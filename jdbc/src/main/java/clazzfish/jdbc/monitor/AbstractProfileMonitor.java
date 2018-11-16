/**
 * $Id: AbstractProfileMonitor.java,v 1.19 2016/12/18 20:19:36 oboehm Exp $
 *
 * Copyright (c) 2008 by Oliver Boehm
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
 * (c)reated 27.12.2008 by oliver (ob@oasd.de)
 */
package clazzfish.jdbc.monitor;

import clazzfish.monitor.util.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * The Class AbstractProfileMonitor.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 * @version $Revision: 1.19 $
 * @since 27.12.2008
 */
public abstract class AbstractProfileMonitor implements ProfileMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractProfileMonitor.class);

	/**
	 * The ProfileMonitor with the higher number of totals is considered as
	 * "greater".
	 *
	 * @param other
	 *            the other ProfileMonitor
	 * @return 0 if both ProfileMonitors has the same result and the same label
	 */
	@Override
	public final int compareTo(final ProfileMonitor other) {
		int diff = other.getHits() - this.getHits();
		if (diff == 0) {
			diff = this.getLabel().compareTo(other.getLabel());
		}
		if (this.getLabel().equals(other.getLabel())) {
			diff = 0;
		} else if (this.getTotal() > other.getTotal()) {
			diff = -1;
		} else if (this.getTotal() < other.getTotal()) {
			diff = 1;
		}
		LOG.trace("{} compared to {} returns {}.", this, other, diff);
		return diff;
	}

	/**
	 * Gets the last value as time string. It returns the same result as
	 * {@link #getLastValue()} but in a human readable format. The english
	 * locale is used for formatting because this method is normally used for
	 * logging (which should be normally done in English).
	 *
	 * @return the last time (e.g. "42 seconds")
	 * @since 1.4.2
	 */
	@Override
	public final String getLastTime() {
		double value = this.getLastValue();
		return Converter.getTimeAsString(value, Locale.ENGLISH);
	}

	/**
	 * If we impmlement the {@link Comparable#compareTo(Object)} method we
	 * should also implement/overwrite the {@link Object#equals(Object)} method.
	 * For the result of the equals method we use not the result of
	 * {@link Comparable#compareTo(Object)} but the label of the other object.
	 *
	 * @param obj
	 *            the other
	 * @return true, if successful
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof AbstractProfileMonitor)) {
			LOG.trace("{} is not an instance of AbstractProfileMonitor.", obj);
			return false;
		}
		AbstractProfileMonitor other = (AbstractProfileMonitor) obj;
		boolean ok = this.getLabel().equals(other.getLabel());
		LOG.trace("{} equals {} is {}.", this, other, ok);
		return ok;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getLabel().hashCode();
	}

}
