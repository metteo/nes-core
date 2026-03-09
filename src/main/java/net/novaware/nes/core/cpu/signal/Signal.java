package net.novaware.nes.core.cpu.signal;

public enum Signal {
    LOW,
    HIGH;

    private Signal not() {
        return this == LOW ? HIGH : LOW;
    }

    public static Signal not(Signal sinal) {
        return sinal.not();
    }
}
