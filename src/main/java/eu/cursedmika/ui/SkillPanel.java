package eu.cursedmika.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import eu.cursedmika.TrackingService;

import java.awt.*;

public class SkillPanel extends JPanel
{
    private final Font xpFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    public SkillPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    public void updateSkills(java.util.List<TrackingService.ComputedSnapshot.ComputedSkill> computedSkills)
    {
        removeAll();

        for (var skill : computedSkills)
        {
            JPanel skillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            skillRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel imageLabel = new JLabel(new ImageIcon(skill.getImage()));

            JLabel nameLabel = new JLabel(skill.getName());

            JLabel xpLabel = new JLabel(String.format("%,d XP", skill.getDiff()));
            xpLabel.setFont(xpFont);

            skillRow.add(imageLabel);
            skillRow.add(Box.createHorizontalStrut(5));
            skillRow.add(nameLabel);
            skillRow.add(Box.createHorizontalGlue());
            skillRow.add(xpLabel);

            add(skillRow);
        }

        revalidate();
        repaint();
    }
}