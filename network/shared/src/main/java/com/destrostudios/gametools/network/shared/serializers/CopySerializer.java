package com.destrostudios.gametools.network.shared.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class CopySerializer<T> extends Serializer<T> {

    @Override
    public T copy(Kryo kryo, T original) {
        if (original == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output out = new Output(stream);
        kryo.writeObject(out, original);
        out.flush();

        Input in = new Input(new ByteArrayInputStream(stream.toByteArray()));
        T copy = kryo.readObject(in, (Class<? extends T>) original.getClass());
        return copy;
    }
}
