# Notes to Self

Started to write this little journal in the evening of December 27, 2025.

## Dec 29, 2025

- Improvements to web socket client state management.
- Implemented initial server-side authentication and session management.
  - Authentication happens with an AUTH message.
  - If it does not arrive in 5 seconds, the server terminates the session.
- Implemented server initiated pings (seem to keep the connection alive).
- Added context path to server.

Things to do:
- [ ] Server: Implement support for back channel logouts
- [ ] Server: Verify tokens with the introspection endpoint upon initial authentication


## Dec 28, 2025

- Roles are available in the access token and can be checked through the `Keycloak` instance.
- Locale is added to `userInfo` if is actually set to a value other than default.
- Session info was being posted with the built-in `postMessage` function and not the custom `post` function. After fixing that, things started to work.
- Event handlers need to be declared using the syntax `private onXyx = (evt) => {}`. If you use a function, you have to use `bind(this)`.
- Using `<dialog>` to show error messages that prevent the application from functioning at all.
- Implemented initial web socket connection handling with random back-off for reconnects.

Things to do:
- [ ] Add Keycloak error handlers to the launcher
- [ ] Add proper context paths to all servers (dispatcher, server, gis) so that they all can run behind the same proxy
- [X] Implement server-side authentication of clients (how to pass the secret without logging it?)
- [X] Find out why the websocket is automatically closed after some time (I assume this is by design and some keep alive traffic is needed)
- [X] How to keep the browser tabs alive on both screens for hours?
- [X] Add more intermediate connection states (e.g. when the client is waiting to reconnect, authenticating, etc.)
- [ ] Access denied => end the session globally (both windows + launcher)

## Dec 27, 2025

- Added preliminary Keycloak support
- Started to work on the server-side websocket messages (nothing committed yet)

Things to do:
- [X] Figure out how to get locale and roles into the `userInfo` token
- [X] Build an API for receiving and accessing the updated session info from the launcher, and showing the user name in the UI
- [X] Add support for destroying the session
