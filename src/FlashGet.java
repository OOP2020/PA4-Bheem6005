package src;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

import java.net.URL;
import java.net.URLConnection;


/**
 * The JavaFX Multi-threaded file downloader application that uses
 * multiple threads to download parts of a file in parallel.
 * <p>
 * // For now it's still just a single thread downloader and I still working on how to use multiple thread.
 *
 * @author Bheem Suttipong
 */
public class FlashGet extends Application {
    /**
     * Text field to fill a URL download link.
     */
    private TextField urlField;
    /**
     * Progress bar showing how far the work done.
     */
    private ProgressBar progressBar;
    /**
     * Text label show the downloading byte and overall bytes.
     */
    private Label progressLabel;
    private Label fileNameLabel;
    /**
     * String to remember last directory used by FileChooser.
     */
    private static String lastVisitedDirectory = System.getProperty("user.home");
    /**
     * The download thread.
     */
    private Thread thread = new Thread();


    /**
     * Setting up the stage.
     */
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();
        Pane loaderPane = initLoader();
        loaderPane.styleProperty().set("-fx-background-color: #3B3B40");
        Separator separator = new Separator();
        separator.styleProperty().set("-fx-background-color: #BCBCBC");
        AnchorPane loadingPane = initLoading();
        loadingPane.styleProperty().set("-fx-background-color: #BCBCBC");

        borderPane.setTop(loaderPane);
        borderPane.setCenter(separator);
        borderPane.setBottom(loadingPane);

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setTitle("FlashGet! - Multithreaded Downloader");
        stage.getIcons().add(new Image("src/images/icon.png"));
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Initialize components for the loaderPane to display.
     */
    private Pane initLoader() {
        FlowPane pane = new FlowPane();
        pane.setPrefSize(700.0, 50.0);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(10.0));
        pane.setHgap(20.0);

        Label label1 = new Label("URL to Download :");
        label1.setTextFill(Color.web("#FFFFFF"));

        urlField = new TextField();
        urlField.setPrefWidth(350);

        Button downloadButton = new Button("Download");
        downloadButton.styleProperty().set("-fx-background-color: #EC5700");
        downloadButton.setTextFill(Color.web("#FFFFFF"));
        downloadButton.setOnAction(e -> {
            try {
                URL url = new URL(urlField.getText());
                URLConnection conn = url.openConnection();

                if (conn.getContentLengthLong() > 0 && !thread.isAlive()) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save File");
                    fileChooser.setInitialDirectory(new File(lastVisitedDirectory));
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("All Files", "*.*"),
                            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                            new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                            new FileChooser.ExtensionFilter("Executable Files", "*.exe"),
                            new FileChooser.ExtensionFilter("ZIP or RAR files", "*.zip", "*.rar"));


                    File file = fileChooser.showSaveDialog(new Stage());

                    if (file != null) {
                        lastVisitedDirectory = file.getParent();
                        fileNameLabel.setText(file.getName());
                    }

                    Task<Void> task = new DownloadTask(urlField.getText(), file);
                    progressBar.progressProperty().bind(task.progressProperty());

                    thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();
                }
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error: Invalid URL");
                alert.show();
            }


        });

        Button clearButton = new Button("Clear");
        clearButton.styleProperty().set("-fx-background-color: #EC5700");
        clearButton.setTextFill(Color.web("#FFFFFF"));
        clearButton.setOnAction(e -> {
            urlField.clear();
        });

        pane.getChildren().addAll(label1, urlField, downloadButton, clearButton);
        return pane;
    }

    /**
     * Initialize components for the loadingPane to display.
     */
    private AnchorPane initLoading() {
        AnchorPane pane = new AnchorPane();
        pane.setPrefSize(700.0, 200.0);
        pane.setPadding(new Insets(10.0));

        Label Label1 = new Label("File name :");
        Label1.setLayoutY(10.0);
        Label1.setLayoutX(20.0);
        Label1.setPrefHeight(25.0);
        Label1.setFont(Font.font(18.0));
        Label1.setStyle("-fx-font-weight: bold");
        Label1.setTextFill(Color.web("#EC5700"));

        fileNameLabel = new Label("");
        fileNameLabel.setLayoutY(10.0);
        fileNameLabel.setLayoutX(120.0);
        fileNameLabel.setPrefHeight(25.0);
        fileNameLabel.setFont(Font.font(18.0));

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(540.0);
        progressBar.setLayoutY(50.0);
        progressBar.setLayoutX(30.0);
        progressBar.setPrefHeight(25.0);

        progressLabel = new Label("");
        progressLabel.setLayoutY(50.0);
        progressLabel.setLayoutX(270.0);
        progressLabel.setPrefHeight(25.0);

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80.0);
        cancelButton.styleProperty().set("-fx-background-color: #FFFFFF");
        cancelButton.setLayoutY(50.0);
        cancelButton.setLayoutX(600.0);
        cancelButton.setOnAction(e -> {
            if (thread.isAlive()) {
                thread.stop();
                progressLabel.setText("");
                fileNameLabel.setText("");
            }
        });

        pane.getChildren().addAll(Label1, fileNameLabel, progressBar, progressLabel, cancelButton);

        return pane;
    }

    /**
     * Run application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Task class to download files from URL
     */
    private class DownloadTask extends Task<Void> {
        private String urlName;
        final int BUFFER_SIZE = 16 * 1024;
        private File file;

        public DownloadTask(String urlName, File file) {
            this.urlName = urlName;
            this.file = file;
        }

        @Override
        protected Void call() throws Exception {
            String format = urlName.substring(urlName.lastIndexOf("."));
            URL url = new URL(urlName);
            URLConnection conn = url.openConnection();
            long fileLength = conn.getContentLengthLong();

            byte[] buffer = new byte[BUFFER_SIZE];
            try (InputStream in = conn.getInputStream();
                 OutputStream out = getOutputStream(file)) {
                long bytesRead = 0L;
                do {
                    int n = in.read(buffer);
                    if (n < 0) break;
                    out.write(buffer, 0, n);
                    bytesRead += n;

                    long finalBytesRead = bytesRead;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressLabel.setText(String.format("%d/%d", finalBytesRead, fileLength));
                        }
                    });
                    updateProgress(bytesRead, fileLength);
                } while (true);
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    public FileOutputStream getOutputStream(File file) {
        FileOutputStream fileOutputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
        } catch (Exception ignored) {
        }
        return fileOutputStream;
    }

}
