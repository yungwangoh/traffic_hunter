package ygo.traffic_hunter.presentation.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ygo.traffic_hunter.AbstractTestConfiguration;
import ygo.traffic_hunter.core.assembler.span.SpanTreeNode;
import ygo.traffic_hunter.core.dto.request.statistics.StatisticsRequest;
import ygo.traffic_hunter.core.dto.response.statistics.metric.StatisticsMetricAvgResponse;
import ygo.traffic_hunter.core.dto.response.statistics.metric.StatisticsMetricMaxResponse;
import ygo.traffic_hunter.core.dto.response.statistics.transaction.ServiceTransactionResponse;
import ygo.traffic_hunter.core.service.MetricStatisticsService;
import ygo.traffic_hunter.core.statistics.StatisticsMetricTimeRange;
import ygo.traffic_hunter.domain.metric.TransactionData;
import ygo.traffic_hunter.presentation.advice.GlobalControllerAdvice;

@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(controllers = MetricController.class)
class MetricControllerTest extends AbstractTestConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    MetricStatisticsService metricStatisticsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new MetricController(metricStatisticsService))
                .apply(documentationConfiguration(restDocumentation))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalControllerAdvice())
                .build();
    }

    @Test
    void 서비스_트랜잭션은_정상적으로_동작한다_200() throws Exception {
        // given
        StatisticsRequest sr = new StatisticsRequest(
                Instant.now().minus(Duration.ofDays(1)),
                Instant.now()
        );

        ServiceTransactionResponse str = new ServiceTransactionResponse(
                Instant.now().atOffset(ZoneOffset.UTC),
                "/test",
                100,
                "POST",
                "payments-agent",
                "traffic-hunter.com",
                200,
                UUID.randomUUID().toString()
        );

        // when
        ResultActions resultActions = mockMvc.perform(get("/statistics/transaction")
                .content(objectMapper.writeValueAsString(sr))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "count,DESC"));

        // then
        resultActions.andExpect(status().isOk())
                .andDo(result -> {
                    MockHttpServletResponse response = result.getResponse();
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter()
                            .write(objectMapper.writeValueAsString(new SliceImpl<>(List.of(str))));
                })
                .andDo(print())
                .andDo(document("service-transaction"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())
                        , queryParameters(
                                parameterWithName("page").description("페이지 넘버 (default 0)"),
                                parameterWithName("size").description("페이지 사이즈 (default 10)"),
                                parameterWithName("sort").description("페이지 정렬 (default count,DESC)")
                        )
                        , requestFields(
                                fieldWithPath("begin").description("조회 시작 시간 UTC"),
                                fieldWithPath("end").description("조회 종료 시간 UTC")
                        )
                        , responseFields(
                                subsectionWithPath("content").description("조회된 서비스 트랜잭션 목록"),
                                fieldWithPath("content[].timestamp").description("트랜잭션 시작 시간 UTC"),
                                fieldWithPath("content[].uri").description("해당 서비스의 uri"),
                                fieldWithPath("content[].duration").description("해당 서비스의 실행 시간"),
                                fieldWithPath("content[].httpMethod").description("해당 서비스의 http 응답 method"),
                                fieldWithPath("content[].agentName").description("에이전트 이름"),
                                fieldWithPath("content[].clientName").description("클라이언트 이름 (도메인 이름)"),
                                fieldWithPath("content[].httpStatusCode").description("http 상태 코드"),
                                fieldWithPath("content[].traceId").description("트랜잭션 식별자"),
                                subsectionWithPath("pageable").description("페이징 처리 정보"),
                                fieldWithPath("first").description("첫 페이지 여부"),
                                fieldWithPath("last").description("마지막 페이지 여부"),
                                fieldWithPath("size").description("페이지 크기"),
                                fieldWithPath("number").description("현재 페이지 번호"),
                                subsectionWithPath("sort").description("정렬 정보"),
                                fieldWithPath("numberOfElements").description("현재 페이지에 포함된 데이터 개수"),
                                fieldWithPath("empty").description("조회 결과가 비어있는지 여부")
                        )
                ));
    }

    @Test
    void 트랜젝션_상세_페이지는_정상적으로_동작한다_200() throws Exception {
        // given
        String traceId = "a7ae6e1955ce6033770a93fe8257f636";

        TransactionData transactionData = TransactionData.builder()
                .traceId(traceId)
                .endTime(Instant.now())
                .startTime(Instant.now())
                .duration(100)
                .attributesCount(1)
                .name("GET /test")
                .exception("exception")
                .spanId("26b9ee3faac1317c")
                .parentSpanId("88e42ecd823670e2")
                .attributes(Map.of())
                .ended(true)
                .build();

        SpanTreeNode spanTreeNode = new SpanTreeNode(transactionData);

        given(metricStatisticsService.retrieveSpanTree(traceId)).willReturn(spanTreeNode);

        // when
        ResultActions resultActions = mockMvc.perform(get("/statistics/transaction/{traceId}", traceId));

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("service-transaction-detail"
                    , preprocessRequest(prettyPrint())
                    , preprocessResponse(prettyPrint())
                    , pathParameters(
                            parameterWithName("traceId").description("trace ID (span들의 집합을 식별하는 ID)")
                    )
                    , responseFields(
                                fieldWithPath("data").description("span의 정보를 담고 있는 DTO"),
                                fieldWithPath("data.name").description("span의 이름 (예: 'GET /test')"),
                                fieldWithPath("data.traceId").description("trace ID (span들의 집합을 식별하는 ID)"),
                                fieldWithPath("data.parentSpanId").description("부모 span의 ID (최고 부모는 0000000000000)"),
                                fieldWithPath("data.spanId").description("현재 span의 ID"),
                                fieldWithPath("data.attributes").description("span에 대한 추가 메타데이터 key-value"),
                                fieldWithPath("data.attributesCount").description("속성의 개수"),
                                fieldWithPath("data.startTime").description("span의 시작 시간 UTC"),
                                fieldWithPath("data.endTime").description("span의 종료 시간 UTC"),
                                fieldWithPath("data.duration").description("span의 지속 시간 (밀리초)"),
                                fieldWithPath("data.exception").description("발생한 예외 (있을 경우)"),
                                fieldWithPath("data.ended").description("span이 종료되었는지 여부 (true/false)"),
                                fieldWithPath("children").description("자식 transaction data (자식은 여러 개일 수 있음)")
                        )
                ));
    }

    @Test
    void 최대_메트릭을_조회하는_API는_정상적으로_동작한다_200() throws Exception {
        // given
        StatisticsMetricMaxResponse maxResponse = new StatisticsMetricMaxResponse(
            Instant.now(),
            50.8,
            3.0,
            2048,
            10,
            10,
            20,
            3,
            1,
            10
        );

        given(metricStatisticsService.retrieveStatisticsMaxMetric(StatisticsMetricTimeRange.LATEST_ONE_HOUR))
                .willReturn(maxResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/statistics/metric/max/{timeRange}", StatisticsMetricTimeRange.LATEST_ONE_HOUR)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("retrieve-max-metric"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())
                        , pathParameters(
                                parameterWithName("timeRange").description(
                                        Arrays.stream(StatisticsMetricTimeRange.values())
                                                .map(Enum::name)
                                                .collect(Collectors.joining(", "))
                                )
                        )
                        , responseFields(
                                fieldWithPath("time").description("데이터 저장된 시간 UTC"),
                                fieldWithPath("maxSystemCpuUsage").description("최고 system cpu (linux) 사용량"),
                                fieldWithPath("maxProcessCpuUsage").description("최고 process cpu (jvm) 사용량"),
                                fieldWithPath("maxHeapMemoryUsage").description("최고 힙 메모리 사용량"),
                                fieldWithPath("maxThreadCount").description("최고 쓰레드 사용 개수"),
                                fieldWithPath("maxPeakThreadCount").description("최고 쓰레드 경합 개수"),
                                fieldWithPath("maxWebRequestCount").description("최고 웹 요청 횟수"),
                                fieldWithPath("maxWebErrorCount").description("최고 웹 에러 응답 횟수"),
                                fieldWithPath("maxWebThreadCount").description("최고 웹 쓰레드 활성화 개수"),
                                fieldWithPath("maxDbConnectionCount").description("최고 db 커넥션 활성화 개수")
                        )
                ));
    }

    @Test
    void 평균_메트릭을_조회하는_API는_정상적으로_동작한다_200() throws Exception {
        // given
        StatisticsMetricAvgResponse avgResponse = new StatisticsMetricAvgResponse(
                Instant.now(),
                50.8,
                3.0,
                2048.0,
                10.0,
                10.0,
                20.0,
                3.0,
                1.0,
                10.0
        );

        given(metricStatisticsService.retrieveStatisticsAvgMetric(StatisticsMetricTimeRange.LATEST_ONE_HOUR))
                .willReturn(avgResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/statistics/metric/avg/{timeRange}", StatisticsMetricTimeRange.LATEST_ONE_HOUR)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("retrieve-avg-metric"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())
                        , pathParameters(
                                parameterWithName("timeRange").description(
                                        Arrays.stream(StatisticsMetricTimeRange.values())
                                                .map(Enum::name)
                                                .collect(Collectors.joining(", "))
                                )
                        )
                        , responseFields(
                                fieldWithPath("time").description("데이터 저장된 시간 UTC"),
                                fieldWithPath("avgSystemCpuUsage").description("평균 system cpu (linux) 사용량"),
                                fieldWithPath("avgProcessCpuUsage").description("평균 process cpu (jvm) 사용량"),
                                fieldWithPath("avgHeapMemoryUsage").description("평균 힙 메모리 사용량"),
                                fieldWithPath("avgThreadCount").description("평균 쓰레드 사용 개수"),
                                fieldWithPath("avgPeakThreadCount").description("평균 쓰레드 경합 개수"),
                                fieldWithPath("avgWebRequestCount").description("평균 웹 요청 횟수"),
                                fieldWithPath("avgWebErrorCount").description("평균 웹 에러 응답 횟수"),
                                fieldWithPath("avgWebThreadCount").description("평균 웹 쓰레드 활성화 개수"),
                                fieldWithPath("avgDbConnectionCount").description("평균 db 커넥션 활성화 개수")
                        )
                ));
    }

    @Test
    @Disabled
    void 서비스_트랜잭션은_begin이_null일때_에러가_발생한다_400() throws Exception {
        // given
        StatisticsRequest sr = new StatisticsRequest(
                null,
                Instant.now()
        );

        // when
        ResultActions resultActions = mockMvc.perform(get("/statistics/transaction")
                .content(objectMapper.writeValueAsString(sr))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "count,DESC"));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("400-error-service-transaction"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())
                ));
    }

    @Test
    void 최대_메트릭을_조회하는_API의_결과가_없을_경우_에러를_발생시킨다_400() throws Exception {
        // given
        given(metricStatisticsService.retrieveStatisticsMaxMetric(StatisticsMetricTimeRange.LATEST_ONE_HOUR))
                .willThrow(new IllegalArgumentException("not found max metric"));

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/statistics/metric/max/{timeRange}", StatisticsMetricTimeRange.LATEST_ONE_HOUR)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("400-error-retrieve-max-metric"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())
                ));
    }

    @Test
    void 평균_메트릭을_조회하는_API의_결과가_없을_경우_에러를_발생시킨다_400() throws Exception {
        // given
        given(metricStatisticsService.retrieveStatisticsAvgMetric(StatisticsMetricTimeRange.LATEST_ONE_HOUR))
                .willThrow(new IllegalArgumentException("not found max metric"));

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/statistics/metric/avg/{timeRange}", StatisticsMetricTimeRange.LATEST_ONE_HOUR)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("400-error-retrieve-avg-metric"
                        , preprocessRequest(prettyPrint())
                        , preprocessResponse(prettyPrint())
                ));
    }
}