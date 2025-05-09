package eu.cursedmika.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;

public class SkillPanel extends JPanel
{
    private final SkillIconManager skillIconManager;
    private final Font xpFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    public SkillPanel(SkillIconManager skillIconManager)
    {
        this.skillIconManager = skillIconManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    public void updateSkills(HashMap<Skill, Integer> skillXP)
    {
        removeAll();

        for (Map.Entry<Skill, Integer> entry : skillXP.entrySet())
        {
            Skill skill = entry.getKey();
            int xp = entry.getValue();

            JPanel skillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            skillRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            BufferedImage skillImage = skillIconManager.getSkillImage(skill);
            JLabel imageLabel = new JLabel(new ImageIcon(skillImage));

            JLabel nameLabel = new JLabel(skill.getName());

            JLabel xpLabel = new JLabel(String.format("%,d XP", xp));
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