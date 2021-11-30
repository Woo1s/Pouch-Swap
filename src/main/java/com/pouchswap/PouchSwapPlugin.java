package com.pouchswap;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.Set;

@Slf4j
@PluginDescriptor(
	name = "Pouch Swap",
	description = "Swap the default menu items for Runecrafting pouches to fill when banking, and to empty when not.",
	tags = {"runecrafting", "rune", "pouch", "menu", "swap"}
)
public class PouchSwapPlugin extends Plugin
{


	private static final String FILL_POUCH = "Fill";
	private static final String EMPTY_POUCH = "Empty";

	private static final Set<String> POUCHES = ImmutableSet.of(
			"Small pouch",
			"Medium pouch",
			"Large pouch",
			"Giant pouch");

	@Inject
	private Client client;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Pouch Swap started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Pouch Swap stopped");
	}



	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		//trim tags for target
		String target = Text.removeTags(menuEntryAdded.getTarget());

		//return if the target isn't a pouch
		if(!(POUCHES.contains(target)))
			return;


		MenuEntry[] menuEntries = client.getMenuEntries();

		//determine if banking
		Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankContainer == null || bankContainer.isSelfHidden())
		{
			//find and prioritize the empty option
			for(MenuEntry menuEntry: menuEntries) {

				String option = Text.removeTags(menuEntry.getOption());

				if(option.equals(EMPTY_POUCH) && (menuEntries[(menuEntries.length -1)] != menuEntry))
				{
					prioritizeEntry(menuEntry, false);
					return;
				}
			}

		}
		else{

			//find and prioritize the fill option
			for(MenuEntry menuEntry: menuEntries) {

				String option = Text.removeTags(menuEntry.getOption());

				if(option.equals(FILL_POUCH) && (menuEntries[(menuEntries.length -1)] != menuEntry))
				{
					prioritizeEntry(menuEntry, true);
					return;
				}
			}
		}
	}

	private void prioritizeEntry(MenuEntry entry, boolean banking)
	{
		MenuEntry[] menuEntries = client.getMenuEntries();

		for (int i = menuEntries.length - 1; i >= 0; --i)
		{
			if (entry == menuEntries[i] && i != (menuEntries.length -1))
			{
				if(banking)
					entry.setType(MenuAction.CC_OP.getId());

				//swap with primary entry
				MenuEntry replacedEntry = menuEntries[(menuEntries.length) - 1];
				menuEntries[(menuEntries.length) - 1] = entry;
				menuEntries[(i)] = replacedEntry;

				client.setMenuEntries(menuEntries);
				break;
			}
		}
	}
}
