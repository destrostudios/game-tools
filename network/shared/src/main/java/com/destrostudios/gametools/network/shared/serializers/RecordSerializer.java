package com.destrostudios.gametools.network.shared.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.stream.Stream;

public class RecordSerializer<T extends Record> extends CopySerializer<T> {

    @Override
    public void write(Kryo kryo, Output output, T object) {
        Class<?> type = object.getClass();
        RecordComponent[] recordComponents = type.getRecordComponents();
        try {
            for (RecordComponent component : recordComponents) {
                Method accessor = component.getAccessor();
                Object value = accessor.invoke(object);
                if (component.getType().isPrimitive()) {
                    kryo.writeObject(output, value);
                } else if ((component.getType().getModifiers() & Modifier.FINAL) != 0) {
                    kryo.writeObjectOrNull(output, value, component.getType());
                } else {
                    kryo.writeClassAndObject(output, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write " + object, e);
        }
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> type) {
        RecordComponent[] recordComponents = type.getRecordComponents();
        Object[] args = new Object[recordComponents.length];
        for (int i = 0; i < args.length; i++) {
            RecordComponent component = recordComponents[i];
            if (component.getType().isPrimitive()) {
                args[i] = kryo.readObject(input, component.getType());
            } else if ((component.getType().getModifiers() & Modifier.FINAL) != 0) {
                args[i] = kryo.readObjectOrNull(input, component.getType());
            } else {
                args[i] = kryo.readClassAndObject(input);
            }
        }
        try {
            Constructor<T> constructor = type.getConstructor(Stream.of(recordComponents).map(RecordComponent::getType).toArray(Class[]::new));
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read object of type " + type.getName(), e);
        }
    }
}
