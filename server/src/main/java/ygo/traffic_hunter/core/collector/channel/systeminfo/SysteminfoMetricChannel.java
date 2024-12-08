package ygo.traffic_hunter.core.collector.channel.systeminfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ygo.traffic_hunter.core.collector.channel.MetricChannel;
import ygo.traffic_hunter.core.collector.processor.MetricProcessor;
import ygo.traffic_hunter.core.dto.request.metadata.MetadataWrapper;
import ygo.traffic_hunter.core.dto.request.systeminfo.SystemInfo;
import ygo.traffic_hunter.core.event.channel.SystemInfoMetricEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SysteminfoMetricChannel implements MetricChannel {

    private final MetricProcessor processor;

    private final ApplicationEventPublisher publisher;

    @Override
    public MetricHeaderSpec getHeaderSpec() {
        return MetricHeaderSpec.SYSTEM;
    }

    @Override
    public void open(final byte[] payload) {

        MetadataWrapper<SystemInfo> object = processor.processSystemInfo(payload);

        log.info("process system info: {}", object);

        publisher.publishEvent(new SystemInfoMetricEvent(object));
    }
}
