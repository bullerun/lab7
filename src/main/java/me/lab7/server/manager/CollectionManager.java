package me.lab7.server.manager;

import me.lab7.common.data.LabWork;

import java.time.LocalDate;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * class of working with the TreeSet collection
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class CollectionManager {
    private final NavigableSet<LabWork> labWorks = new TreeSet<>();
    private final LocalDate creatingCollection;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public CollectionManager() {
        creatingCollection = LocalDate.now();
    }


    public void addToCollection(LabWork newLab) {
        lock.writeLock().lock();
        labWorks.add(newLab);
        lock.writeLock().unlock();
    }

    public NavigableSet<LabWork> getLabWork() {
        return labWorks;
    }

    public void clearCollection(Long client) {
        lock.writeLock().lock();
        labWorks.removeIf(labWork -> labWork.getOwnerID().equals(client));
        lock.writeLock().unlock();
    }

    public void removeByID(Long removeLabWorkId, Long client) {
        lock.writeLock().lock();
        labWorks.removeIf(i -> i.getId() == removeLabWorkId && i.getOwnerID().equals(client));
        lock.writeLock().unlock();

    }

    public void removeGreater( Long id, Long client) {
        lock.writeLock().lock();
        labWorks.removeIf(i-> i.getId()>id && i.getOwnerID().equals(client));
        lock.writeLock().unlock();
    }

    public void removeLower( Long id, Long client) {
        lock.writeLock().lock();
        labWorks.removeIf(i-> i.getId()<id && i.getOwnerID().equals(client));
        lock.writeLock().unlock();
    }
    public LocalDate getCreatingCollection() {
        return creatingCollection;
    }

    @Override
    public String toString() {
        return "labWorks=" + labWorks;
    }


    public void initializeData(NavigableSet<LabWork> collection) {
        labWorks.addAll(collection);
    }
}
