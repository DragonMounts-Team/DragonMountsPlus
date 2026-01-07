package net.dragonmounts.neo.common.util;

import static net.minecraft.SharedConstants.TICKS_PER_GAME_DAY;

public class TimeUtil {
    public static final int TICKS_PER_GAME_HOUR = TICKS_PER_GAME_DAY / 24;

    /**
     * @param value raw time (in ticks)
     * @return formatted time (in seconds)
     */
    public static String formatAsFloat(int value) {
        if (value < 19) return "0." + ((value + 1) >> 1);
        StringBuilder builder = new StringBuilder().append((value + 1) >> 1);//value: ticks
        builder.append(builder.charAt(value = builder.length() - 1)).setCharAt(value, '.');//value: index
        return builder.toString();
    }
}
