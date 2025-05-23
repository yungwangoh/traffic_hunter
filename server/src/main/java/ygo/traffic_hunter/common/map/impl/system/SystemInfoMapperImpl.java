/**
 * The MIT License
 *
 * Copyright (c) 2024 traffic-hunter.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ygo.traffic_hunter.common.map.impl.system;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ygo.traffic_hunter.common.map.SystemInfoMapper;
import ygo.traffic_hunter.core.dto.request.metadata.AgentMetadata;
import ygo.traffic_hunter.core.dto.request.metadata.MetadataWrapper;
import ygo.traffic_hunter.core.dto.request.systeminfo.SystemInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.cpu.CpuStatusInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.dbcp.HikariDbcpInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.gc.GarbageCollectionStatusInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.memory.MemoryStatusInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.memory.MemoryStatusInfo.MemoryUsage;
import ygo.traffic_hunter.core.dto.request.systeminfo.runtime.RuntimeStatusInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.thread.ThreadStatusInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.web.tomcat.TomcatWebServerInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.web.tomcat.request.TomcatRequestInfo;
import ygo.traffic_hunter.core.dto.request.systeminfo.web.tomcat.thread.TomcatThreadPoolInfo;
import ygo.traffic_hunter.core.dto.response.metric.SystemMetricResponse;
import ygo.traffic_hunter.core.repository.AgentRepository;
import ygo.traffic_hunter.domain.entity.Agent;
import ygo.traffic_hunter.domain.entity.MetricMeasurement;
import ygo.traffic_hunter.domain.metric.MetricData;
import ygo.traffic_hunter.domain.metric.cpu.CpuMetricMeasurement;
import ygo.traffic_hunter.domain.metric.dbcp.hikari.HikariCPMeasurement;
import ygo.traffic_hunter.domain.metric.gc.GCMetricMeasurement;
import ygo.traffic_hunter.domain.metric.gc.time.GCMetricCollectionTime;
import ygo.traffic_hunter.domain.metric.memory.MemoryMetricMeasurement;
import ygo.traffic_hunter.domain.metric.memory.usage.MemoryMetricUsage;
import ygo.traffic_hunter.domain.metric.runtime.RuntimeMetricMeasurement;
import ygo.traffic_hunter.domain.metric.thread.ThreadMetricMeasurement;
import ygo.traffic_hunter.domain.metric.web.tomcat.TomcatWebServerMeasurement;
import ygo.traffic_hunter.domain.metric.web.tomcat.request.TomcatWebServerRequestMeasurement;
import ygo.traffic_hunter.domain.metric.web.tomcat.thread.TomcatWebServerThreadPoolMeasurement;

/**
 * @author yungwang-o
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class SystemInfoMapperImpl implements SystemInfoMapper {

    private final AgentRepository agentRepository;

    @Override
    public MetricMeasurement map(final MetadataWrapper<SystemInfo> wrapper) {

        final AgentMetadata metadata = wrapper.metadata();

        final SystemInfo data = wrapper.data();

        final Agent agent = agentRepository.findByAgentId(metadata.agentId());

        return new MetricMeasurement(
                data.time(),
                agent.id(),
                getMetricData(data)
        );
    }

    @Deprecated
    @Override
    public SystemMetricResponse map(final MetricMeasurement measurement) {

        final Agent agent = agentRepository.findById(measurement.agentId());

        return new SystemMetricResponse(
                measurement.time(),
                agent.agentName(),
                agent.agentBootTime(),
                agent.agentVersion(),
                null
        );
    }

    private MetricData getMetricData(final SystemInfo data) {
        return new MetricData(
                mapToMeasurement(data.cpuStatusInfo()),
                mapToMeasurement(data.garbageCollectionStatusInfo()),
                mapToMeasurement(data.memoryStatusInfo()),
                mapToMeasurement(data.runtimeStatusInfo()),
                mapToMeasurement(data.threadStatusInfo()),
                mapToMeasurement(data.tomcatWebServerInfo()),
                mapToMeasurement(data.hikariDbcpInfo())
        );
    }

    private CpuMetricMeasurement mapToMeasurement(final CpuStatusInfo cpuStatusInfo) {
        return new CpuMetricMeasurement(
                cpuStatusInfo.systemCpuLoad(),
                cpuStatusInfo.processCpuLoad(),
                cpuStatusInfo.availableProcessors()
        );
    }

    private GCMetricMeasurement mapToMeasurement(final GarbageCollectionStatusInfo gcStatusInfo) {
        return new GCMetricMeasurement(getGcMetricCollectionTimes(gcStatusInfo));
    }

    private MemoryMetricMeasurement mapToMeasurement(final MemoryStatusInfo memoryStatusInfo) {
        return new MemoryMetricMeasurement(
                getMemoryUsage(memoryStatusInfo.heapMemoryUsage()),
                getMemoryUsage(memoryStatusInfo.nonHeapMemoryUsage())
        );
    }

    private RuntimeMetricMeasurement mapToMeasurement(final RuntimeStatusInfo runtimeStatusInfo) {
        return new RuntimeMetricMeasurement(
                runtimeStatusInfo.getStartTime(),
                runtimeStatusInfo.getUpTime(),
                runtimeStatusInfo.getVmName(),
                runtimeStatusInfo.getVmVersion()
        );
    }

    private ThreadMetricMeasurement mapToMeasurement(final ThreadStatusInfo threadStatusInfo) {
        return new ThreadMetricMeasurement(
                threadStatusInfo.threadCount(),
                threadStatusInfo.getPeekThreadCount(),
                threadStatusInfo.getTotalStartThreadCount()
        );
    }

    private TomcatWebServerMeasurement mapToMeasurement(final TomcatWebServerInfo tomcatWebServerInfo) {
        return new TomcatWebServerMeasurement(
                getTomcatWebServerRequestMeasurement(tomcatWebServerInfo.tomcatRequestInfo()),
                getTomcatThreadPoolMeasurement(tomcatWebServerInfo.tomcatThreadPoolInfo())
        );
    }

    private HikariCPMeasurement mapToMeasurement(final HikariDbcpInfo hikariDbcpInfo) {
        return new HikariCPMeasurement(
                hikariDbcpInfo.activeConnections(),
                hikariDbcpInfo.idleConnections(),
                hikariDbcpInfo.totalConnections(),
                hikariDbcpInfo.threadsAwaitingConnection()
        );
    }

    private TomcatWebServerRequestMeasurement getTomcatWebServerRequestMeasurement(
            final TomcatRequestInfo tomcatRequestInfo) {
        return new TomcatWebServerRequestMeasurement(
                tomcatRequestInfo.requestCount(),
                tomcatRequestInfo.bytesReceived(),
                tomcatRequestInfo.bytesSent(),
                tomcatRequestInfo.processingTime(),
                tomcatRequestInfo.errorCount()
        );
    }

    private TomcatWebServerThreadPoolMeasurement getTomcatThreadPoolMeasurement(
            final TomcatThreadPoolInfo tomcatThreadPoolInfo) {
        return new TomcatWebServerThreadPoolMeasurement(
                tomcatThreadPoolInfo.maxThreads(),
                tomcatThreadPoolInfo.currentThreads(),
                tomcatThreadPoolInfo.currentThreadsBusy()
        );
    }

    private MemoryMetricUsage getMemoryUsage(final MemoryUsage memoryUsage) {
        return new MemoryMetricUsage(
                memoryUsage.init(),
                memoryUsage.used(),
                memoryUsage.committed(),
                memoryUsage.max()
        );
    }

    private List<GCMetricCollectionTime> getGcMetricCollectionTimes(final GarbageCollectionStatusInfo gcStatusInfo) {
        return gcStatusInfo.garbageCollectionTimes().stream()
                .map(garbageCollectionTime -> new GCMetricCollectionTime(
                        garbageCollectionTime.getCollectionCount(),
                        garbageCollectionTime.getCollectionTime())
                )
                .toList();
    }
}
