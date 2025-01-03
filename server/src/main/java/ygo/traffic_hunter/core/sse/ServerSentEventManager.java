/**
 * The MIT License
 *
 * Copyright (c) 2024 yungwang-o
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
package ygo.traffic_hunter.core.sse;

import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * <p>
 * The {@code ServerSentEventManager} interface provides a contract for managing
 * Server-Sent Events (SSE) and sending data to clients. This interface is typically
 * implemented by classes designed for specific purposes, such as managing UI updates
 * for client-side rendering (CSR) frameworks like React.js or Vue.js, or handling
 * alert notifications.
 * </p>
 *
 * @see SseEmitter
 *
 * @author yungwang-o
 * @version 1.0.0
 */
public interface ServerSentEventManager {

    SseEmitter register(SseEmitter sseEmitter);

    <T> void send(T data);

    <T> void send(List<T> data);

    <T> void asyncSend(T data);

    <T> void asyncSend(List<T> data);

    int size();
}
