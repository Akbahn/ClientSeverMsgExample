package org.example.clientsevermsgexample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

public class MainController implements Initializable {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream serverOut;
    private DataInputStream serverIn;
    private DataOutputStream clientOut;
    private DataInputStream clientIn;

    @FXML
    private ComboBox dropdownPort;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dropdownPort.getItems().addAll("7",     // ping
                "13",     // daytime
                "21",     // ftp
                "23",     // telnet
                "71",     // finger
                "80",     // http
                "119",    // nntp (news)
                "161",    // snmp
                "6666"    // local host);
        );
    }

    @FXML
    private Button clearBtn;

    @FXML
    private TextArea resultArea;

    @FXML
    private Label server_lbl;

    @FXML
    private Button testBtn;

    @FXML
    private Label test_lbl;

    @FXML
    private TextField urlName;

    Socket socket1;

    Label lb122, lb12;
    TextField msgText;

    @FXML
    void checkConnection(ActionEvent event) {

        String host = urlName.getText();
        int port = Integer.parseInt(dropdownPort.getValue().toString());
        try {
            Socket sock = new Socket(host, port);
            resultArea.appendText(host + " listening on port " + port + "\n");
            sock.close();
        } catch (UnknownHostException e) {
            resultArea.setText(String.valueOf(e) + "\n");
            return;
        } catch (Exception e) {
            resultArea.appendText(host + " not listening on port "
                    + port + "\n");
        }
    }

    @FXML
    void clearBtn(ActionEvent event) {
        resultArea.setText("");
        urlName.setText("");
    }

    @FXML
    void startServer(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();
        Label lb11 = new Label("Server");
        lb11.setLayoutX(100);
        lb11.setLayoutY(100);

        lb12 = new Label("info");
        lb12.setLayoutX(100);
        lb12.setLayoutY(200);

        TextField serverMsgField = new TextField();
        serverMsgField.setLayoutX(100);
        serverMsgField.setLayoutY(250);

        Button sendBtn = new Button("Send");
        sendBtn.setLayoutX(250);
        sendBtn.setLayoutY(250);
        sendBtn.setOnAction(e -> {
            try {
                String msg = serverMsgField.getText();
                if (serverOut != null) {
                    serverOut.writeUTF(msg);
                    updateServer("Sent to client: " + msg);
                }
            } catch (IOException ex) {
                updateServer("Error sending: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(lb11, lb12, serverMsgField, sendBtn);
        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        stage.setTitle("Server");
        stage.show();

        new Thread(this::runServer).start();
    }
    String message;

    private void runServer() {
        try {
            serverSocket = new ServerSocket(6666);
            updateServer("Server waiting for client...");

            clientSocket = serverSocket.accept();
            updateServer("Client connected!");

            serverIn = new DataInputStream(clientSocket.getInputStream());
            serverOut = new DataOutputStream(clientSocket.getOutputStream());

            while (true) {
                String msg = serverIn.readUTF();
                updateServer("Client: " + msg);
            }

        } catch (IOException e) {
            updateServer("Server error: " + e.getMessage());
        }
    }

    private void updateServer(String message) {
        // Run on the UI thread
        javafx.application.Platform.runLater(() -> lb12.setText(message + "\n"));
    }


    @FXML
    void startClient(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();

        Label lb11 = new Label("Client");
        lb11.setLayoutX(100);
        lb11.setLayoutY(100);

        msgText = new TextField();
        msgText.setLayoutX(100);
        msgText.setLayoutY(150);

        Button sendBtn = new Button("Send");
        sendBtn.setLayoutX(250);
        sendBtn.setLayoutY(150);

        lb122 = new Label("info");
        lb122.setLayoutX(100);
        lb122.setLayoutY(200);

        sendBtn.setOnAction(e -> {
            try {
                if (clientOut != null) {
                    clientOut.writeUTF(msgText.getText());
                    updateTextClient("You: " + msgText.getText());
                }
            } catch (IOException ex) {
                updateTextClient("Send error: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(lb11, msgText, sendBtn, lb122);
        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        stage.setTitle("Client");
        stage.show();

        new Thread(this::connectToServer).start();
    }



    private void connectToServer() {
        try {
            socket1 = new Socket("localhost", 6666);
            clientOut = new DataOutputStream(socket1.getOutputStream());
            clientIn = new DataInputStream(socket1.getInputStream());

            updateTextClient("Connected to server.");

            while (true) {
                String msg = clientIn.readUTF();
                updateTextClient("Server: " + msg);
            }

        } catch (IOException e) {
            updateTextClient("Connection error: " + e.getMessage());
        }
    }

    private void updateTextClient(String message) {
        // Run on the UI thread
        javafx.application.Platform.runLater(() -> lb122.setText(message + "\n"));
    }

}
