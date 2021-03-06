package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeDiskDrive;
import com.raoulvdberge.refinedstorage.render.PropertyObject;
import com.raoulvdberge.refinedstorage.tile.TileDiskDrive;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BlockDiskDrive extends BlockNode {
    public static final PropertyObject<Integer[]> DISK_STATE = new PropertyObject<>("disk_state", Integer[].class);

    public BlockDiskDrive() {
        super("disk_drive");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileDiskDrive();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            tryOpenNetworkGui(RSGui.DISK_DRIVE, player, world, pos, side);
        }

        return true;
    }

    @Override
    protected BlockStateContainer.Builder createBlockStateBuilder() {
        return super.createBlockStateBuilder().add(DISK_STATE);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((IExtendedBlockState) super.getExtendedState(state, world, pos)).withProperty(DISK_STATE, ((TileDiskDrive) world.getTileEntity(pos)).getDiskState());
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        ((NetworkNodeDiskDrive) ((TileDiskDrive) world.getTileEntity(pos)).getNode()).onBreak();

        super.breakBlock(world, pos, state);
    }
}
