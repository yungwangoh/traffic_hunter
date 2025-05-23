/**
 * The MIT License
 * <p>
 * Copyright (c) 2024 traffic-hunter.org
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ygo.traffic_hunter.core.dto.response;

import java.time.Instant;
import java.util.List;
import ygo.traffic_hunter.core.dto.response.metric.SystemMetricResponse;

/**
 * @author JuSeong
 * @version 1.1.0
 */
public record RealTimeMonitoringResponse(String agentName,
                                         Instant agentBootTime,
                                         String agentVersion,
                                         List<SystemMetricResponse> systemMetricResponses
) {

    public static RealTimeMonitoringResponse create(List<SystemMetricResponse> systemMetricResponses) {

        if (systemMetricResponses.isEmpty()) {
            return null;
        }

        SystemMetricResponse systemMetricResponse = systemMetricResponses.getFirst();
        String agentName = systemMetricResponse.agentName();
        Instant agentBootTime = systemMetricResponse.agentBootTime();
        String agentVersion = systemMetricResponse.agentVersion();
        return new RealTimeMonitoringResponse(agentName, agentBootTime, agentVersion, systemMetricResponses);
    }
}
