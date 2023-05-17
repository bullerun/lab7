package me.lab7.server.manager;

import me.lab7.common.data.LabWork;

import java.time.LocalDate;
import java.util.NavigableSet;
import java.util.TreeSet;
/**
 * class of working with the TreeSet collection
 * @author Nikita and Vlad
 * @version 0.1
 */
public class CollectionManager {
    private final NavigableSet<LabWork> labWorks = new TreeSet<>();
    private final LocalDate creatingCollection;

    public CollectionManager() {
        creatingCollection = LocalDate.now();
    }


    public void addToCollection(LabWork newLab) {
        newLab.setId(generateNextId());
        labWorks.add(newLab);
    }

    public long generateNextId() {
        if (labWorks.isEmpty()) return 1L;
        return labWorks.last().getId() + 1;
    }

    public NavigableSet<LabWork> getLabWork() {
        return labWorks;
    }

    public void clearCollection() {
        this.labWorks.clear();
    }

    public void removeLabWork(LabWork removeLabWork) {
        labWorks.remove(removeLabWork);
    }
    public void removeGreater(LabWork removeLabWork) {
        labWorks.removeAll(labWorks.tailSet(removeLabWork));
    }
    public void removeLower(LabWork removeLabWork) {
        labWorks.removeAll(labWorks.headSet(removeLabWork));

    }

    public LabWork getElementById(long id) {
        return labWorks.stream().filter(i -> i.getId() == id).findFirst().orElse(null);

    }

    public LocalDate getCreatingCollection() {
        return creatingCollection;
    }

    @Override
    public String toString() {
        return "labWorks=" + labWorks;
    }

    public void addToCollectionFromFile(LabWork newLab) {
        labWorks.add(newLab);
    }

    public void initializeData(NavigableSet<LabWork> collection) {
        labWorks.addAll(collection);
    }
}
