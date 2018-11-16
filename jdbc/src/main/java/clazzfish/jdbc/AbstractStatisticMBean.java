/*
 * $Id: AbstractStatisticMBean.java,v 1.8 2016/12/18 20:19:36 oboehm Exp $
 *
 * Copyright (c) 2009 by Oliver Boehm
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
 */
package clazzfish.jdbc;

import clazzfish.monitor.jmx.Description;
import clazzfish.monitor.jmx.Unit;

import javax.management.openmbean.TabularData;

/**
 * The Interface AbstractStatisticMBean.
 */
@Description("Profile statistic for different methods")
public interface AbstractStatisticMBean {

	/**
	 * Reset.
	 */
	@Description("reset the statistic to be ready for new measurement")
	void reset();

	/**
	 * Gets the max hits.
	 *
	 * @return the max hits
	 */
	@Description("maximal number how often a method is called")
	int getMaxHits();

	/**
	 * Gets the max hits label.
	 *
	 * @return the max hits label
	 */
	@Description("the name of the method which was called most")
	String getMaxHitsLabel();

	/**
	 * Gets the max hits statistic.
	 *
	 * @return the max hits statistic
	 */
	@Description("statistic of the most called method")
	String getMaxHitsStatistic();

	/**
	 * Gets the max total.
	 *
	 * @return the max total
	 */
	@Description("maximal total time of a method")
	@Unit("milliseconds")
	double getMaxTotal();

	/**
	 * Gets the max total label.
	 *
	 * @return the max total label
	 */
	@Description("the name of the method which need most of the time")
	String getMaxTotalLabel();

	/**
	 * Gets the max total statistic.
	 *
	 * @return the max total statistic
	 */
	@Description("statistic of the method which need most of the time")
	String getMaxTotalStatistic();

	/**
	 * Gets the max avg.
	 *
	 * @return the max avg
	 */
	@Description("maximal average time")
	@Unit("milliseconds")
	double getMaxAvg();

	/**
	 * Gets the max avg label.
	 *
	 * @return the max avg label
	 */
	@Description("the name of the method with the maximal average time")
	String getMaxAvgLabel();

	/**
	 * Gets the max avg statistic.
	 *
	 * @return the max avg statistic
	 */
	@Description("statistic of the method with the maximal average time")
	String getMaxAvgStatistic();

	/**
	 * Gets the max max.
	 *
	 * @return the max max
	 */
	@Description("maximal time of a method")
	@Unit("milliseconds")
	double getMaxMax();

	/**
	 * Gets the max max label.
	 *
	 * @return the max max label
	 */
	@Description("the name of the method with the maximal time of a single run")
	String getMaxMaxLabel();

	/**
	 * Gets the max max statistic.
	 *
	 * @return the max max statistic
	 */
	@Description("statistic of the method with the maximal time of a single run")
	String getMaxMaxStatistic();

	/**
	 * Sets the maximal size of statistic entries.
	 *
	 * @param size
	 *            the new max size
	 * @since 1.6
	 */
	@Description("set maximal number of statistic entries")
	void setMaxSize(int size);

	/**
	 * Gets the max size.
	 *
	 * @return the max size
	 * @since 1.6
	 */
	@Description("get maximal number of statistic entries")
	int getMaxSize();

	/**
	 * Gets the statistics.
	 *
	 * @return the statistics
	 */
	@Description("the statistic table")
	TabularData getStatistics();

}
