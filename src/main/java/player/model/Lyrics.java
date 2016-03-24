package player.model;

import player.model.exceptions.InternetConnectionException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by anotender on 04.12.15.
 */
public class Lyrics {

    private String lyrics;

    private static boolean isInternetConnectionAvailible() {
        Socket s = new Socket();
        InetSocketAddress address = new InetSocketAddress("www.google.com", 80);

        try {
            s.connect(address, 3000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                s.close();
            } catch (IOException e) {
            }
        }
    }

    public static Lyrics loadLyrics(String artist, String title) throws IOException, InternetConnectionException {
        if (!isInternetConnectionAvailible()) {
            throw new InternetConnectionException();
        }

        Scanner in = null;
        Lyrics newLyrics = new Lyrics();

        try {
            URL queryURL = new URL("http://lyrics.wikia.com/api.php?action=lyrics&artist=" + artist.replaceAll(" ", "%20") + "&song=" + title.replaceAll(" ", "%20") + "&fmt=xml");
            URL lyricsURL = null;
            in = new Scanner(queryURL.openStream());

            //sprawdzenie czy tekst jest dostepny
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.startsWith("<lyrics>")) {
                    line = line.replaceAll("<.*?>", "");
                    if (line.equals("Not found")) {
                        return newLyrics;
                    }
                }

                if (line.startsWith("<url>")) {
                    lyricsURL = new URL(line.replaceAll("<.*?>", "") + "?action=edit");
                    break;
                }
            }

            in.close();
            in = new Scanner(lyricsURL.openStream());

            boolean isReadingLyrics = false;

            String downloadedLyrics = "";

            //pobranie tekstu
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.contains("&lt;lyrics>")) {
                    isReadingLyrics = true;
                    line = line.replace("&lt;lyrics>", "");
                    if (line != "") {
                        downloadedLyrics += (line + "\n");
                    }
                    continue;
                } else if (line.contains("&lt;/lyrics>")) {
                    line = line.replace("&lt;/lyrics>", "");
                    if (line != "") {
                        downloadedLyrics += (line + "\n");
                    }
                    break;
                }
                if (!isReadingLyrics && !line.contains("&lt;lyrics>")) {
                    continue;
                } else if (isReadingLyrics) {
                    downloadedLyrics += (line + "\n");
                }
            }

            newLyrics.setLyrics(downloadedLyrics);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return newLyrics;
    }

    public Lyrics() {
        lyrics = "Lyrics not found";
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getLyrics() {
        return lyrics;
    }

}
