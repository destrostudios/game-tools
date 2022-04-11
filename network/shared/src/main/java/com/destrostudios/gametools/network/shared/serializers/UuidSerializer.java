package com.destrostudios.gametools.network.shared.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.UUID;

public class UuidSerializer extends CopySerializer<UUID> {

    @Override
    public void write(Kryo kryo, Output output, UUID t) {
        output.writeLong(t.getMostSignificantBits());
        output.writeLong(t.getLeastSignificantBits());
    }

    @Override
    public UUID read(Kryo kryo, Input input, Class<UUID> type) {
        return new UUID(input.readLong(), input.readLong());
    }

}