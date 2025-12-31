# Notes to Self

Started to write this little journal around noon of December 30, 2025.

## Dec 30, 2025

- Reviewed dispatcher session handling code with ChatGPT:
    - Removed the virtual thread based `ScheduledJob` class and replaced it with a proper executor service.
    - Improved locks, serialization and overall code structure
    - Should now be robust
- I can't test back channel logout with my real Keycloak server as it can't reach my local machine.
  - Solved this with SSH port forwarding
- Back channel logout now works, but the client automatically authenticates again with the same token, and
  since it's still valid, it gets access. Need to use the introspection endpoint and also cache the invalidated SIDs.
