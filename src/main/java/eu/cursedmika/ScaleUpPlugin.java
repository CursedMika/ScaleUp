package eu.cursedmika;

import eu.cursedmika.ui.ScaleUpPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;

import static net.runelite.api.gameval.InventoryID.BANK;

@Slf4j
@PluginDescriptor(
	name = "ScaleUp", description = "ScaleUp any activity",
	tags = {"panel", "skilling"}
)
public class ScaleUpPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private TrackingService trackingService;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ItemCollector itemCollector;

    private NavigationButton navButton;
	private int refreshCounter = 0;
	private ScaleUpPanel scaleUpPanel = null;

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Override
	protected void startUp()
	{
		scaleUpPanel = new ScaleUpPanel(trackingService, clientThread);
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/scaleup.png");
		this.navButton = NavigationButton.builder()
				.tooltip("ScaleUp")
				.icon(icon)
				.priority(36)
				.panel(scaleUpPanel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == BANK)
		{
			ItemContainer bankContainer = event.getItemContainer();
			if (bankContainer != null)
			{
				itemCollector.snapshotBankItems(bankContainer);
				trackingService.handleBankData();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) {
		if(!trackingService.isTracking())
		{
			return;
		}

		incrementRefresh();

		if(shouldRefresh())
		{
			trackingService.snapshot();
			SwingUtilities.invokeLater(() -> scaleUpPanel.updateUI(trackingService.getComputedSnapshot()));
		}
	}

	private boolean shouldRefresh()
	{
		return refreshCounter == 5;
	}

	private void incrementRefresh()
	{
		if(refreshCounter >= 5)
		{
			refreshCounter = 0;
			return;
		}
		refreshCounter++;
	}
}
