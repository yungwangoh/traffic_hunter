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
package org.traffichunter.javaagent.plugin.servlet;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import io.opentelemetry.context.Context;
import jakarta.servlet.Filter;
import net.bytebuddy.asm.Advice.Enter;
import net.bytebuddy.asm.Advice.Local;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.asm.Advice.Thrown;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.traffichunter.javaagent.extension.AbstractPluginInstrumentation;
import org.traffichunter.javaagent.extension.Transformer;
import org.traffichunter.javaagent.plugin.sdk.CallDepth;
import org.traffichunter.javaagent.plugin.sdk.instumentation.Instrumentor;
import org.traffichunter.javaagent.plugin.sdk.instumentation.SpanScope;

/**
 * @author yungwang-o
 * @version 1.1.0
 */
public class ServletFilterPluginInstrumentation extends AbstractPluginInstrumentation {

    public ServletFilterPluginInstrumentation() {
        super("jakarta-servlet-filter", ServletFilterPluginInstrumentation.class.getName(), "6.0.0");
    }

    @Override
    public void transform(final Transformer transformer) {

        transformer.processAdvice(
                Advices.create(
                        this.isMethod(),
                        DoFilterAdvice.class
                )
        );
    }

    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("jakarta.servlet.Filter"));
    }

    @Override
    protected ElementMatcher<? super MethodDescription> isMethod() {
        return named("doFilter")
                .and(takesArgument(0, named("jakarta.servlet.ServletRequest")))
                .and(takesArgument(1, named("jakarta.servlet.ServletResponse")))
                .and(isPublic());
    }

    @SuppressWarnings("unused")
    public static class DoFilterAdvice {

        @OnMethodEnter
        public static SpanScope enter(@This final Filter filter,
                                      @Local("callDepth") CallDepth callDepth) {

            callDepth = CallDepth.forClass(Filter.class);
            if(callDepth.getAndIncrement() > 0) {
                return null;
            }

            return Instrumentor.startBuilder(filter)
                    .instrumentationName("servlet-filter-inst")
                    .spanName(filtering -> filtering.getClass().getSimpleName())
                    .context(Context.current())
                    .start();
        }

        @OnMethodExit
        public static void end(@Enter final SpanScope spanScope,
                               @Local("callDepth") CallDepth callDepth,
                               @Thrown final Throwable throwable) {

            if(spanScope == null) {
                return;
            }

            try {
                Instrumentor.end(spanScope, throwable);
            } finally {
                callDepth.decrementAndGet();
            }
        }
    }
}
