package refinedstorage.gui;

import net.minecraft.inventory.Container;
import refinedstorage.gui.sidebutton.SideButtonRedstoneMode;
import refinedstorage.tile.TileWirelessTransmitter;

public class GuiWirelessTransmitter extends GuiBase
{
	private TileWirelessTransmitter wirelessTransmitter;

	public GuiWirelessTransmitter(Container container, TileWirelessTransmitter wirelessTransmitter)
	{
		super(container, 176, 137);

		this.wirelessTransmitter = wirelessTransmitter;
	}

	@Override
	public void init(int x, int y)
	{
		addSideButton(new SideButtonRedstoneMode(wirelessTransmitter));
	}

	@Override
	public void update(int x, int y)
	{
	}

	@Override
	public void drawBackground(int x, int y, int mouseX, int mouseY)
	{
		bindTexture("gui/wireless_transmitter.png");

		drawTexture(x, y, 0, 0, width, height);

		if (wirelessTransmitter.isWorking())
		{
			int progress = (int) ((float) wirelessTransmitter.getProgress() / (float) TileWirelessTransmitter.TOTAL_PROGRESS * 14f);

			drawTexture(x + 36 - 1, y + 21 - 1 + progress, 178, 0 + progress, 14, 14);
		}
	}

	@Override
	public void drawForeground(int mouseX, int mouseY)
	{
		drawString(7, 7, t("gui.refinedstorage:wireless_transmitter"));
		drawString(7, 43, t("container.inventory"));

		if (inBounds(36, 21, 14, 14, mouseX, mouseY) && wirelessTransmitter.isWorking())
		{
			int workRemaining = (int) (((float) (TileWirelessTransmitter.TOTAL_PROGRESS - wirelessTransmitter.getProgress())) / (float) TileWirelessTransmitter.TOTAL_PROGRESS * 100f);

			drawTooltip(mouseX, mouseY, workRemaining + "%");
		}
	}
}