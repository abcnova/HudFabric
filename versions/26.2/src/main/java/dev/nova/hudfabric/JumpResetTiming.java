package dev.nova.hudfabric;

public final class JumpResetTiming {
    private JumpResetTiming() {}
    public enum Result { PERFECT, EARLY, LATE, MISSED }
    public static Result classify(int milliseconds) {
        if (milliseconds < 0) return Result.EARLY;
        if (milliseconds <= 80) return Result.PERFECT;
        if (milliseconds <= 200) return Result.LATE;
        return Result.MISSED;
    }
}
