package eu.cursedmika.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

import eu.cursedmika.TrackingService;
import net.runelite.client.util.ImageUtil;

public class ItemPanel extends JPanel
{
        private final Font quantityFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);

        public ItemPanel()
        {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(5, 5, 5, 5));
        }

        public void updateItems(java.util.List<TrackingService.ComputedSnapshot.ComputedItem> itemChanges)
        {
            removeAll();

            for (var item : itemChanges)
            {
                JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel imageLabel = new JLabel(new ImageIcon(item.getImage()));

                JLabel nameLabel = new JLabel(item.getName());

                JLabel quantityLabel = new JLabel(Integer.toString(Math.abs(item.getDiff())));
                quantityLabel.setFont(quantityFont);

                JLabel arrowLabel = new JLabel();
                BufferedImage arrowIcon;
                if (item.getDiff() > 0)
                {
                    arrowIcon = ImageUtil.loadImageResource(getClass(), "/arrow_up.png");
                    arrowLabel.setForeground(new Color(0, 150, 0));
                }
                else if (item.getDiff() < 0)
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

