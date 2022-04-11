package com.destrostudios.gametools.network.shared.serializers;

import com.destrostudios.gametools.network.shared.modules.jwt.messages.Login;
import com.esotericsoftware.kryo.Kryo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class RecordSerializerTest {

    @Test
    public void copyRecord() {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setCopyReferences(false);
        kryo.register(Login.class, new RecordSerializer<>());

        Login value = new Login("myLogin");

        Login copy = kryo.copy(value);
        assertEquals(value, copy);
    }
}
