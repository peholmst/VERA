# VERA Dispatcher Server - Programming Language (DONE)

Documented on: 2023-07-20

Decide which programming language to use for VERA Dispatcher Server. The server must be able to run on a Linux-server or inside a Linux docker container.

## Alternatives

### Java 20+

*Pros*:

* No learning curve, possibly with the exception of figuring out how to utilize the latest Java language features.
* Lots of libraries and frameworks to choose from.
* Fast development.

*Cons*:

* May not be as fast and resource efficient as a native binary, although this could be addressed with [GraalVM](https://www.graalvm.org/).
* No learning of a new programming language.

### Rust

*Pros*:

* An opportunity to learn a new programming language and programming paradigm.
* Fast, resource efficient executable.

*Cons*:

* Need to learn a completely new programming language and paradigm. An application such as VERA is probably not the best program to start with.
* Not sure what kind of libraries and frameworks there are to choose from, nor how stable they are.
* Slow development.

## Decision

**VERA Dispatcher Server is written in Java.** This project has been hanging in the air for too long now and it is time to get something done. There will be more than enough learning opportunities with regards to the actual design of the application and throwing in a new programming language "just because" is probably going to have a negative impact on the overall learning. There will be other opportunities to tinker with other programming languages later.

That said, no decisions have been made with regards to libraries or frameworks yet. For example, GraalVM may impose restrictions on what you can and can't do. The majority of the code should be written in a framework agnostic manner.