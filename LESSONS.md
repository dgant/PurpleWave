# Packaging Lessons

- Never create a ZIP inside the directory being archived. Doing so can include the output archive in itself and corrupt its central directory.
- Verify tournament packages with a standard ZIP reader and compare the archived JAR hash against the deployment source before distributing them.

