package com.raoulvdberge.refinedstorage.apiimpl.storage;

import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.api.storage.IStorage;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A implementation of {@link IStorage<ItemStack>} that stores storage items in NBT.
 */
public abstract class StorageItemNBT implements IStorage<ItemStack> {
    /**
     * The current save protocol that is used. It's set to every {@link StorageItemNBT} to allow for
     * safe backwards compatibility breaks.
     */
    private static final int PROTOCOL = 1;

    private static final String NBT_PROTOCOL = "Protocol";

    private static final String NBT_ITEMS = "Items";
    private static final String NBT_STORED = "Stored";

    private static final String NBT_ITEM_TYPE = "Type";
    private static final String NBT_ITEM_QUANTITY = "Quantity";
    private static final String NBT_ITEM_DAMAGE = "Damage";
    private static final String NBT_ITEM_NBT = "NBT";
    private static final String NBT_ITEM_CAPS = "Caps";

    private NBTTagCompound tag;
    private int capacity;
    @Nullable
    private INetworkNode node;

    private NonNullList<ItemStack> stacks = NonNullList.create();

    /**
     * @param tag      The NBT tag we are reading from and writing the amount stored to, has to be initialized with {@link StorageItemNBT#createNBT()} if it doesn't exist yet
     * @param capacity The capacity of this storage, -1 for infinite capacity
     * @param node     A {@link INetworkNode} that the NBT storage is in, will be marked dirty when the storage changes
     */
    public StorageItemNBT(NBTTagCompound tag, int capacity, @Nullable INetworkNode node) {
        this.tag = tag;
        this.capacity = capacity;
        this.node = node;

        readFromNBT();
    }

    private void readFromNBT() {
        NBTTagList list = (NBTTagList) tag.getTag(NBT_ITEMS);

        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound tag = list.getCompoundTagAt(i);

            ItemStack stack = new ItemStack(
                Item.getItemById(tag.getInteger(NBT_ITEM_TYPE)),
                tag.getInteger(NBT_ITEM_QUANTITY),
                tag.getInteger(NBT_ITEM_DAMAGE),
                tag.hasKey(NBT_ITEM_CAPS) ? tag.getCompoundTag(NBT_ITEM_CAPS) : null
            );

            stack.setTagCompound(tag.hasKey(NBT_ITEM_NBT) ? tag.getCompoundTag(NBT_ITEM_NBT) : null);

            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
    }

    /**
     * Writes the items to the NBT tag.
     */
    public void writeToNBT() {
        NBTTagList list = new NBTTagList();

        // Dummy value for extracting ForgeCaps
        NBTTagCompound dummy = new NBTTagCompound();

        for (ItemStack stack : stacks) {
            NBTTagCompound itemTag = new NBTTagCompound();

            itemTag.setInteger(NBT_ITEM_TYPE, Item.getIdFromItem(stack.getItem()));
            itemTag.setInteger(NBT_ITEM_QUANTITY, stack.getCount());
            itemTag.setInteger(NBT_ITEM_DAMAGE, stack.getItemDamage());

            if (stack.hasTagCompound()) {
                itemTag.setTag(NBT_ITEM_NBT, stack.getTagCompound());
            }

            stack.writeToNBT(dummy);

            if (dummy.hasKey("ForgeCaps")) {
                itemTag.setTag(NBT_ITEM_CAPS, dummy.getTag("ForgeCaps"));
            }

            dummy.removeTag("ForgeCaps");

            list.appendTag(itemTag);
        }

        tag.setTag(NBT_ITEMS, list);
        tag.setInteger(NBT_PROTOCOL, PROTOCOL);
    }

    @Override
    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }

    @Override
    public synchronized ItemStack insert(@Nonnull ItemStack stack, int size, boolean simulate) {
        for (ItemStack otherStack : stacks) {
            if (API.instance().getComparer().isEqualNoQuantity(otherStack, stack)) {
                if (getCapacity() != -1 && getStored() + size > getCapacity()) {
                    int remainingSpace = getCapacity() - getStored();

                    if (remainingSpace <= 0) {
                        if (isVoiding()) {
                            return null;
                        }

                        return ItemHandlerHelper.copyStackWithSize(stack, size);
                    }

                    if (!simulate) {
                        tag.setInteger(NBT_STORED, getStored() + remainingSpace);

                        otherStack.grow(remainingSpace);

                        onStorageChanged();
                    }

                    return isVoiding() ? null : ItemHandlerHelper.copyStackWithSize(otherStack, size - remainingSpace);
                } else {
                    if (!simulate) {
                        tag.setInteger(NBT_STORED, getStored() + size);

                        otherStack.grow(size);

                        onStorageChanged();
                    }

                    return null;
                }
            }
        }

        if (getCapacity() != -1 && getStored() + size > getCapacity()) {
            int remainingSpace = getCapacity() - getStored();

            if (remainingSpace <= 0) {
                if (isVoiding()) {
                    return null;
                }

                return ItemHandlerHelper.copyStackWithSize(stack, size);
            }

            if (!simulate) {
                tag.setInteger(NBT_STORED, getStored() + remainingSpace);

                stacks.add(ItemHandlerHelper.copyStackWithSize(stack, remainingSpace));

                onStorageChanged();
            }

            return isVoiding() ? null : ItemHandlerHelper.copyStackWithSize(stack, size - remainingSpace);
        } else {
            if (!simulate) {
                tag.setInteger(NBT_STORED, getStored() + size);

                stacks.add(ItemHandlerHelper.copyStackWithSize(stack, size));

                onStorageChanged();
            }

            return null;
        }
    }

    @Override
    public synchronized ItemStack extract(@Nonnull ItemStack stack, int size, int flags, boolean simulate) {
        for (ItemStack otherStack : stacks) {
            if (API.instance().getComparer().isEqual(otherStack, stack, flags)) {
                if (size > otherStack.getCount()) {
                    size = otherStack.getCount();
                }

                if (!simulate) {
                    if (otherStack.getCount() - size == 0) {
                        stacks.remove(otherStack);
                    } else {
                        otherStack.shrink(size);
                    }

                    tag.setInteger(NBT_STORED, getStored() - size);

                    onStorageChanged();
                }

                return ItemHandlerHelper.copyStackWithSize(otherStack, size);
            }
        }

        return null;
    }

    public void onStorageChanged() {
        if (node != null) {
            node.markDirty();
        }
    }

    @Override
    public int getStored() {
        return getStoredFromNBT(tag);
    }

    public int getCapacity() {
        return capacity;
    }

    protected boolean isVoiding() {
        return false;
    }

    @Override
    public int getCacheDelta(int storedPreInsertion, int size, @Nullable ItemStack remainder) {
        if (getAccessType() == AccessType.INSERT) {
            return 0;
        }

        int inserted = remainder == null ? size : (size - remainder.getCount());

        if (isVoiding() && storedPreInsertion + inserted > getCapacity()) {
            inserted = getCapacity() - storedPreInsertion;
        }

        return inserted;
    }

    public NBTTagCompound getTag() {
        return tag;
    }

    public static int getStoredFromNBT(NBTTagCompound tag) {
        return tag.getInteger(NBT_STORED);
    }

    public static NBTTagCompound getNBTShareTag(NBTTagCompound tag) {
        NBTTagCompound otherTag = new NBTTagCompound();

        otherTag.setInteger(NBT_STORED, getStoredFromNBT(tag));
        otherTag.setTag(NBT_ITEMS, new NBTTagList()); // To circumvent not being able to insert disks in Disk Drives (see ItemStorageNBT#isValid(ItemStack)).
        otherTag.setInteger(NBT_PROTOCOL, PROTOCOL);

        return otherTag;
    }

    /*
     * @return A NBT tag initialized with the fields that {@link NBTStorage} uses
     */
    public static NBTTagCompound createNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag(NBT_ITEMS, new NBTTagList());
        tag.setInteger(NBT_STORED, 0);
        tag.setInteger(NBT_PROTOCOL, PROTOCOL);

        return tag;
    }

    public static boolean isValid(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_ITEMS) && stack.getTagCompound().hasKey(NBT_STORED);
    }

    /**
     * @param stack The {@link ItemStack} to populate with the NBT tags from {@link StorageItemNBT#createNBT()}
     * @return The provided {@link ItemStack} with NBT tags from {@link StorageItemNBT#createNBT()}
     */
    public static ItemStack createStackWithNBT(ItemStack stack) {
        stack.setTagCompound(createNBT());

        return stack;
    }
}