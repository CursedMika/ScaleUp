package eu.cursedmika;

import net.runelite.api.Client;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@Singleton
public class SkillCollector
{
    private final Client client;

    @Inject
    public SkillCollector(Client client)
    {
        this.client = client;
    }

    public HashMap<Skill, Integer> snapshot()
    {
        HashMap<Skill, Integer> currentXP = new HashMap<>();
        for(Skill skill : Skill.values())
        {
            currentXP.put(skill, client.getSkillExperience(skill));
        }

        return currentXP;
    }
}
