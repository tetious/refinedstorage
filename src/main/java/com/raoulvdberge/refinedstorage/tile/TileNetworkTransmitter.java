package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeNetworkTransmitter;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.network.datasync.DataSerializers;

public class TileNetworkTransmitter extends TileNode {
    public static final TileDataParameter<Integer> DISTANCE = new TileDataParameter<>(DataSerializers.VARINT, 0, new ITileDataProducer<Integer, TileNetworkTransmitter>() {
        @Override
        public Integer getValue(TileNetworkTransmitter tile) {
            NetworkNodeNetworkTransmitter transmitter = (NetworkNodeNetworkTransmitter) tile.getNode();

            return (transmitter.getReceiver() != null && transmitter.isSameDimension()) ? transmitter.getDistance() : -1;
        }
    });

    public static final TileDataParameter<Integer> RECEIVER_DIMENSION = new TileDataParameter<>(DataSerializers.VARINT, 0, new ITileDataProducer<Integer, TileNetworkTransmitter>() {
        @Override
        public Integer getValue(TileNetworkTransmitter tile) {
            return ((NetworkNodeNetworkTransmitter) tile.getNode()).getReceiverDimension();
        }
    });

    public static final TileDataParameter<Boolean> RECEIVER_DIMENSION_SUPPORTED = new TileDataParameter<>(DataSerializers.BOOLEAN, false, new ITileDataProducer<Boolean, TileNetworkTransmitter>() {
        @Override
        public Boolean getValue(TileNetworkTransmitter tile) {
            return ((NetworkNodeNetworkTransmitter) tile.getNode()).isDimensionSupported();
        }
    });

    public TileNetworkTransmitter() {
        dataManager.addWatchedParameter(DISTANCE);
        dataManager.addWatchedParameter(RECEIVER_DIMENSION);
        dataManager.addWatchedParameter(RECEIVER_DIMENSION_SUPPORTED);
    }

    @Override
    public INetworkNode createNode() {
        return new NetworkNodeNetworkTransmitter(this);
    }
}
