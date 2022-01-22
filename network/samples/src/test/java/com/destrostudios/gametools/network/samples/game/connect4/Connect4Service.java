package com.destrostudios.gametools.network.samples.game.connect4;

import com.destrostudios.gametools.network.shared.modules.game.GameService;
import com.destrostudios.gametools.network.shared.modules.game.NetworkRandom;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Connect4Service implements GameService<Connect4Impl, Long> {

    @Override
    public void initialize(Kryo kryo) {
        kryo.register(Connect4Impl.class, new Serializer<Connect4Impl>() {
            @Override
            public void write(Kryo kryo, Output output, Connect4Impl object) {
                output.writeInt(object.width, true);
                output.writeInt(object.height, true);
                output.writeLong(object.own);
                output.writeLong(object.opp);
            }

            @Override
            public Connect4Impl read(Kryo kryo, Input input, Class type) {
                Connect4Impl state = new Connect4Impl(input.readInt(true), input.readInt(true));
                state.own = input.readLong();
                state.opp = input.readLong();
                return state;
            }
        });
        kryo.register(Connect4StartInfo.class);
    }

    @Override
    public Connect4Impl applyAction(Connect4Impl state, Long action, NetworkRandom random) {
        if (Long.bitCount(action) != 1) {

            // destroy state to test whether backup works
            state.own = ~0;
            state.opp = ~0;

            throw new IllegalArgumentException(action + " is not a valid move.");
        }
        if ((state.availableMoves() & action) == 0) {
            throw new IllegalArgumentException(action + " is not an available move.");
        }
        state.move(action);
        return state;
    }

}
