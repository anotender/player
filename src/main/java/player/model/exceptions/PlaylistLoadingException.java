package player.model.exceptions;

import java.util.LinkedList;

/**
 * Created by mateu on 14.02.2016.
 */
public class PlaylistLoadingException extends Exception {

    private LinkedList<String> notLoadedSongs;

    public PlaylistLoadingException(LinkedList<String> notLoadedSongs) {
        this.notLoadedSongs = notLoadedSongs;
    }

    public LinkedList<String> getNotLoadedSongs() {
        return notLoadedSongs;
    }
}
