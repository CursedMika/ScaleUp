package eu.cursedmika;

import net.runelite.api.ItemComposition;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class TrackingService
{
    private final SkillCollector skillCollector;
    private final ItemCollector itemCollector;
    private ComputedSnapshot computedSnapshot = null;
    private final AtomicBoolean isTracking = new AtomicBoolean(false);
    private final SkillIconManager skillIconManager;
    private final ItemManager itemManager;

    @Inject
    public TrackingService(SkillCollector skillCollector, ItemCollector itemCollector, SkillIconManager skillIconManager, ItemManager itemManager)
    {
        this.skillCollector = skillCollector;
        this.itemCollector = itemCollector;
        this.skillIconManager = skillIconManager;
        this.itemManager = itemManager;
    }

    private final List<TrackedSnapshot> trackedSnapshots = new ArrayList<>();

    public boolean isTracking()
    {
        return isTracking.get();
    }

    public void startTracking()
    {
        snapshot();
        isTracking.set(true);
    }

    public void stopTracking()
    {
        snapshot();
        isTracking.set(false);
    }

    public void handleBankData()
    {
        trackedSnapshots.forEach(snapshot -> snapshot.addBankData(itemCollector.getBankItems()));
    }

    public void snapshot()
    {
        if(trackedSnapshots.size() == 2)
        {
            trackedSnapshots.remove(1);
        }

        TrackedSnapshot e = new TrackedSnapshot(skillCollector.snapshot(), itemCollector.snapshot(), LocalDateTime.now());

        if(itemCollector.getBankItems() != null)
        {
            e.addBankData(itemCollector.getBankItems());
        }

        trackedSnapshots.add(e);
        recalculate();
    }

    private void recalculate()
    {
        if(trackedSnapshots.size() != 2)
        {
            return;
        }

        var previous = trackedSnapshots.get(0);
        var next = trackedSnapshots.get(1);

        var duration = Math.abs(Duration.between(previous.time, next.time).toSeconds());

        ComputedSnapshot computedSnapshot = new ComputedSnapshot(duration);

        for(var skill : Skill.values())
        {
            var diff = Math.abs(next.trackedSkills.get(skill) - previous.trackedSkills.get(skill));
            if(diff != 0)
            {
                BufferedImage skillImage = skillIconManager.getSkillImage(skill);
                computedSnapshot.computedSkills.add(new ComputedSnapshot.ComputedSkill(skill, diff, skillImage, skill.getName()));
            }
        }

        var allItems = new HashSet<Integer>();
        allItems.addAll(previous.trackedItems.keySet());
        allItems.addAll(next.trackedItems.keySet());

        for(var item : allItems)
        {
            var diff = (next.trackedItems.get(item) == null ? 0 : next.trackedItems.get(item)) - (previous.trackedItems.get(item) == null ? 0 : previous.trackedItems.get(item));
            if(diff != 0)
            {
                ItemComposition itemComposition = itemManager.getItemComposition(item);
                BufferedImage itemImage = itemManager.getImage(item);

                computedSnapshot.computedItems.add(new ComputedSnapshot.ComputedItem(item, diff, itemImage, itemComposition.getName()));
            }
        }

        this.computedSnapshot = computedSnapshot;
    }

    public void clear()
    {
        trackedSnapshots.clear();
        computedSnapshot = null;
    }

    public ComputedSnapshot getComputedSnapshot()
    {
        return computedSnapshot;
    }

    static class TrackedSnapshot
    {
        private final HashMap<Skill, Integer> trackedSkills;
        private final HashMap<Integer, Integer> trackedItems;
        private boolean requestForBankData = true;
        private final LocalDateTime time;

        TrackedSnapshot(HashMap<Skill, Integer> trackedSkills, HashMap<Integer, Integer> trackedItems, LocalDateTime time) {
            this.trackedSkills = trackedSkills;
            this.trackedItems = trackedItems;
            this.time = time;
        }

        public void addBankData(HashMap<Integer, Integer> bankItems)
        {
            if(!requestForBankData)
            {
                return;
            }

            this.requestForBankData = false;
            bankItems.forEach((k,v) -> createOrAdd(this.trackedItems, k, v));
        }

        private void createOrAdd(HashMap<Integer, Integer> map, int id, int quantity)
        {
            map.put(id, map.getOrDefault(id, 0) + quantity);
        }
    }

    public static class ComputedSnapshot
    {
        private final List<ComputedSkill> computedSkills;
        private final List<ComputedItem> computedItems;
        private final long durationInSeconds;

        public ComputedSnapshot(long durationInSeconds)
        {
            this.durationInSeconds = durationInSeconds;
            this.computedSkills = new ArrayList<>();
            this.computedItems = new ArrayList<>();
        }

        public List<ComputedSkill> getComputedSkills()
        {
            return computedSkills;
        }

        public List<ComputedItem> getComputedItems()
        {
            return computedItems;
        }

        public long getDurationInMinutes()
        {
            return durationInSeconds/60;
        }

        public long getDurationInSeconds()
        {
            return durationInSeconds;
        }

        public static class ComputedSkill
        {
            private final Skill skill;
            private final int diff;
            private final BufferedImage image;
            private final String name;

            public ComputedSkill(Skill skill, int diff, BufferedImage image, String name)
            {
                this.skill = skill;
                this.diff = diff;
                this.image = image;
                this.name = name;
            }

            public ComputedSkill(ComputedSkill computedSkill, double scale)
            {
                this(computedSkill.skill, (int) (computedSkill.diff * scale), computedSkill.image, computedSkill.name);
            }

            public String getName() {
                return name;
            }

            public Skill getSkill() {
                return skill;
            }

            public int getDiff() {
                return diff;
            }

            public BufferedImage getImage() {
                return image;
            }
        }

        public static class ComputedItem
        {
            private final int id;
            private final int diff;
            private final String name;
            private final BufferedImage image;

            public ComputedItem(int id, int diff, BufferedImage image, String name)
            {
                this.id = id;
                this.diff = diff;
                this.image = image;
                this.name = name;
            }

            public ComputedItem(ComputedItem computedItem, double scale)
            {
                this(computedItem.id, (int) (computedItem.diff * scale), computedItem.image, computedItem.name);
            }

            public String getName() {
                return name;
            }

            public int getId() {
                return id;
            }

            public int getDiff() {
                return diff;
            }

            public BufferedImage getImage() {
                return image;
            }
        }
    }
}
