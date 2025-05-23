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
package org.traffichunter.javaagent.plugin.sdk.support.http;

import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author yungwang-o
 * @version 1.1.0
 */
public class HttpInstrumentationSupport {

    public static final class HttpHeaderExtractor {

        private final HttpHeaders httpHeaders;

        private final Map<String, List<String>> headerMap;

        public HttpHeaderExtractor(final HttpHeaders httpHeaders) {
            this.httpHeaders = httpHeaders;
            this.headerMap = new HashMap<>();
        }

        public HttpHeaderExtractor mapToHeaderName(final String name) {
            List<String> value = httpHeaders.allValues(name.toLowerCase());
            headerMap.put(name, value);
            return this;
        }

        public Map<String, List<String>> getHeaderMap() {
            return headerMap;
        }

        public Set<Entry<String, List<String>>> getHeaderEntry() {
            return headerMap.entrySet();
        }
    }
}
