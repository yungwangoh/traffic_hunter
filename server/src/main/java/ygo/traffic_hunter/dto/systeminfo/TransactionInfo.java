package ygo.traffic_hunter.dto.systeminfo;

import java.time.Instant;
import ygo.traffic_hunter.dto.Metric;

public record TransactionInfo(
        String txName,
        Instant startTime,
        Instant endTime,
        long duration,
        String errorMessage,
        boolean isSuccess
) implements Metric {}
