package player.model;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.util.LinkedList;

/**
 * Created by mateu on 22.12.2015.
 */
public class PreviousSongs {

    private SimpleListProperty<Song> previousSongs = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));

    public SimpleListProperty<Song> getPreviousSongs() {
        return previousSongs;
    }

    public boolean isEmpty() {
        return previousSongs.isEmpty();
    }

    public void push(Song song) {
        if (previousSongs.contains(song)) {
            previousSongs.remove(song);
        }
        previousSongs.add(0, song);
    }

    public Song pop() {
        Song tmp = previousSongs.get(0);
        previousSongs.remove(0);
        return tmp;
    }

}
