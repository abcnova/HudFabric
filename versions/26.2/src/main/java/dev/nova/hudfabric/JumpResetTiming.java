package dev.nova.hudfabric;

public final class JumpResetTiming {
    private JumpResetTiming() {}
    public enum Result { PERFECT, EARLY, LATE, MISSED }
    public static Result classify(int milliseconds) {
        if (milliseconds < 0) return Result.EARLY;
        if (milliseconds <= 25) return Result.PERFECT;
        if (milliseconds <= 250) return Result.LATE;
        return Result.MISSED;
    }
}
