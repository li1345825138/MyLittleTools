package org.mytool;

/**
 * Signals that the user's input does not satisfy a Tool's precondition, such as
 * a path that is not a directory or a directory holding no files of the
 * expected kind. Main maps it to exit code 2, the same code used for an unknown
 * option and for too few arguments.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
class UsageException extends RuntimeException {

    UsageException(String message) {
        super(message);
    }
}
