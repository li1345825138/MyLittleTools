# MyLittleTools

A collection of small command-line tools. Each tool does one independent thing, and the command line is the only way in.

## Language

**Tool**:
One independent thing the program can do — merge PDFs, convert WEBP to JPG, generate a password. Every Tool is reachable through exactly one Option.
_Avoid_: command, operation, feature

**Option**:
The way a user selects a Tool on the command line: a name (e.g. `-mi`) plus the number of arguments that Tool expects.
_Avoid_: flag, switch, command
