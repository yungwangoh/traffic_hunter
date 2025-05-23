/*
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
package org.traffichunter.javaagent.extension;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.traffichunter.javaagent.bootstrap.Configurations;
import org.traffichunter.javaagent.bootstrap.Configurations.ConfigProperty;
import org.traffichunter.javaagent.extension.exporter.thunter.TrafficHunterLogExporter;
import org.traffichunter.javaagent.extension.exporter.thunter.TrafficHunterSpanExporter;
import org.traffichunter.javaagent.extension.exporter.zipkin.ZipkinSpanExportDelegator;
import org.traffichunter.javaagent.extension.metadata.AgentMetadata;
import org.traffichunter.javaagent.websocket.TrafficHunterWebsocketClient;

/**
 * @author yungwang-o
 * @version 1.1.0
 */
final class OpenTelemetryManager {

    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

    private static final String endpoint = Configurations.export(ConfigProperty.ZIPKIN_EXPORTER_ENDPOINT);

    private OpenTelemetryManager() {}

    static OpenTelemetrySdk manageOpenTelemetrySdk(final String serviceName,
                                                   final TrafficHunterWebsocketClient client,
                                                   final AgentMetadata metadata) {

        SdkTracerProvider tracerProvider = createSdkTracerProvider(serviceName, client, metadata);

        SdkLoggerProvider loggerProvider = createSdkLoggerProvider(serviceName, client, metadata);

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setLoggerProvider(loggerProvider)
                .buildAndRegisterGlobal();
    }

    private static SdkTracerProvider createSdkTracerProvider(final String serviceName,
                                                             final TrafficHunterWebsocketClient client,
                                                             final AgentMetadata metadata) {

        SdkTracerProviderBuilder sdkTracerProviderBuilder = SdkTracerProvider.builder();

        if(!(endpoint == null || endpoint.isEmpty())) {
            sdkTracerProviderBuilder.addSpanProcessor(
                    SimpleSpanProcessor.create(new ZipkinSpanExportDelegator(endpoint))
            );
        }

        return sdkTracerProviderBuilder
                .addSpanProcessor(SimpleSpanProcessor.create(new TrafficHunterSpanExporter(client, metadata)))
                .setIdGenerator(IdGenerator.random())
                .setSampler(Sampler.alwaysOn())
                .setResource(Resource.create(Attributes.of(SERVICE_NAME, serviceName)))
                .build();
    }

    private static SdkLoggerProvider createSdkLoggerProvider(final String serviceName,
                                                             final TrafficHunterWebsocketClient client,
                                                             final AgentMetadata metadata) {

        return SdkLoggerProvider.builder()
                .addLogRecordProcessor(SimpleLogRecordProcessor.create(new TrafficHunterLogExporter(client, metadata)))
                .setLogLimits(LogLimits::getDefault)
                .setResource(Resource.create(Attributes.of(SERVICE_NAME, serviceName)))
                .build();
    }
}
