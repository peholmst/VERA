# Notes to Self

Started to write this little journal around noon of December 30, 2025.

## Dec 30, 2025

- Reviewed dispatcher session handling code with ChatGPT:
    - Removed the virtual thread based `ScheduledJob` class and replaced it with a proper executor service.
    - Improved locks, serialization and overall code structure
    - Should now be robust