package eu.cursedmika.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.ImageUtil;

public class ItemPanel extends JPanel
{
        private final ItemManager itemManager;
        private final Font quantityFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);

        public ItemPanel(ItemManager itemManager)
        {
            this.itemManager = itemManager;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(5, 5, 5, 5));
        }

        public void updateItems(HashMap<Integer, Integer> itemChanges)
        {
            removeAll();

            for (Map.Entry<Integer, Integer> entry : itemChanges.entrySet())
            {
                int itemId = entry.getKey();
                int quantityChange = entry.getValue();

                JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);

                ItemComposition itemComposition = itemManager.getItemComposition(itemId);
                BufferedImage itemImage = itemManager.getImage(itemId);
                JLabel imageLabel = new JLabel(new ImageIcon(itemImage));

                JLabel nameLabel = new JLabel(itemComposition.getName());

                JLabel quantityLabel = new JLabel(Integer.toString(Math.abs(quantityChange)));
                quantityLabel.setFont(quantityFont);

                JLabel arrowLabel = new JLabel();
                BufferedImage arrowIcon;
                if (quantityChange > 0)
                {
                    arrowIcon = ImageUtil.loadImageResource(getClass(), "/arrow_up.png");
                    arrowLabel.setForeground(new Color(0, 150, 0));
                }
                else if (quantityChange < 0)
                {
                    arrowIcon = ImageUtil.loadImageResource(getClass(), "/arrow_down.png");
                    arrowLabel.setForeground(new Color(150, 0, 0));
                }
                else
                {
                    arrowIcon = null;
                }

                if (arrowIcon != null)
                {
                    arrowLabel.setIcon(new ImageIcon(arrowIcon));
                }

                itemRow.add(imageLabel);
                itemRow.add(Box.createHorizontalStrut(5));
                itemRow.add(nameLabel);
                itemRow.add(Box.createHorizontalGlue());
                itemRow.add(quantityLabel);
                if (arrowIcon != null)
                {
                    itemRow.add(arrowLabel);
                }

                add(itemRow);
            }

            revalidate();
            repaint();
        }
}

