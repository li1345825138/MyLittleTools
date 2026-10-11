package org.mytool;

import java.nio.file.Path;

/**
 * Reports what a Tool is doing while it works. Tests pass their own
 * implementation to capture the steps instead of printing them.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
interface Progress {

    void reading(Path file);

    void writing(Path file);

    void done();

    /**
     * @return a Progress that narrates each step on standard output
     */
    static Progress console() {
        return new Progress() {
            @Override
            public void reading(Path file) {
                System.out.println("[+] Reading: " + file);
            }

            @Override
            public void writing(Path file) {
                System.out.println("[+] Writing: " + file);
            }

            @Override
            public void done() {
                // One line per step already reads clearly on a console.
            }
        };
    }
}
