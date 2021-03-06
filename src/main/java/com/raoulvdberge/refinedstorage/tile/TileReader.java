package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReader;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterChannel;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterHandler;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeReader;
import com.raoulvdberge.refinedstorage.gui.GuiReaderWriter;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataConsumer;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileReader extends TileNode {
    static <T extends TileEntity & IReaderWriter> TileDataParameter<String> createChannelParameter() {
        return new TileDataParameter<>(DataSerializers.STRING, "", new ITileDataProducer<String, T>() {
            @Override
            public String getValue(T tile) {
                return tile.getChannel();
            }
        }, new ITileDataConsumer<String, T>() {
            @Override
            public void setValue(T tile, String value) {
                tile.setChannel(value);

                tile.markDirty();
            }
        }, parameter -> {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiReaderWriter) {
                ((GuiReaderWriter) Minecraft.getMinecraft().currentScreen).updateSelection(parameter.getValue());
            }
        });
    }

    public static final TileDataParameter<String> CHANNEL = createChannelParameter();

    public TileReader() {
        dataManager.addWatchedParameter(CHANNEL);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (super.hasCapability(capability, facing)) {
            return true;
        }

        IReader reader = (IReader) getNode();

        if (facing != getDirection() || reader.getNetwork() == null) {
            return false;
        }

        IReaderWriterChannel channel = reader.getNetwork().getReaderWriterChannel(reader.getChannel());

        if (channel == null) {
            return false;
        }

        for (IReaderWriterHandler handler : channel.getHandlers()) {
            if (handler.hasCapability(reader, capability)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        T foundCapability = super.getCapability(capability, facing);

        if (foundCapability == null) {
            IReader reader = (IReader) getNode();

            if (facing != getDirection() || reader.getNetwork() == null) {
                return null;
            }

            IReaderWriterChannel channel = reader.getNetwork().getReaderWriterChannel(reader.getChannel());

            if (channel == null) {
                return null;
            }

            for (IReaderWriterHandler handler : channel.getHandlers()) {
                foundCapability = handler.getCapability(reader, capability);

                if (foundCapability != null) {
                    return foundCapability;
                }
            }
        }

        return foundCapability;
    }

    @Override
    public INetworkNode createNode() {
        return new NetworkNodeReader(this);
    }
}
