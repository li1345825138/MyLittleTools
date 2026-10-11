# CLI exit codes

Commands report three outcomes through the process exit code: `0` for success — including an explicit help request and a bare invocation with no arguments — `2` for a usage error (unknown option name, too few arguments, or a directory with no matching files), and `1` for an internal failure (I/O errors, unreadable files).

We did not follow the `diff` convention, where `1` means "the two things differ" and `2` means trouble, because `1` and `2` were already taken by internal failure and usage error. So `-hash` exits `0` even when the two files differ; it reports the comparison in its output instead.
