package eu.cursedmika;

import net.runelite.api.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ItemCollector
{
    private final Client client;
    private HashMap<Integer, Integer> bankItems = null;

    public void snapshotBankItems(ItemContainer itemContainer)
    {
        if(bankItems == null)
        {
            bankItems = new HashMap<>();
        }
        bankItems.clear();

        Item[] items = itemContainer.getItems();

        for (Item item : items)
        {
            if (item == null || item.getId() == -1)
            {
                continue;
            }

            int itemID = item.getId();
            ItemComposition itemComposition = client.getItemDefinition(itemID);
            boolean isPlaceholder = itemComposition.getPlaceholderTemplateId() != -1;
            if(isPlaceholder)
            {
                continue;
            }
            bankItems.put(itemID, item.getQuantity());
        }
    }

    public HashMap<Integer, Integer> getBankItems()
    {
        return bankItems;
    }

    @Inject
    public ItemCollector(Client client)
    {
        this.client = client;
    }

    public HashMap<Integer, Integer> snapshot()
    {
        HashMap<Integer, Integer> currentInventory = new HashMap<>();

        var containers = List.of(InventoryID.INVENTORY, InventoryID.EQUIPMENT);
        for(var container : containers)
        {
            var itemContainer = client.getItemContainer(container);

            if (itemContainer == null) {
                continue;
            }

            Item[] items = itemContainer.getItems();

            for (Item item : items)
            {
                if (item == null || item.getId() == -1)
                {
                    continue;
                }

                int itemID = item.getId();
                createOrAdd(currentInventory, itemID, item.getQuantity());
            }
        }


        return currentInventory;
    }

    private void createOrAdd(HashMap<Integer, Integer> map, int id, int quantity)
    {
        map.put(id, map.getOrDefault(id, 0) + quantity);
    }
}
