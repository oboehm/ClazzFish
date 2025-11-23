# Agent

This module contains the source code of the Java agent for ClazzFish.
The agent can be started with

    java -javaagent:path/to/clazzfish-agent-2.8.jar ...

The agent provides the information which classes were loaded by the classloader.



## Design Decisions

To keep the agent small it was decided to

- use no other dependencies
- keep the memory foot print small

Because of the use of no other dependencies logging is done with the logging package of the JDK.



## Dead Class Detection

With v3 of the agent it is possible to record a statistic of loaded and unloaded classes to a CSV file.
This allows you to detect [dead classes](../src/doc/DeadClasses.adoc).

You can also record to other targets like a GIT repository (see e.g. [ClazzFishTest](https://github.com/oboehm/ClazzFishTest)).
In this case you must use the [monitor module](../monitor/README.md).
The agent module does not support it to avoid dependencies to external libraries and to keep the size small.
With v3 the size of the agent increased to 53 kB (before: 8 kB).