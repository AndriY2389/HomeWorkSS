package MainPackage;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class ConnectionMaker {

    private final Socket socket;
    private final Thread rxThread;  // потік, який слухає вхідне з'єднання і в разі отримання повідомлення генерує подію
    private final ConnectionMakerInterface connectionMakerInterface;  //той хто слухає подіїї
    private final BufferedReader in;
    private final BufferedWriter out;

    public ConnectionMaker(ConnectionMakerInterface connectionMakerInterface, String ipAddress, int port) throws IOException {
        this(connectionMakerInterface, new Socket(ipAddress, port));
    }

    public ConnectionMaker(ConnectionMakerInterface connectionMakerInterface, Socket socket) throws IOException {  //приймає сокет (готове з'єднання) і зробить з ним з'єднання
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
                        connectionMakerInterface.receiveMessage(ConnectionMaker.this, in.readLine());  //отримуємо рядок і відправляємо до connectionMakerInterface
                    }
                } catch (IOException e) {
                    connectionMakerInterface.somethingWithConnection(ConnectionMaker.this, e);
                }finally {
                    connectionMakerInterface.disconnection(ConnectionMaker.this);  //стався дісконект
                }
            }
        };
        rxThread.start();  //запускаємо потік
    }

    public synchronized void sendString(String valve) {  //відправити рядок
        try {
            out.write(valve+"\r\n");  //написати в потік виводу (в буфер)ю "\r\n" щоб перевести на новий рядок на клієнті
            out.flush();  //скинути буфер і надіслати
        } catch (IOException e) {
            connectionMakerInterface.somethingWithConnection(ConnectionMaker.this, e);
            disconnect();  //роз'єднати
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();  //перервати з'єднання
        try {
            socket.close();  //закрити сокет
        } catch (IOException e) {
            connectionMakerInterface.somethingWithConnection(ConnectionMaker.this, e);
        }
    }

    @Override
    public String toString() {
        return "ConnectionMaker: " + socket.getInetAddress() + " " + socket.getPort();
    }
}