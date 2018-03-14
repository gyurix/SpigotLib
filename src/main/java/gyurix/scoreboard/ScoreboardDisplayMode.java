package gyurix.scoreboard;

import gyurix.protocol.Reflection;

import java.lang.reflect.Method;

public enum ScoreboardDisplayMode {
    INTEGER,
    HEARTS;
    private static Method valueOf;

    static {
        valueOf = Reflection.getMethod(Reflection.getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay"), "valueOf", String.class);
    }

    ScoreboardDisplayMode() {
    }

    public Object toNMS() {
        try {
            return valueOf.invoke(null, name());
        } catch (Throwable e) {
            return null;
        }
    }
}