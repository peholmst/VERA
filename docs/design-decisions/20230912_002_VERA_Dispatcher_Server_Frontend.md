# VERA Dispatcher Server - Frontend (PENDING)

Created on: 2023-09-12

Decide which technology to use for implementing the frontend for VERA Dispatcher Server. This is the application that will be used by dispatchers to dispatch units and to show the real-time positions of units and incidents.

## Alternatives

### Web: React and Hilla

*Pros*:

* Web application, no need to distribute updates.
* Can be stateless, good for high availability.
* A good opportunity for me to learn React.
* There are open source map components that can be used more or less out of the box.

*Cons*:

* Multi-monitor mode can be difficult, but not impossible, to implement.
* Would introduce a new programming language (TypeScript) into the mix.

### Web: Vaadin Flow

*Pros*:

* Web application, no need to distribute updates.
* Multi-monitor mode is easier to implement compared to React.
* No learning curve; I already know this.

*Cons*:

* Flow applications are stateful, which means achieveing high availability is more difficult.
* Map component is commercial, so would have to write my own wrapper for an open source JS map component.

### Desktop: Java FX

*Pros*:

* Desktop application that makes the server stateless, good for high availabilty.
* Familiar Java ecosystem and development environment.
* Supports Windows, macOS and Linux.
* Multi-monitor mode is easy to implement.

*Cons*:

* Requires a mechanism for distributing updates.
* Future of Java FX is unclear; it does not seem to be used that much.
* Will likely need to implement a map component from scratch.

### Desktop: .NET MAUI

*Pros*:

* Desktop application that makes the server stateless, good for high availability.
* Multi-monitor mode is easy to implement.
* Supports Windows, macOS, iOS and Android.
* There will be a need to build native mobile apps for VERA in the future.

*Cons*:

* Requires a mechanism for distributing updates.
* Would introduce a new programming language (C#) and development environment (Visual Studio) into the mix.
* I have no experience whatsoever with this technology. I don't know what you can and can't do.
* Will likely need to implement a map component from scratch.

## Risks and Open Questions

To be written.

## Decision

No decision has been made yet.