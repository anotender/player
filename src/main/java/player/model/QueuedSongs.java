package player.model;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.util.LinkedList;

/**
 * Created by mateu on 22.12.2015.
 */
public class QueuedSongs {

    private SimpleListProperty<Song> queue = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));

    public void add(Song song) {
        if (queue.isEmpty() || !queue.get(queue.getSize() - 1).equals(song)) {
            queue.add(song);
        }
    }

    public Song poll() {
        Song tmp = queue.get(queue.getSize() - 1);
        queue.remove(tmp);
        return tmp;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public SimpleListProperty<Song> getQueuedSongs() {
        return queue;
    }

}
