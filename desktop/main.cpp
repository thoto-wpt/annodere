#include "notificationwindow.h"
#include "replywindow.h"
#include"main.h"
#include<QApplication>
#include<QString>

NotificationWindow* not_window;
Annodere::Connection_worker conn_worker;

int main(int argc, char *argv[]) {
    //jonas: hier müsste dann handle_notifications() gestartet werden?
    //jonas: öffnen des Notification Fensters noch vom Dummy
    QApplication a(argc, argv);
//    NotificationWindow n;
    not_window= new NotificationWindow();
//    n.show();


    return a.exec();
}
