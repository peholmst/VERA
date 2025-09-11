/*
 * Copyright (c) 2025 Petter Holmström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.pkhapps.vera.server.util.wal;

import net.pkhapps.vera.server.util.serde.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/// Helper class used by [FileSystemWal] to find the right [WalSerde] for serializing and deserializing
/// [WalSnapshot]s and [WalEvent]s.
class WalSerdeManager {

    private static final Logger log = LoggerFactory.getLogger(WalSerdeManager.class);

    private final ConcurrentMap<Class<?>, WalSerde<?>> classToSerdeMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, WalSerde<?>> serdeIdToSerdeMap = new ConcurrentHashMap<>();
    private final ThreadLocal<ScratchBuffer> scratchBuffer = ThreadLocal.withInitial(ScratchBuffer::new);

    WalSerdeManager(Iterable<WalSerdeRegistrator> walSerdeRegistrators) {
        walSerdeRegistrators.forEach(walSerdeRegistrator ->
                walSerdeRegistrator.registerSerdes(serde -> {
                    log.info("Registering serde {} for serdeId {}", serde, serde.serdeId());
                    if (serdeIdToSerdeMap.putIfAbsent(serde.serdeId(), serde) != null) {
                        throw new SerdeException("Duplicate serdeId: " + serde.serdeId());
                    }
                })
        );
    }

    @SuppressWarnings("unchecked")
    private <T> Serde<T> findSerde(T object) {
        Class<?> type = object.getClass();
        WalSerde<?> serde = classToSerdeMap.get(type);
        if (serde == null) {
            serde = serdeIdToSerdeMap.values()
                    .stream()
                    .filter(s -> s.supports(object))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("No serde found for type {}", type);
                        return new UnknownTypeException(type);
                    });
            log.info("Registering serde {} for type {} (serdeId: {})", serde, type.getName(), serde.serdeId());
            classToSerdeMap.putIfAbsent(type, serde);
        }
        return (Serde<T>) serde;
    }

    @SuppressWarnings("unchecked")
    private <T> Serde<T> findSerde(int serdeId) {
        WalSerde<?> serde = serdeIdToSerdeMap.get(serdeId);
        if (serde == null) {
            log.error("No serde found for serdeId {}", serdeId);
            throw new UnknownSerdeIdException(serdeId);
        }
        return (Serde<T>) serde;
    }

    /// Serializes the given `object` into a byte array.
    ///
    /// **Note:** Multiple threads can call this method, but the returned array should only be used inside the thread
    /// that called this method. The array might also be reused by subsequent calls to this method from the same thread.
    ///
    /// @param object the object to serialize
    /// @return the byte array, offset, and length
    /// @throws SerdeException if the object cannot be serialized to a byte array
    public <T> Serialized serialize(T object) {
        var serde = findSerde(object);
        var sizingOutput = new SizingOutput();
        serde.writeTo(object, sizingOutput);

        var buffer = ByteBuffer.wrap(scratchBuffer.get().ensureCapacity(sizingOutput.size()));
        var bufferOutput = BufferOutput.wrap(buffer);
        serde.writeTo(object, bufferOutput);
        return new Serialized(buffer.array(), 0, buffer.position());
    }

    /// Data structure holding a byte array, an offset, and a length.
    ///
    /// @param bytes  the byte array holding the data
    /// @param offset the position of the first byte to read from the array
    /// @param length the number of bytes to read from the array
    public record Serialized(byte[] bytes, int offset, int length) {
    }

    /// Deserializes the given bytes into an object.
    ///
    /// **Note:** Multiple threads can call this method, provided that the array is not being modified by another
    /// thread.
    ///
    /// @param serialized the byte array, offset, and length
    /// @return the object
    /// @throws SerdeException if the bytes cannot be deserialized to an object
    public <T> T deserialize(Serialized serialized) {
        return deserialize(serialized.bytes, serialized.offset, serialized.length);
    }

    /// Deserializes the given bytes into an object.
    ///
    /// **Note:** Multiple threads can call this method, provided that the array is not being modified by another
    /// thread.
    ///
    /// @param bytes  the byte array
    /// @param offset the position of the first byte to read from the array
    /// @param length the number of bytes to read from the array
    /// @return the object
    /// @throws SerdeException if the bytes cannot be deserialized to an object
    public <T> T deserialize(byte[] bytes, int offset, int length) {
        var input = BufferInput.wrap(bytes, offset, length);
        var serdeId = WalSerde.readSerdeId(input);
        input.reset();
        Serde<T> serde = findSerde(serdeId);
        return serde.readFrom(input);
    }
}
