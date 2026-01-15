package net.novaware.nes.core.file;

public record Problem(Severity severity, String message) {

    // NOTE: consider following additional properties:
    //       - Domain
    //       - Code
    //       - Text instead of message
    //       - Source
    //       - Parameters list (for translation)
    //       - Cause (recursive)
    //       - Suppressed (recursive list)

    public enum Severity {
        MINOR,
        MAJOR
    }
}
