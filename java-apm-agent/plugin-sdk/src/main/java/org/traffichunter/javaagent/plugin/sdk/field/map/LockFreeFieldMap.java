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
package org.traffichunter.javaagent.plugin.sdk.field.map;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * @author yungwang-o
 * @version 1.1.0
 * @param <K> key
 * @param <V> value
 */
final class LockFreeFieldMap<K, V> implements FieldMap<K, V> {

    private final Map<K, V> weakMap = new WeakHashMap<>();

    public LockFreeFieldMap() {}

    @Override
    public void put(final K key, final V value) {
        weakMap.put(key, value);
    }

    @Override
    public V get(final K key) {
        return weakMap.get(key);
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return weakMap.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public void remove(final K key) {
        weakMap.remove(key);
    }

    @Override
    public void clear() {
        weakMap.clear();
    }

    @Override
    public int size() {
        return weakMap.size();
    }

    @Override
    public List<V> values() {
        return List.copyOf(weakMap.values());
    }

    @Override
    public List<K> keys() {
        return List.copyOf(weakMap.keySet());
    }
}
