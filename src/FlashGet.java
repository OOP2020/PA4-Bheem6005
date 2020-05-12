package src;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * The JavaFX Multi-threaded file downloader application that uses
 * multiple threads to download parts of a file in parallel.
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
     * Progress bar showing how far the work in each thread done.
     */
    private ProgressBar threadBar1;
    private ProgressBar threadBar2;
    private ProgressBar threadBar3;
    private ProgressBar threadBar4;
    /**
     * List that contains all running task
     */
    private List<Task<Long>> taskList = new ArrayList<>();

    /**
     * Setting up the stage.
     */
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();
        Pane inputPane = initInputPane();
        inputPane.styleProperty().set("-fx-background-color: #3B3B40");
        AnchorPane infoPane = initInfoPane();
        infoPane.styleProperty().set("-fx-background-color: #BCBCBC");
        Pane threadPane = initThreadPane();
        threadPane.styleProperty().set("-fx-background-color: #BCBCBC");

        borderPane.setTop(inputPane);
        borderPane.setCenter(infoPane);
        borderPane.setBottom(threadPane);

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setTitle("FlashGet! - Multithreaded Downloader");
        stage.getIcons().add(new Image("src/images/icon.png"));
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Initialize components for the inputPane to display.
     */
    private Pane initInputPane() {
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
        downloadButton.setOnAction(this::downloadHandle);

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
     * Initialize components for the infoPane to display.
     */
    private AnchorPane initInfoPane() {
        AnchorPane pane = new AnchorPane();
        pane.setPrefSize(700.0, 100.0);
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
        cancelButton.setOnAction(this::cancelHandle);

        pane.getChildren().addAll(Label1, fileNameLabel, progressBar, progressLabel, cancelButton);

        return pane;
    }

    /**
     * Initialize components for the threadPane to display.
     */
    private Pane initThreadPane() {
        FlowPane pane = new FlowPane();
        pane.setPrefSize(700.0, 50.0);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(10.0));
        pane.setHgap(20.0);

        Label label1 = new Label("Threads :");
        label1.setFont(Font.font(14.0));
        threadBar1 = new ProgressBar();
        threadBar2 = new ProgressBar();
        threadBar3 = new ProgressBar();
        threadBar4 = new ProgressBar();

        pane.getChildren().addAll(label1, threadBar1, threadBar2, threadBar3, threadBar4);
        return pane;
    }

    /**
     * Run application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Handle method for download button
     */
    private void downloadHandle(ActionEvent e) {
        try {
            taskList.clear();
            URL url = new URL(urlField.getText());
            URLConnection conn = url.openConnection();
            long fileSize = conn.getContentLengthLong();
            long quarterFileSize = fileSize / 4;
            if (fileSize > 0) {
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

                    var ref = new Object() {
                        long bytesDownloaded = 0L;
                        long task1Downloaded = 0L;
                        long task2Downloaded = 0L;
                        long task3Downloaded = 0L;
                        long task4Downloaded = 0L;
                    };

                    ChangeListener<Long> listener1 = (observableValue, oldValue, newValue) ->
                    {
                        ref.task1Downloaded = (newValue);
                        ref.bytesDownloaded = ref.task1Downloaded + ref.task2Downloaded + ref.task3Downloaded + ref.task4Downloaded;
                        progressLabel.setText(String.format("%d/%d", ref.bytesDownloaded, fileSize));
                    };
                    ChangeListener<Long> listener2 = (observableValue, oldValue, newValue) ->
                    {
                        ref.task2Downloaded = (newValue);
                        ref.bytesDownloaded = ref.task1Downloaded + ref.task2Downloaded + ref.task3Downloaded + ref.task4Downloaded;
                        progressLabel.setText(String.format("%d/%d", ref.bytesDownloaded, fileSize));
                    };
                    ChangeListener<Long> listener3 = (observableValue, oldValue, newValue) ->
                    {
                        ref.task3Downloaded = (newValue);
                        ref.bytesDownloaded = ref.task1Downloaded + ref.task2Downloaded + ref.task3Downloaded + ref.task4Downloaded;
                        progressLabel.setText(String.format("%d/%d", ref.bytesDownloaded, fileSize));
                    };
                    ChangeListener<Long> listener4 = (observableValue, oldValue, newValue) ->
                    {
                        ref.task4Downloaded = (newValue);
                        ref.bytesDownloaded = ref.task1Downloaded + ref.task2Downloaded + ref.task3Downloaded + ref.task4Downloaded;
                        progressLabel.setText(String.format("%d/%d", ref.bytesDownloaded, fileSize));
                    };

                    ExecutorService service = Executors.newFixedThreadPool(4, new ThreadFactory() {
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r) {
                                @Override
                                public void interrupt() {
                                    super.interrupt();
                                }
                            };
                            t.setDaemon(true);
                            return t;
                        }
                    });

                    Task<Long> task1 = new DownloadTask(urlField.getText(), file, 0, quarterFileSize);
                    Task<Long> task2 = new DownloadTask(urlField.getText(), file, quarterFileSize, quarterFileSize);
                    Task<Long> task3 = new DownloadTask(urlField.getText(), file, quarterFileSize + quarterFileSize, quarterFileSize);
                    Task<Long> task4 = new DownloadTask(urlField.getText(), file, quarterFileSize + quarterFileSize + quarterFileSize, fileSize - (quarterFileSize * 3));

                    task1.valueProperty().addListener(listener1);
                    task2.valueProperty().addListener(listener2);
                    task3.valueProperty().addListener(listener3);
                    task4.valueProperty().addListener(listener4);

                    taskList.add(task1);
                    taskList.add(task2);
                    taskList.add(task3);
                    taskList.add(task4);

                    threadBar1.progressProperty().bind(task1.progressProperty());
                    threadBar2.progressProperty().bind(task2.progressProperty());
                    threadBar3.progressProperty().bind(task3.progressProperty());
                    threadBar4.progressProperty().bind(task4.progressProperty());

                    progressBar.progressProperty().bind(task1.progressProperty().multiply(0.25)
                            .add(task2.progressProperty().multiply(0.25))
                            .add(task3.progressProperty().multiply(0.25))
                            .add(task4.progressProperty().multiply(0.25)));

                    for (Task<Long> t : taskList) {
                        service.execute(t);
                    }

                    service.shutdown();
                }
            }
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error: Invalid URL");
            alert.show();
        } catch (RejectedExecutionException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error: Previous download is unfinished");
            alert.show();
        }
    }

    /**
     * Handle method for cancel button
     */
    private void cancelHandle(ActionEvent e) {
        for (Task<Long> longTask : taskList) {
            longTask.cancel();
            }
        taskList.clear();
        }
}
