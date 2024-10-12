package ygo.traffichunter.engine.systeminfo.thread;

import java.lang.management.ThreadInfo;

public record ThreadStatusInfo(
        int threadCount,
        int getPeekThreadCount,
        long getTotalStartThreadCount
) {
}
