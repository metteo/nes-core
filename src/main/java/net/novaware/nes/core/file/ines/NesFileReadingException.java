package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.Problem;

import java.util.List;

public class NesFileReadingException extends RuntimeException {

    private final List<Problem> problems;

    public NesFileReadingException(String message, List<Problem> problems) {
        super(message);

        this.problems = roCopy(problems);
    }

    public NesFileReadingException(String message, Throwable cause, List<Problem> problems) {
        super(message, cause);

        this.problems = roCopy(problems);
    }

    public NesFileReadingException(Throwable cause, List<Problem> problems) {
        super(cause);

        this.problems = roCopy(problems);
    }

    public NesFileReadingException(List<Problem> problems) {
        this.problems = roCopy(problems);
    }

    public NesFileReadingException(String message) {
        this(message, List.of());
    }

    public NesFileReadingException(String message, Throwable cause) {
        this(message, cause, List.of());
    }

    public NesFileReadingException(Throwable cause) {
        this(cause, List.of());
    }

    public NesFileReadingException() {
        this(List.of());
    }

    public List<Problem> getProblems() {
        return problems;
    }

    private static List<Problem> roCopy(List<Problem> problems) {
        return problems.stream().toList();
    }
}
