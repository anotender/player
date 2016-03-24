package player.model;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import player.model.exceptions.PlaylistLoadingException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by mateu on 14.11.2015.
 */
public class Playlist {

    private final SimpleListProperty<Song> songs = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
    private final SimpleBooleanProperty random = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty saved = new SimpleBooleanProperty(false);
    private final SimpleStringProperty path = new SimpleStringProperty();
    private int lastSongIndex = -1;

    public void add(Song song) {
        if (!songs.contains(song)) {
            songs.add(song);
            saved.set(false);
        }
    }

    public void remove(Song song) {
        songs.remove(song);
        saved.set(false);
    }

    public boolean isRandom() {
        return random.get();
    }

    public void setRandom(boolean random) {
        this.random.set(random);
    }

    public BooleanProperty randomProperty() {
        return random;
    }

    public boolean isSaved() {
        return saved.get();
    }

    public void setSaved(boolean saved) {
        this.saved.set(saved);
    }

    public BooleanProperty savedProperty() {
        return saved;
    }

    public String getPath() {
        return path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public StringProperty pathProperty() {
        return path;
    }

    public SimpleListProperty<Song> getSongs() {
        return songs;
    }

    public Song nextSong() {
        Song tmp;

        if (random.get()) {
            lastSongIndex = new Random().nextInt(songs.size());
            tmp = songs.get(lastSongIndex);
        } else {
            tmp = songs.get(++lastSongIndex % songs.size());
        }

        return tmp;
    }

    public void save(String location) throws FileNotFoundException {
        PrintWriter out = null;
        try {
            out = new PrintWriter(location);

            for (Song song : songs) {
                out.println(song.getPath());
            }

            saved.set(true);
            path.set(location);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void open(File file) throws FileNotFoundException, PlaylistLoadingException {
        Scanner in = null;
        try {
            in = new Scanner(file);

            LinkedList<String> notLoadedSongs = new LinkedList<>();

            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (Files.notExists(Paths.get(line))) {
                    notLoadedSongs.add(line);
                } else {
                    try {
                        Song s = new Song(line);
                        songs.add(s);
                    } catch (Exception e) {
                        notLoadedSongs.add(line);
                    }
                }
            }

            saved.set(true);
            path.set(file.getPath());

            if (!notLoadedSongs.isEmpty()) {
                throw new PlaylistLoadingException(notLoadedSongs);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

}