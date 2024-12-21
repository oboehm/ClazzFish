# Class Monitoring


The main goal of the monitor module is

* to find dead classes
* to find problematic classes


## Setup

To start the monitor classes just call the main method in the Starter class:

     clazzfish.monitor.Starter.start()

But before you start your application add clazzfish-agent as Java agent to your VM args, e.g.

     java -javaagent:${user.home}/.m2/repository/de/aosd/clazzfish/clazzfish-agent/2.2/clazzfish-agent-2.2.jar ...

This is the recommend way because it uses the official API to get the internals of class loading.
If the agent is started and the library loaded you can use the `jconsole` to examine the loaded classes and resources.

If you use an VM based on the OpenJDK (which are the most VMs) you can try it without this agent.
In this case the monitor classes uses an alternative MBean of the JDK to receive the needed infos.



## How to find dead classes

To find unused classes you can use the [ClasspathMonitor](src/main/java/clazzfish/monitor/ClasspathMonitor.java) which can be accessed via [JMX](https://en.wikipedia.org/wiki/Java_Management_Extensions).
If the [ClasspathMonitor](src/main/java/clazzfish/monitor/ClasspathMonitor.java) is registered as shutdown hook it writes the unused classes and other infos together with [CpMonREADME](src/main/resources/clazzfish/monitor/CpMonREADME.txt) into the temp directory.

The classes listed in `UnusedClasses.txt` are the classes which are never loaded during the run of your application.
Can they be deleted?
Perhaps.
It may be that one of the classes are loaded if you start other workflows in your application or use other pathes.
You must collect all this "snaphshots" over a long time of period to be sure that a class is never loaded.
This is where the [ClazzStatistic](src/main/java/clazzfish/monitor/stat/ClazzStatistic.java) came into.
Since 2.3 it imports the statistics from the last run, enriches it with the actual run and exports it at the end of the program run into the same statistics file as CSV.

Usually this statistics is stored in the temp directory (${java.io.tmpdir}) in the file `ClazzFish/.../ClazzStatistic.csv` as CSV. It looks like

| URI                                    | class                                   | count |
|----------------------------------------|-----------------------------------------|-------|
| file:/ClazzFish/monitor/target/classes | clazzfish.monitor.ClasspathMonitor      | 2     |
| file:/ClazzFish/monitor/target/classes | clazzfish.monitor.ClasspathMonitorMBean | 2     | 
| file:/ClazzFish/monitor/target/classes | clazzfish.monitor.DeadClassSample       | 0     |

The first 2 classes/interfaces in this example are loaded 2 times whereas DeadClassSample is never loaded and is likely to be a dead class.

If you want the statistics file in another file or directory you can use the 2 system properties to set it:

* clazzfish.dump.dir
* clazzfish.statistics.file

For more info see the [ClazzStatistic](src/main/java/clazzfish/monitor/stat/ClazzStatistic.java) class.


## Troublehooting

To see what happens (e.g. where the statistics or monitor files are stored) set the log level to DEBUG or TRACE.
