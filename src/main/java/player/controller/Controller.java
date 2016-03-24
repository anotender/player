package player.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import player.model.*;
import player.model.exceptions.PlaylistLoadingException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by mateu on 14.11.2015.
 */
public class Controller implements Initializable {

    private Song song;
    private Playlist playlist;
    private QueuedSongs queue;
    private PreviousSongs previousSongs;
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setOnCloseRequest(event -> {
            if (!playlist.getSongs().isEmpty() && !playlist.isSaved()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText("Want to save your playlist?");

                ButtonType buttonTypeSave = new ButtonType("Save");
                ButtonType buttonTypeDontSave = new ButtonType("Dont't save");
                ButtonType buttonTypeCancel = new ButtonType("Cancel");

                alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDontSave, buttonTypeCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonTypeSave) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName("playlist");
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlists (*.playlist)", "*.playlist"));
                    File selected = fileChooser.showSaveDialog(primaryStage);
                    if (selected != null) {
                        savePlaylist(selected);
                    }
                } else if (result.get() == buttonTypeDontSave) {
                    // user chose not to save
                } else {
                    event.consume();
                }
            }
        });
    }

    @FXML
    private MenuItem openSongMenuItem;

    @FXML
    private void handleOpenSongMenuItem() {
        try {
            if (song != null) {
                previousSongs.push(song);
            }
            openSong();
            playSong();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @FXML
    private MenuItem newPlaylistMenuItem;

    @FXML
    private void handleNewPlaylistMenuItem() {
        try {
            newPlaylist();
            playlistTableView.itemsProperty().bind(playlist.getSongs());
        } catch (Exception e) {
            //
        }
    }

    @FXML
    private MenuItem openPlaylistMenuItem;

    @FXML
    private void handleOpenPlaylistMenuItem() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlists (*.playlist)", "*.playlist"));
            File selected = fileChooser.showOpenDialog(primaryStage);

            if (selected != null) {
                openPlaylist(selected);
            }

            playlistTableView.itemsProperty().bind(playlist.getSongs());
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @FXML
    private MenuItem savePlaylistMenuItem;

    @FXML
    private void handleSavePlaylistMenuItem() {
        savePlaylist(new File(playlist.getPath()));
    }

    @FXML
    private MenuItem addSongToPlaylistMenuItem;

    @FXML
    private void handleAddSongToPlaylistMenuItem() {
        try {
            addSongToPlaylist();
        } catch (Exception e) {
            //
        }
    }

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private void handleClose() {
        if (!playlist.getSongs().isEmpty() && !playlist.isSaved()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Want to save your playlist?");

            ButtonType buttonTypeSave = new ButtonType("Save");
            ButtonType buttonTypeDontSave = new ButtonType("Dont't save");
            ButtonType buttonTypeCancel = new ButtonType("Cancel");

            alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDontSave, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeSave) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("playlist");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlists (*.playlist)", "*.playlist"));
                File selected = fileChooser.showSaveDialog(primaryStage);
                if (selected != null) {
                    savePlaylist(selected);
                }
                Platform.exit();
            } else if (result.get() == buttonTypeDontSave) {
                Platform.exit();
            }
        } else {
            Platform.exit();
        }
    }

    @FXML
    private Button randomButton;

    @FXML
    private void handleRandomButton() {
        playlist.setRandom(!playlist.isRandom());
        randomButton.setStyle(playlist.isRandom() ? "-fx-base: #b6e7c9;" : null);
    }

    @FXML
    private Button addSongButton;

    @FXML
    private void handleAddSongButton() {
        try {
            addSongToPlaylist();
        } catch (Exception e) {
            //
        }
    }

    @FXML
    private Button nextSongButton;

    @FXML
    private void handleNextSongButton() {
        if (song != null) {
            previousSongs.push(song);
        }
        if (!playlist.getSongs().isEmpty()) {
            openSong(playlist.nextSong());
            playSong();
        } else if (!queue.isEmpty()) {
            openSong(queue.poll());
            playSong();
        }
    }

    @FXML
    private Button previousSongButton;

    @FXML
    private void handlePreviousSongButton() {
        if (!previousSongs.isEmpty()) {
            openSong(previousSongs.pop());
            playSong();
        }
    }

    @FXML
    private Button stopButton;

    @FXML
    private void handleStopButton() {
        if (song != null) {
            song.stop();

            ImageView icon = new ImageView("/player/view/play.png");

            icon.setFitWidth(40);
            icon.setFitHeight(40);
            playPauseButton.setGraphic(icon);
        }
    }

    @FXML
    private Button playPauseButton;

    @FXML
    private void handlePlayPauseButton() {
        if (song != null) {
            ImageView icon;

            if (song.mediaPlayerProperty().getStatus() == MediaPlayer.Status.PLAYING) {
                song.pause();
                icon = new ImageView("/player/view/play.png");
            } else {
                song.play();
                icon = new ImageView("/player/view/pause.png");
            }

            icon.setFitWidth(40);
            icon.setFitHeight(40);
            playPauseButton.setGraphic(icon);
        }
    }

    @FXML
    private Button muteButton;

    @FXML
    private void handleMuteButton() {
        if (song != null) {
            song.mediaPlayerProperty().setMute(!song.mediaPlayerProperty().isMute());
            volumeSlider.setDisable(song.mediaPlayerProperty().isMute());

            Image image = new Image(song.mediaPlayerProperty().isMute() ? "/player/view/muteOn.png" : "/player/view/muteOff.png");
            ImageView icon = new ImageView(image);
            icon.setFitWidth(40);
            icon.setFitHeight(40);
            muteButton.setGraphic(icon);
        }
    }

    @FXML
    private Slider volumeSlider;

    @FXML
    private Slider progressSlider;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab playlistTab;

    @FXML
    private TableView<Song> playlistTableView;

    @FXML
    private TableView<Song> queueTableView;

    @FXML
    private TableView<Song> previousSongsTableView;

    @FXML
    private TableColumn<Song, String> playlistFilenameColumn;

    @FXML
    private TableColumn<Song, String> playlistArtistColumn;

    @FXML
    private TableColumn<Song, String> playlistTitleColumn;

    @FXML
    private TableColumn<Song, String> playlistAlbumColumn;

    @FXML
    private TableColumn<Song, String> playlistDurationColumn;

    @FXML
    private TableColumn<Song, String> queueFilenameColumn;

    @FXML
    private TableColumn<Song, String> queueArtistColumn;

    @FXML
    private TableColumn<Song, String> queueTitleColumn;

    @FXML
    private TableColumn<Song, String> queueAlbumColumn;

    @FXML
    private TableColumn<Song, String> queueDurationColumn;

    @FXML
    private TableColumn<Song, String> previousSongsFilenameColumn;

    @FXML
    private TableColumn<Song, String> previousSongsArtistColumn;

    @FXML
    private TableColumn<Song, String> previousSongsTitleColumn;

    @FXML
    private TableColumn<Song, String> previousSongsAlbumColumn;

    @FXML
    private TableColumn<Song, String> previousSongsDurationColumn;

    @FXML
    private TextArea lyricsArea;

    @FXML
    private ProgressIndicator lyricsProgressIndicator;

    @FXML
    private TextField searchTextField;

    @FXML
    private HBox controlBox;

    @FXML
    private Label albumLabel;

    @FXML
    private Label artistLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private ImageView coverImageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        song = null;
        playlist = new Playlist();

        //bind playlist control buttons
        previousSongButton.disableProperty().bind(playlist.getSongs().emptyProperty());
        nextSongButton.disableProperty().bind(playlist.getSongs().emptyProperty());
        addSongToPlaylistMenuItem.disableProperty().bind(playlist.pathProperty().isEmpty());
        addSongButton.disableProperty().bind(playlist.pathProperty().isEmpty());
        savePlaylistMenuItem.disableProperty().bind(playlist.savedProperty().or(playlist.getSongs().emptyProperty()));
        randomButton.disableProperty().bind(playlist.getSongs().emptyProperty());

        //initialize queue
        queue = new QueuedSongs();
        queueTableView.itemsProperty().bind(queue.getQueuedSongs());

        //initialize previous songs list
        previousSongs = new PreviousSongs();
        previousSongsTableView.itemsProperty().bind(previousSongs.getPreviousSongs());

        //set playlistTableView cells appearance
        playlistFilenameColumn.setCellValueFactory(param -> param.getValue().filenameProperty());
        playlistArtistColumn.setCellValueFactory(param -> param.getValue().artistProperty());
        playlistTitleColumn.setCellValueFactory(param -> param.getValue().titleProperty());
        playlistAlbumColumn.setCellValueFactory(param -> param.getValue().getAlbum().titleProperty());
        playlistDurationColumn.setCellValueFactory(param -> param.getValue().totalDurationStringProperty());

        //set queueTableView cells appearance
        queueFilenameColumn.setCellValueFactory(param -> param.getValue().filenameProperty());
        queueArtistColumn.setCellValueFactory(param -> param.getValue().artistProperty());
        queueTitleColumn.setCellValueFactory(param -> param.getValue().titleProperty());
        queueAlbumColumn.setCellValueFactory(param -> param.getValue().getAlbum().titleProperty());
        queueDurationColumn.setCellValueFactory(param -> param.getValue().totalDurationStringProperty());

        //set previousSongsTableView cells appearance
        previousSongsFilenameColumn.setCellValueFactory(param -> param.getValue().filenameProperty());
        previousSongsArtistColumn.setCellValueFactory(param -> param.getValue().artistProperty());
        previousSongsTitleColumn.setCellValueFactory(param -> param.getValue().titleProperty());
        previousSongsAlbumColumn.setCellValueFactory(param -> param.getValue().getAlbum().titleProperty());
        previousSongsDurationColumn.setCellValueFactory(param -> param.getValue().totalDurationStringProperty());

        //handle searchTextField
        SimpleListProperty<Song> filteredSongs = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            TableView<Song> selectedTable = (TableView<Song>) selectedTab.getContent();

            SimpleListProperty<Song> songs;
            if (selectedTab.getText().toLowerCase().equals("playlist")) {
                songs = playlist.getSongs();
            } else if (selectedTab.getText().toLowerCase().equals("queue")) {
                songs = queue.getQueuedSongs();
            } else {
                songs = previousSongs.getPreviousSongs();
            }

            if (newValue.isEmpty()) {
                selectedTable.itemsProperty().bind(songs);
            } else if (!newValue.equals(oldValue)) {
                filteredSongs.clear();

                for (Song s : songs) {
                    if (s.getArtist().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredSongs.add(s);
                    } else if (s.getTitle().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredSongs.add(s);
                    } else if (s.getFilename().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredSongs.add(s);
                    } else if (s.getAlbum().getTitle().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredSongs.add(s);
                    }
                }

                selectedTable.itemsProperty().bind(filteredSongs);
            }
        });
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                searchTextField.clear();
                filteredSongs.clear();
            }
        });

        //handle playlistTableView events
        playlistTableView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !playlist.getSongs().isEmpty()) {
                if (song != null) {
                    previousSongs.push(song);
                }
                openSong(playlistTableView.getSelectionModel().getSelectedItem());
                playSong();
//                playlist.setCurrentSong(song);
            }
        });
        playlistTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && !playlist.getSongs().isEmpty()) {
                if (song != null) {
                    previousSongs.push(song);
                }
                openSong(playlistTableView.getSelectionModel().getSelectedItem());
                playSong();
//                playlist.setCurrentSong(song);
            }
        });

        //handle queueTableView events
        queueTableView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !queue.isEmpty()) {
                if (song != null) {
                    previousSongs.push(song);
                }
                Song chosen = queueTableView.getSelectionModel().getSelectedItem();
                openSong(chosen);
                queue.getQueuedSongs().remove(chosen);
                playSong();
            }
        });
        queueTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && !queue.isEmpty()) {
                if (song != null) {
                    previousSongs.push(song);
                }
                Song chosen = queueTableView.getSelectionModel().getSelectedItem();
                openSong(chosen);
                queue.getQueuedSongs().remove(chosen);
                playSong();
            }
        });

        //handle previousSongsTableView events
        previousSongsTableView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !previousSongs.isEmpty()) {
                if (song != null) {
                    previousSongs.push(song);
                }
                Song chosen = previousSongsTableView.getSelectionModel().getSelectedItem();
                openSong(chosen);
                previousSongs.getPreviousSongs().remove(chosen);
                playSong();
            }
        });
        previousSongsTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && !previousSongs.isEmpty()) {
                if (song != null) {
                    previousSongs.push(song);
                }
                Song chosen = previousSongsTableView.getSelectionModel().getSelectedItem();
                openSong(chosen);
                previousSongs.getPreviousSongs().remove(chosen);
                playSong();
            }
        });

        //drag and drop over playlistView (add new songs to playlist or open a playlist)
        playlistTableView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            } else {
                event.consume();
            }
        });
        playlistTableView.setOnDragEntered(event -> playlistTableView.setEffect(new BoxBlur()));
        playlistTableView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    try {
                        if (file.getName().endsWith(".mp3")) {
                            if (playlist.getPath() != null) {
                                playlist.add(new Song(file.getPath()));
                            } else {
                                Alert a = new Alert(Alert.AlertType.INFORMATION);
                                a.setHeaderText("No playlist opened");
                                a.setContentText("Open or create a playlist first");
                                a.showAndWait();
                                break;
                            }
                        } else if (file.getName().endsWith(".playlist")) {
                            openPlaylist(file);
                            playlistTableView.itemsProperty().bind(playlist.getSongs());
                            break;
                        }
                    } catch (Exception e) {
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
        playlistTableView.setOnDragExited(event -> playlistTableView.setEffect(null));

        //drag and drop over controlBox (open only one song)
        controlBox.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            } else {
                event.consume();
            }
        });
        controlBox.setOnDragEntered(event -> controlBox.setEffect(new BoxBlur()));
        controlBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;

                File file = db.getFiles().get(0);
                if (file.getName().endsWith(".mp3")) {
                    try {
                        openSong(new Song(file.getPath()));
                        playSong();
                    } catch (Exception e) {
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
        controlBox.setOnDragExited(event -> controlBox.setEffect(null));

        //playlistTableView context menu
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(event -> playlist.remove(playlistTableView.getSelectionModel().getSelectedItem()));
        MenuItem queueMenuItem = new MenuItem("Queue");
        queueMenuItem.setOnAction(event -> queue.add(playlistTableView.getSelectionModel().getSelectedItem()));
        playlistTableView.setContextMenu(new ContextMenu(deleteMenuItem, queueMenuItem));

        //drag and drop over lyricsArea
        //to do

        //volumeSlider handler
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (song != null) {
                song.setVolume(volumeSlider.getValue());
            }
        });

        //progressSlider handler
        progressSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (!progressSlider.isValueChanging()) {
                double minChange = 0.5;
                double currentTime = song.mediaPlayerProperty().getCurrentTime().toSeconds();
                if (Math.abs(currentTime - newValue.doubleValue()) > minChange && song != null) {
                    song.seek(newValue.doubleValue());
                }
            }
        });
        progressSlider.valueChangingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue && song != null) {
                song.seek(progressSlider.getValue());
            }
        });

        //set defaultCover
        coverImageView.setImage(Album.getDefaultCover().get());
    }

    private void openSong() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Songs (*.mp3)", "*.mp3"));
        File selected = fileChooser.showOpenDialog(primaryStage);
        if (selected != null) {
            boolean mute = false;

            if (song != null) {
                mute = song.mediaPlayerProperty().isMute();
                song.stop();
            }

            song = new Song(selected.getPath());
            song.mediaPlayerProperty().setMute(mute);

            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> {
                        lyricsArea.textProperty().unbind();
                        lyricsArea.setText(null);
                        lyricsProgressIndicator.setVisible(true);
                    });
                    Controller.this.song.loadLyrics();
                    Platform.runLater(() -> {
                        lyricsArea.textProperty().bind(new SimpleStringProperty(song.getLyrics()));
                        lyricsProgressIndicator.setVisible(false);
                    });
                    return null;
                }
            }).start();

            coverImageView.imageProperty().bind(song.getAlbum().coverProperty());
            totalTimeLabel.textProperty().bind(song.totalDurationStringProperty());
            titleLabel.textProperty().bind(song.titleProperty());
            albumLabel.textProperty().bind(song.getAlbum().titleProperty());
            artistLabel.textProperty().bind(song.artistProperty());
            primaryStage.setTitle(song.toString());

            //handle song
            song.mediaPlayerProperty().setOnEndOfMedia(() -> {
                //song.stop();
                if (!playlist.getSongs().isEmpty()) {
                    if (song != null) {
                        previousSongs.push(song);
                    }
                    openSong(queue.isEmpty() ? playlist.nextSong() : queue.poll());
                    playSong();
                } else {
                    progressSlider.setValue(0);
                    progressSlider.setMax(song.getTotalDuration());
                    song.play();
                }
            });
            song.mediaPlayerProperty().currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!progressSlider.isValueChanging()) {
                    currentTimeLabel.setText(String.format("%02d:%02d", (int) newValue.toSeconds() / 60, (int) newValue.toSeconds() % 60));
                    progressSlider.setValue(newValue.toSeconds());
                }
            });

            //enable control buttons
            stopButton.setDisable(false);
            playPauseButton.setDisable(false);
            muteButton.setDisable(false);
            volumeSlider.setDisable(mute);
            progressSlider.setDisable(false);

            //prepare progressSlider
            progressSlider.setMax(song.getTotalDuration());
            progressSlider.setValue(0);
        }
    }

    private void openSong(Song song) {
        if (song != null && !song.equals(this.song)) {
            boolean mute = false;

            if (this.song != null) {
                mute = this.song.mediaPlayerProperty().isMute();
                this.song.stop();
            }

            this.song = song;
            this.song.mediaPlayerProperty().setMute(mute);

            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> {
                        lyricsArea.textProperty().unbind();
                        lyricsArea.setText(null);
                        lyricsProgressIndicator.setVisible(true);
                    });
                    Controller.this.song.loadLyrics();
                    Platform.runLater(() -> {
                        lyricsArea.textProperty().bind(new SimpleStringProperty(Controller.this.song.getLyrics()));
                        lyricsProgressIndicator.setVisible(false);
                    });
                    return null;
                }
            }).start();

            coverImageView.imageProperty().bind(this.song.getAlbum().coverProperty());
            totalTimeLabel.textProperty().bind(this.song.totalDurationStringProperty());
            titleLabel.textProperty().bind(this.song.titleProperty());
            albumLabel.textProperty().bind(this.song.getAlbum().titleProperty());
            artistLabel.textProperty().bind(this.song.artistProperty());
            primaryStage.setTitle(this.song.toString());

            //handle song ending
            this.song.mediaPlayerProperty().setOnEndOfMedia(() -> {
                //this.song.stop();
                if (!playlist.getSongs().isEmpty()) {
                    if (this.song != null) {
                        previousSongs.push(this.song);
                    }
                    openSong(queue.isEmpty() ? playlist.nextSong() : queue.poll());
                    playSong();
                } else {
                    progressSlider.setValue(0);
                    progressSlider.setMax(song.getTotalDuration());
                    this.song.play();
                }
            });
            this.song.mediaPlayerProperty().currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!progressSlider.isValueChanging()) {
                    currentTimeLabel.setText(String.format("%02d:%02d", (int) newValue.toSeconds() / 60, (int) newValue.toSeconds() % 60));
                    progressSlider.setValue(newValue.toSeconds());
                }
            });

            //enable song control buttons
            stopButton.setDisable(false);
            playPauseButton.setDisable(false);
            muteButton.setDisable(mute);
            volumeSlider.setDisable(false);
            progressSlider.setDisable(false);

            //prepare progressSlider
            progressSlider.setMax(this.song.getTotalDuration());
            progressSlider.setValue(0);
        }
    }

    private void playSong() {
        if (song != null) {
            ImageView icon = new ImageView("/player/view/pause.png");
            icon.setFitWidth(40);
            icon.setFitHeight(40);
            playPauseButton.setGraphic(icon);

            song.play();
        }
    }

    private void newPlaylist() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("playlist");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlists (*.playlist)", "*.playlist"));
        File selected = fileChooser.showSaveDialog(primaryStage);
        if (selected != null) {
            savePlaylist(selected);
        }
    }

    private void openPlaylist(File file) throws Exception {
        playlist.getSongs().clear();

        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    ProgressIndicator progressIndicator = new ProgressIndicator();
                    progressIndicator.setMaxWidth(100);
                    progressIndicator.setMaxHeight(100);
                    playlistTab.setContent(new BorderPane(progressIndicator));
                });

                try {
                    playlist.open(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (PlaylistLoadingException e) {
                    Platform.runLater(() -> {
                        String message = new String();

                        for (String path : e.getNotLoadedSongs()) {
                            message += (path + "\n");
                        }

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Error while loading songs");
                        alert.setContentText("Files may not exist or another error occurred");

                        Label label = new Label("Songs not loaded:");

                        TextArea textArea = new TextArea(message);
                        textArea.setEditable(false);
                        textArea.setWrapText(true);

                        textArea.setMaxWidth(Double.MAX_VALUE);
                        textArea.setMaxHeight(Double.MAX_VALUE);
                        GridPane.setVgrow(textArea, Priority.ALWAYS);
                        GridPane.setHgrow(textArea, Priority.ALWAYS);

                        GridPane expContent = new GridPane();
                        expContent.setMaxWidth(Double.MAX_VALUE);
                        expContent.add(label, 0, 0);
                        expContent.add(textArea, 0, 1);

                        alert.getDialogPane().setExpandableContent(expContent);

                        alert.showAndWait();
                    });
                }

                Platform.runLater(() -> playlistTab.setContent(playlistTableView));

                return null;
            }
        }).start();
    }

    private void addSongToPlaylist() throws Exception {
        FileChooser fileChooser = new FileChooser();

        List<File> selected = fileChooser.showOpenMultipleDialog(primaryStage);

        for (File file : selected) {
            playlist.add(new Song(file.getPath()));
        }
    }

    @FXML
    private void savePlaylist(File file) {
        try {
            playlist.save(file.getPath());
        } catch (FileNotFoundException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Error");
            a.setContentText("Fail to save playlist");
            a.showAndWait();
        }
    }
}
