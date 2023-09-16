# VERA Dispatcher Server - Blocking or non-blocking API (DONE)

Created on: 2023-07-20

Decide whether the API of VERA Dispatcher Server is going to be blocking or non-blocking. This decision will also affect all the clients of VERA Dispatcher Server.

## Alternatives

### Blocking

*Pros*:

* Easy to write, debug and reason about.
* No need to use a third-party library.
* The lightweight threads coming in Java 21 could be used to improve performance even though the code is written in a blocking manner.
* Remote clients can interact with the server through a simple REST interface.

*Cons*:

* Not necessarily as performant or resource efficient as a non-blocking design.

### Non-blocking

*Pros*:

* More performant and resource efficient than a blocking design.
* A good opportunity to learn how to write non-blocking APIs.
* A non-blocking API can be turned into a blocking one, but not the other way around.

*Cons*:

* Not as easy to write, debug and reason about.
* Needs a third-party library such as [Project Reactor](https://projectreactor.io/) unless you want to use the primitives like `Flow` or `CompletableFuture` in `java.util.concurrent`.
* Remote clients need a non-blocking protocol to interact with the server, such as gRPC, web sockets or a message queue. This makes the application more complicated.

## Risks and Open Questions

* Changing this decision after any code has been written is going to be expensive so once the decision has been made, we have to stick to it.
* GraalVM may impose restrictions on what libraries you can use to write non-blocking code.
* If the database and other external services are blocking, it does not really matter if the rest of the application is non-blocking - there will still be a blocking bottleneck.
* Both Quarkus and Spring are going in the reactive direction, which means there is plenty of framework support for building reactive (non-blocking) applications out there.

## Decision

Decided on: 2023-09-16

**The API will be non-blocking.** The Java primitives for doing reactive programming are not enough, so a third-party library will be needed. This is a separate decision. When making a decision about which database to use, non-blocking support should be an important factor to consider.
