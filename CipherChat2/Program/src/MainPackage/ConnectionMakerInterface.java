package MainPackage;

public interface ConnectionMakerInterface {

    void connectionReady(ConnectionMaker connectionMaker);  //з'єднання готове ми запустили
    void receiveMessage(ConnectionMaker connectionMaker, String value);  //прийняти рядок String value
    void disconnection(ConnectionMaker connectionMaker);  //роз'єднання
    void somethingWithConnection(ConnectionMaker connectionMaker, Exception e);  //помилка, щось не так
}