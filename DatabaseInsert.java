import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class DatabaseInsert extends Application {
  private static final String DB_URL = "jdbc:mysql://localhost:3306/mydatabase";
  private static final String DB_USERNAME = "your_username";
  private static final String DB_PASSWORD = "your_password";

  private TextArea logTextArea;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Database Insert Performance");

    logTextArea = new TextArea();
    logTextArea.setEditable(false);
    logTextArea.setWrapText(true);

    Button insertWithoutBatchButton = new Button("Insert without Batch");
    insertWithoutBatchButton.setOnAction(e -> insertWithoutBatch());

    Button insertWithBatchButton = new Button("Insert with Batch");
    insertWithBatchButton.setOnAction(e -> insertWithBatch());

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(10));
    layout.getChildren().addAll(
      new Label("Insert 1000 records into database and compare performance:"),
      insertWithoutBatchButton,
      insertWithBatchButton,
      logTextArea
    );

    Scene scene = new Scene(layout, 400, 300);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
  private void insertWithoutBatch() {
    log("Inserting without batch...");
    long startTime = System.currentTimeMillis();

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD));
    connection.setAutoCommit(true);

    for (int i = 0; i < 1000; i++) {
      try (PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO Temp(num1, num2, num3) VALUES (?, ?, ?)")) {
        statement.setDouble(1, Math.random());
        statement.setDouble(2, Math.random());
        statement.setDouble(3, Math.random());
        statement.executeUpdate();
      }
      
    }
  } catch (SQLException e) {
    log("Error inserting without batch: " + e.getMessage());
  }

long endTime = System.currentTimeMillis();
log("Non-batch update completed in " + (endTime - startTime) + " milliseconds.");
}

private void insertWithBatch() {
  log("Inserting with batch...);
  long startTime = System.currentTimeMillis();


  try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
    connection.setAutoCommit(false);

    try (PreparedStatement statement = connection.prepareStatement(
      "INSERT INTO Temp(num1, num2, num3) VALUES (?, ?, ?)")) {

      for (int i = 0; i < 1000; i++) {
        statement.setDouble(1, Math.random());
        statement.setDouble(2, Math.random());
        statement.setDouble(3, Math.random());
        statement.addBatch();
      }

      statement.executeBatch();
      connection.commit();
    } catch (SQLException e) {
      connection.rollback();
      log("Error inserting with batch: " + e.getMessage());
    }
  } catch (SQLException e) {
    log("Error connecting to database: " + e.getMessage());
  }
    
  long endTime = System.currentTimeMillis();
  log("Batch update completed in " + (endTime - startTime) + " milliseconds.");
}

private void log(String Message) {
  logTextArea.appendText(Message + "\n");
}
}