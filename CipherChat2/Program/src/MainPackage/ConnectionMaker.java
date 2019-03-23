package MainPackage;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class ConnectionMaker {

    private final Socket socket;
    private final Thread rxThread;
    private final ConnectionMakerInterface connectionMakerInterface;
    private final BufferedReader in;
    private final BufferedWriter out;

    public ConnectionMaker(ConnectionMakerInterface connectionMakerInterface, String ipAddress, int port) throws IOException {
        this(connectionMakerInterface, new Socket(ipAddress, port));
    }

    public ConnectionMaker(ConnectionMakerInterface connectionMakerInterface, Socket socket) throws IOException {
        this.connectionMakerInterface = connectionMakerInterface;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread = new Thread() {  //генеруємо новий потік
            @Override
            public void run() {
                try {
                    connectionMakerInterface.connectionReady(ConnectionMaker.this);
                    while (!rxThread.isInterrupted()) {
                        connectionMakerInterface.receiveMessage(ConnectionMaker.this, in.readLine());
                    }
                } catch (IOException e) {
                    connectionMakerInterface.somethingWithConnection(ConnectionMaker.this, e);
                }finally {
                    connectionMakerInterface.disconnection(ConnectionMaker.this);
                }
            }
        };
        rxThread.start();
    }

    public synchronized void sendString(String valve) {
        try {
            out.write(valve+"\r\n");
            out.flush();
        } catch (IOException e) {
            connectionMakerInterface.somethingWithConnection(ConnectionMaker.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            connectionMakerInterface.somethingWithConnection(ConnectionMaker.this, e);
        }
    }

    @Override
    public String toString() {
        return "ConnectionMaker: " + socket.getInetAddress() + " " + socket.getPort();
    }
}