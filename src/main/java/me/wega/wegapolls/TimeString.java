package me.wega.wegapolls;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public final class TimeString {
    private static final Pattern PATTERN = Pattern.compile("(\\d+)\\s*([a-zA-Z]+)");
    private static final Map<String, Long> UNITS = new LinkedHashMap<>();

    static {
        long yr = TimeUnit.DAYS.toMillis(365), mo = TimeUnit.DAYS.toMillis(30),
                wk = TimeUnit.DAYS.toMillis(7), dy = TimeUnit.DAYS.toMillis(1),
                hr = TimeUnit.HOURS.toMillis(1), mn = TimeUnit.MINUTES.toMillis(1),
                sc = TimeUnit.SECONDS.toMillis(1);
        for (String k : new String[]{"years", "year", "yrs", "yr", "y"}) UNITS.put(k, yr);
        for (String k : new String[]{"months", "month", "mo", "M"}) UNITS.put(k, mo);
        for (String k : new String[]{"weeks", "week", "w"}) UNITS.put(k, wk);
        for (String k : new String[]{"days", "day", "d"}) UNITS.put(k, dy);
        for (String k : new String[]{"hours", "hour", "hrs", "hr", "h"}) UNITS.put(k, hr);
        for (String k : new String[]{"minutes", "minute", "mins", "min", "m"}) UNITS.put(k, mn);
        for (String k : new String[]{"seconds", "second", "secs", "sec", "s"}) UNITS.put(k, sc);
    }

    private final String string;
    private final long timeMillis;

    public TimeString(@NotNull String string) {
        this.string = string;
        this.timeMillis = parse(string);
    }

    private static long parse(@NotNull String input) {
        String s = input.replaceAll("\\s+", "").trim();
        if (s.isBlank()) throw new IllegalArgumentException("Input cannot be blank");

        Matcher m = PATTERN.matcher(s);
        long total = 0;
        int lastEnd = 0;

        while (m.find()) {
            if (m.start() != lastEnd) throw new IllegalArgumentException("Invalid time string format");
            String unit = m.group(2);
            Long mul = UNITS.getOrDefault(unit, UNITS.get(unit.toLowerCase()));
            if (mul == null) throw new IllegalArgumentException("Unknown time unit: " + unit);
            total += Long.parseLong(m.group(1)) * mul;
            lastEnd = m.end();
        }

        if (lastEnd != s.length()) throw new IllegalArgumentException("Invalid trailing content in time string");
        return total;
    }
}
