package player.model;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;

public class Album {
    private static final LinkedList<Album> ALBUMS = new LinkedList<>();
    private static final SimpleObjectProperty<Image> DEFAULT_COVER = new SimpleObjectProperty<>(new Image("/player/view/defaultCover.png"));

    private final SimpleStringProperty title = new SimpleStringProperty();
    private final SimpleObjectProperty<Image> cover = new SimpleObjectProperty<>(DEFAULT_COVER.get());

    public Album(AudioFile audioFile) throws InvalidDataException, IOException, UnsupportedTagException {
        if (audioFile.getFile().getPath().endsWith("mp3")) {
            Mp3File file = new Mp3File(audioFile.getFile().getPath());

            if (file.hasId3v2Tag()) {
                ID3v2 id3v2tag = file.getId3v2Tag();
                byte[] imageData = id3v2tag.getAlbumImage();
                if (imageData != null) {
                    cover.set(new Image(new ByteArrayInputStream(imageData)));
                }
            }
        }

        title.set(audioFile.getTag().getFirst(FieldKey.ALBUM));
        if (title.get().trim().equals("")) {
            title.set("Unknown");
        }

        ALBUMS.add(this);
    }

    public static LinkedList<Album> getAlbums() {
        return ALBUMS;
    }

    public static SimpleObjectProperty<Image> getDefaultCover() {
        return DEFAULT_COVER;
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

    public void setCover(Image cover) {
        this.cover.set(cover);
    }

    public Image getCover() {
        return cover.get();
    }

    public SimpleObjectProperty<Image> coverProperty() {
        return cover;
    }
}
