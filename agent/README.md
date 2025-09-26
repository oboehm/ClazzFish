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