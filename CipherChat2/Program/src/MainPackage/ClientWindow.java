package MainPackage;

import org.apache.commons.codec.DecoderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ClientWindow extends JFrame implements ActionListener, ConnectionMakerInterface {

    private static final int WIDTH = 555;
    private static final int HEIGHT = 400;
    private static boolean connectionStatus = false;
    private static boolean serverRunning = false;
    private static int portFromUser = 0;
    private final ArrayList<ConnectionMaker> connections = new ArrayList<>();

    public static void main(String[] args) {

        Thread startClient = new Thread() {
            @Override
            public void run() {
                new ClientWindow();
            }
        };
        startClient.start();
    }

    private final JPanel panel = new JPanel();
    private final JTextArea listMessages = new JTextArea();
    private final JLabel labelName = new JLabel("Name: ");
    private final JLabel labelPassword = new JLabel("Password: ");
    private final JLabel labelIpAddress = new JLabel("IP Address: ");
    private final JLabel labelIpPort = new JLabel("Port: ");
    private final JTextField userName = new JTextField("AndriY", 7);
    private final JTextField inputTextField = new JTextField();
    private final JTextField ipAddress = new JTextField("127.0.0.1", 10);
    private final JTextField ipPort = new JTextField("8189", 7);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JButton connectButton = new JButton("Connect to server");
    private final JButton startServerButton = new JButton("Start server");

    private ConnectionMaker connection;

    public ClientWindow() {
        super("CipherChat");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        listMessages.setEditable(false);
        inputTextField.setEditable(connectionStatus);
        add(panel, BorderLayout.NORTH);
        panel.setPreferredSize(new Dimension(WIDTH, 70));
        listMessages.setLineWrap(true);
        add(listMessages, BorderLayout.CENTER);
        add(new JScrollPane(listMessages));

        inputTextField.addActionListener(this);

        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent b) {
                if ((b.getSource() == connectButton && !connectionStatus) && (ipAddress.getText().length() > 0 && ipPort.getText().length() > 0)) {
                    try {
                        portFromUser = Integer.parseInt(ipPort.getText());
                    } catch (NumberFormatException e) {
                        printMsg("portFromUser: " + e);
                        return;
                    }
                    connectionStatus = true;
                    try {
                        connection = new ConnectionMaker(ClientWindow.this, ipAddress.getText(), portFromUser);
                    } catch (IOException e) {
                        passwordField.setText(null);
                        connectionStatus = false;
                        printMsg("Connection exception: " + e);
                    }
                    inputTextField.setEditable(connectionStatus);
                }
            }
        });

        startServerButton.addActionListener(ss -> {
            if (((ss.getSource() == startServerButton) && !connectionStatus) && (!serverRunning && ipPort.getText().length() > 0)) {
                try {
                    portFromUser = Integer.parseInt(ipPort.getText());
                } catch (NumberFormatException e) {
                    printMsg("portFromUser: " + e);
                    return;
                }
                serverRunning = true;
                Thread chatServer = new Thread() {
                    class ChatServer implements ConnectionMakerInterface {

                        public ChatServer(int port) {
                            printMsgFromServer("Server running...");
                            try (ServerSocket serverSocket = new ServerSocket(port)) {
                                while (true) {
                                    try {
                                        new ConnectionMaker(this, serverSocket.accept());
                                    } catch (IOException e) {
                                        serverRunning = false;
                                        printMsgFromServer("ConnectionMaker exception: " + e);
                                    }
                                }
                            } catch (IOException e) {
                                serverRunning = false;
                                printMsgFromServer("Server exception: " + e);
                            }
                        }

                        @Override
                        public synchronized void connectionReady(ConnectionMaker connectionMaker) {
                            connections.add(connectionMaker);
                            sendToAllConnection("Client connected: " + connectionMaker);
                        }

                        @Override
                        public synchronized void receiveMessage(ConnectionMaker connectionMaker, String value) {
                            sendToAllConnection(value);
                        }

                        @Override
                        public synchronized void disconnection(ConnectionMaker connectionMaker) {
                            connections.remove(connectionMaker);
                            sendToAllConnection("Client disconnected: " + connectionMaker);
                        }

                        @Override
                        public synchronized void somethingWithConnection(ConnectionMaker connectionMaker, Exception e) {
                            serverRunning = false;
                            printMsgFromServer("ConnectionMaker exception: " + e);
                        }

                        private synchronized void printMsgFromServer(String msg) {
                            listMessages.append(msg + "\n");
                            listMessages.setCaretPosition(listMessages.getDocument().getLength());
                        }

                        private void sendToAllConnection(String valve) {
                            for (int i = 0; i < connections.size(); i++) {
                                connections.get(i).sendString(valve);
                            }
                        }
                    }

                    @Override
                    public void run() throws RuntimeException {
                        new ChatServer(portFromUser);
                    }
                };
                chatServer.start();
            }
        });

        add(inputTextField, BorderLayout.SOUTH);
        panel.add(labelName, BorderLayout.NORTH);
        panel.add(userName, BorderLayout.NORTH);
        panel.add(labelPassword, BorderLayout.NORTH);
        panel.add(passwordField, BorderLayout.NORTH);
        panel.add(labelIpAddress, BorderLayout.SOUTH);
        panel.add(ipAddress, BorderLayout.SOUTH);
        panel.add(labelIpPort, BorderLayout.SOUTH);
        panel.add(ipPort, BorderLayout.SOUTH);
        panel.add(connectButton);
        panel.add(startServerButton);

        inputTextField.setToolTipText("Input text here after connection");
        ipAddress.setToolTipText("Something like 192.168.1.23");
        ipPort.setToolTipText("Something like 8189");
        passwordField.setToolTipText("Key, which you use to encrypt message. Must be 16, 24, 32 symbols");
        connectButton.setToolTipText("Connect to server, which must be run");
        startServerButton.setToolTipText("Start your own server in case you haven't it");

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        char[] psw = passwordField.getPassword();
        if (Cryptogram.isKeyLengthValid(String.valueOf(psw))) {
            String msg = inputTextField.getText();
            if (msg.equals("")) return;
            inputTextField.setText(null);
            try {
                connection.sendString(Cryptogram.encrypt(String.valueOf(psw), "SAFE: " + userName.getText() + ": " + msg));
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException En) {
                passwordField.setText(null);
                printMsg("Encryption exception: " + En);
            }
        } else {
            String msg = inputTextField.getText();
            if (msg.equals("")) return;
            inputTextField.setText(null);
            connection.sendString(userName.getText() + ": " + msg);
        }
    }

    @Override
    public void connectionReady(ConnectionMaker connectionMaker) {
        printMsg("Connection ready...");
    }

    @Override
    public void receiveMessage(ConnectionMaker connectionMaker, String value) {
        printMsg(value);
    }

    @Override
    public void disconnection(ConnectionMaker connectionMaker) {
        printMsg("Connection close");
    }

    @Override
    public void somethingWithConnection(ConnectionMaker connectionMaker, Exception e) {
        printMsg("Connection exception: " + e);
    }

    private synchronized void printMsg(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                char[] psw = passwordField.getPassword();
                if (Cryptogram.isKeyLengthValid(String.valueOf(psw))) {
                    try {
                        listMessages.append(Cryptogram.decrypt(String.valueOf(psw), msg) + "\n");
                    } catch (NoSuchPaddingException | NoSuchAlgorithmException | DecoderException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException De) {
                        passwordField.setText(null);
                        printMsg("Encryption exception: " + De);
                    }
                    listMessages.setCaretPosition(listMessages.getDocument().getLength());
                } else {
                    listMessages.append(msg + "\n");
                    listMessages.setCaretPosition(listMessages.getDocument().getLength());
                }
            }
        });
    }
}