package com.destrostudios.gametools.network.shared.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class EnumSerializer<T extends Enum<T>> extends CopySerializer<T> {

    private final Class<T> enumClass;

    public EnumSerializer(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void write(Kryo kryo, Output output, T object) {
        T[] constants = enumClass.getEnumConstants();
        if (constants.length <= (1 << Byte.SIZE)) {
            output.writeByte(object.ordinal());
        } else if (constants.length <= (1 << Short.SIZE)) {
            output.writeShort(object.ordinal());
        } else {
            output.writeInt(object.ordinal());
        }
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> type) {
        T[] constants = enumClass.getEnumConstants();
        if (constants.length <= (1 << Byte.SIZE)) {
            return constants[Byte.toUnsignedInt(input.readByte())];
        } else if (constants.length <= (1 << Short.SIZE)) {
            return constants[Short.toUnsignedInt(input.readShort())];
        } else {
            return constants[input.readInt()];
        }
    }
}
