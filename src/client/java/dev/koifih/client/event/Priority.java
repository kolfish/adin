package dev.koifih.client.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Priority {
    public static final int HIGHEST = 200;
    public static final int HIGH = 100;
    public static final int NORMAL = 0;
    public static final int LOW = -100;
    public static final int LOWEST = -200;
}
