package MainPackage;

public interface ConnectionMakerInterface {

    void connectionReady(ConnectionMaker connectionMaker);
    void receiveMessage(ConnectionMaker connectionMaker, String value);
    void disconnection(ConnectionMaker connectionMaker);
    void somethingWithConnection(ConnectionMaker connectionMaker, Exception e);
}