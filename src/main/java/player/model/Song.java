package player.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import player.model.exceptions.InternetConnectionException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;

/**
 * Created by mateu on 13.11.2015.
 */
public class Song {

    //private static final MediaPlayer MEDIA_PLAYER = new MediaPlayer(null);

    private final SimpleStringProperty title = new SimpleStringProperty();
    private final SimpleStringProperty artist = new SimpleStringProperty();
    private final SimpleStringProperty path = new SimpleStringProperty();
    private final SimpleStringProperty filename = new SimpleStringProperty();
    private final SimpleDoubleProperty totalDuration = new SimpleDoubleProperty();
    private Album album;
    private Lyrics lyrics;
    private MediaPlayer song;

    public Song(String source) throws Exception {
        File file = new File(source);
        path.set(source);
        filename.set(file.getName());
        song = new MediaPlayer(new Media(file.toURI().toString()));

        AudioFile audioFile = AudioFileIO.read(file);

        title.set(audioFile.getTag().getFirst(FieldKey.TITLE));
        if (title.getValue().trim().equals("")) {
            title.set("Unknown");
        }

        artist.set(audioFile.getTag().getFirst(FieldKey.ARTIST));
        if (artist.getValue().trim().equals("")) {
            artist.set("Unknown");
        }

        //nie zawsze ta metoda zwraca dobra dlugosc piosenki
        totalDuration.set(audioFile.getAudioHeader().getTrackLength());

        lyrics = new Lyrics();

        //przypisanie albumu lub zrobienie nowego
        album = null;
        String albumTitle = audioFile.getTag().getFirst(FieldKey.ALBUM);
        if (albumTitle == null || albumTitle.trim().equals("")) {
            albumTitle = "Unknown";
        }

        for (Album a : Album.getAlbums()) {
            if (a.getTitle().equals(albumTitle)) {
                album = a;
                break;
            }
        }

        if (album == null) {
            album = new Album(audioFile);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        return path.equals(song.path);

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        if (!artist.get().equals("Unknown") && !title.get().equals("Unknown")) {
            return artist.get() + " - " + title.get();
        } else {
            return new File(path.get()).getName();
        }
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getArtist() {
        return artist.get();
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public String getPath() {
        return path.get();
    }

    public StringProperty pathProperty() {
        return path;
    }

    public String getFilename() {
        return filename.get();
    }

    public StringProperty filenameProperty() {
        return filename;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public void loadLyrics() throws IOException, InternetConnectionException {
        lyrics = Lyrics.loadLyrics(artist.get(), title.get());
    }

    public void setLyrics(String lyrics) {
        this.lyrics.setLyrics(lyrics);
    }

    public String getLyrics() {
        return lyrics.getLyrics();
    }

    public double getTotalDuration() {
        return totalDuration.get();
    }

    public SimpleDoubleProperty totalDurationProperty() {
        return totalDuration;
    }

    public StringProperty totalDurationStringProperty() {
        return new SimpleStringProperty(String.format("%02d:%02d", (int) totalDuration.get() / 60, (int) totalDuration.get() % 60));
    }

    public double getVolume() {
        return song.getVolume();
    }

    public void setVolume(double value) {
        song.setVolume(value);
    }

    public MediaPlayer mediaPlayerProperty() {
        return song;
    }

    public void play() {
        song.play();
    }

    public void pause() {
        song.pause();
    }

    public void stop() {
        song.stop();
    }

    public void seek(double seekTime) {
        song.seek(Duration.seconds(seekTime));
    }

}
