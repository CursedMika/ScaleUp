package eu.cursedmika;

import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;
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

    @Inject
    public TrackingService(SkillCollector skillCollector, ItemCollector itemCollector)
    {
        this.skillCollector = skillCollector;
        this.itemCollector = itemCollector;
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
                computedSnapshot.computedSkills.put(skill, diff);
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
                computedSnapshot.computedItems.put(item, diff);
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
        private HashMap<Skill, Integer> computedSkills;
        private HashMap<Integer, Integer> computedItems;
        private long durationInSeconds;

        public ComputedSnapshot(long durationInSeconds)
        {
            this.durationInSeconds = durationInSeconds;
            this.computedSkills = new HashMap<>();
            this.computedItems = new HashMap<>();
        }

        public HashMap<Skill, Integer> getComputedSkills()
        {
            return computedSkills;
        }

        public HashMap<Integer, Integer> getComputedItems()
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
    }
}
