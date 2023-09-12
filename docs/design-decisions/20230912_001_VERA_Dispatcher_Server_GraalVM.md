# VERA Dispatcher Server - GraalVM (PENDING)

Created on: 2023-09-12

Decide whether to use GraalVM and compile a native image of VERA Dispathcer Server or use an ordinary Java VM.

## Alternatives

### GraalVM

*Pros*:

* Compiles the application to a native binary, which means it will run faster and consume less memory.
* Supports JDK 20 and JDK 17.

*Cons*:

* The build process becomes more complicated.
* There are two versions of GraalVM - Oracle and Community Edition. They are both free but under different licenses.
* Does it make sense to use GraalVM for non-container deployment, e.g. when running directly on a Linux VM?
* There may be restrictions on how you write the code in order to make it work with GraalVM.

### Corretto

*Pros*:

* Build once, run everywhere, using the standard Java build process.
* Supports JDK 20 and JDK 17.
* Maintained by Amazon.
* [witchjdk.com](https://whichjdk.com/) recommends this, particularly if you run Java applications directly on Amazon Linux 2 in AWS.

*Cons*:

* Slower (but still good) performance.

### Temurin

*Pros*:

* Build once, run everywhere, using the standard Java build process.
* Supports JDK 20 and JDK 17.
* Maintained by the Adoptium Working Group, which includes companies such as Red HAt, IBM, Microsoft, Azul and the iJUG.
* [witchjdk.com](https://whichjdk.com/) higly recommends this.

*Cons*:

* Slower (but still good) performance.

## Risks and Open Questions

* Switching from GraalVM to a JVM will be trivial, so there is no technical risk in aiming for GraalVM. Switching to it later may be more difficult.
* Switching between Temurin and Corretto will not require any changes to the code, only to the build process (to pick the correct VM).
* GraalVM is a new technology that hasn't (to my knowledge) been tested too much in production yet.
* A GraalVM build would have to be included in the build process form the start, to make sure the code works. This probably means that Docker would have to be involved, and the target operating system would be Linux.
* I don't trust Oracle's licensing.

## Decision

No decision has been made yet.