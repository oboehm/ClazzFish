# Agent

This module contains the source code of the Java agent for ClazzFish.
The agent can be started with

    java -javaagent:path/to/clazzfish-agent-2.8.jar ...

The agent provides the information which classes were loaded by the classloader.


## Dependencies

To keep the agent small it has no dependencies to other libraries.
Logging is done with the logging package of the JDK.