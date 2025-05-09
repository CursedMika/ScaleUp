package eu.cursedmika.ui;

import eu.cursedmika.TrackingService;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class ScaleUpPanel extends PluginPanel
{
    private final TrackingService trackingService;

    private final JPanel computedPanel = new JPanel();
    private final JButton snapshotButton = new JButton("SNAPSHOT");
    private final ItemManager itemManager;
    private final SkillIconManager skillIconManager;
    private final ClientThread clientThread;

    public ScaleUpPanel(TrackingService trackingService, ItemManager itemManager, SkillIconManager skillIconManager, ClientThread clientThread)
    {
        this.trackingService = trackingService;
        this.itemManager = itemManager;
        this.skillIconManager = skillIconManager;
        this.clientThread = clientThread;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);

        add(setupPanel(), c);

        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        add(computedPanel, c);
    }

    private ActionListener startSnapshot()
    {
        return (e) -> {
            snapshotButton.setText("STOP");
            Arrays.stream(snapshotButton.getActionListeners()).forEach(snapshotButton::removeActionListener);
            snapshotButton.addActionListener(stopSnapshot());
            clientThread.invokeAtTickEnd(trackingService::startTracking);
        };
    }

    private ActionListener stopSnapshot()
    {
        return (e) -> {
            snapshotButton.setText("SNAPSHOT");
            Arrays.stream(snapshotButton.getActionListeners()).forEach(snapshotButton::removeActionListener);
            snapshotButton.addActionListener(startSnapshot());
            clientThread.invokeAtTickEnd(trackingService::stopTracking);
        };
    }

    private JPanel setupPanel()
    {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridLayout(1, 1, 0, 0));
        computedPanel.setLayout(new BoxLayout(computedPanel, BoxLayout.Y_AXIS));
        snapshotButton.addActionListener(startSnapshot());

        JButton clear = new JButton("CLEAR");
        clear.addActionListener(e -> clientThread.invokeAtTickEnd(() -> {
            trackingService.stopTracking();
            trackingService.clear();
            stopSnapshot().actionPerformed(null);
            computedPanel.removeAll();
            computedPanel.repaint();
            computedPanel.revalidate();
            computedPanel.getParent().repaint();
        }));

        wrapper.add(snapshotButton);
        wrapper.add(clear);

        return wrapper;
    }

    public void updateUI(TrackingService.ComputedSnapshot computedSnapshot)
    {
        if(computedSnapshot == null)
        {
            return;
        }
        computedPanel.removeAll();
        ItemPanel itemPanel = new ItemPanel(itemManager);
        SkillPanel skillPanel = new SkillPanel(skillIconManager);
        JLabel snapshotDuration = new JLabel("DURATION: "+computedSnapshot.getDurationInMinutes()+" minutes");;
        SliderPanel sliderPanel = new SliderPanel();
        JButton scale = new JButton("SCALE TO 1 HOUR");

        itemPanel.updateItems(computedSnapshot.getComputedItems());
        skillPanel.updateSkills(computedSnapshot.getComputedSkills());

        sliderPanel.addChangeListener(e ->
                clientThread.invokeAtTickEnd(() -> {
                    double scaleFactor = sliderPanel.getSliderValue();

                    TrackingService.ComputedSnapshot newSnapshot = new TrackingService.ComputedSnapshot((int)(computedSnapshot.getDurationInSeconds() * scaleFactor));
                    computedSnapshot.getComputedItems().forEach((k,v) -> newSnapshot.getComputedItems().put(k, (int)(v*scaleFactor)));
                    computedSnapshot.getComputedSkills().forEach((k,v) -> newSnapshot.getComputedSkills().put(k, (int)(v*scaleFactor)));
                    itemPanel.updateItems(newSnapshot.getComputedItems());
                    skillPanel.updateSkills(newSnapshot.getComputedSkills());
                    snapshotDuration.setText("DURATION: "+newSnapshot.getDurationInMinutes()+" minutes");;
                    computedPanel.repaint();
                    computedPanel.revalidate();
                    computedPanel.getParent().repaint();
                }));

        scale.addActionListener(e -> sliderPanel.setSliderValue(3600D/computedSnapshot.getDurationInSeconds()));
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        skillPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        snapshotDuration.setAlignmentX(Component.LEFT_ALIGNMENT);
        sliderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scale.setAlignmentX(Component.LEFT_ALIGNMENT);
        computedPanel.add(Box.createVerticalStrut(5));
        computedPanel.add(itemPanel);
        computedPanel.add(Box.createVerticalStrut(5));
        computedPanel.add(skillPanel);
        computedPanel.add(Box.createVerticalStrut(5));
        computedPanel.add(snapshotDuration);
        computedPanel.add(Box.createVerticalStrut(5));
        computedPanel.add(sliderPanel);
        computedPanel.add(Box.createVerticalStrut(5));
        computedPanel.add(scale);
        computedPanel.repaint();
        computedPanel.revalidate();
        computedPanel.getParent().repaint();

    }
}
