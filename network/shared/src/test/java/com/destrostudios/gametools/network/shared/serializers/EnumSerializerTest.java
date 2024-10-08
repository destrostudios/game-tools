package com.destrostudios.gametools.network.shared.serializers;

import com.esotericsoftware.kryo.Kryo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumSerializerTest {

    @Test
    public void copyEnum() {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setCopyReferences(false);
        kryo.register(TestEnum.class, new EnumSerializer<>(TestEnum.class));

        TestEnum value = TestEnum.VALUE_B;

        TestEnum copy = kryo.copy(value);
        assertEquals(value, copy);
    }
}
