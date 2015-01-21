#include "notificationwindow.h"
#include "replywindow.h"
#include"main.h"
#include<QApplication>
#include<QString>

NotificationWindow* not_window;
Annodere::Connection_worker conn_worker;

int main(int argc, char *argv[]) {
    QApplication a(argc, argv);
    not_window= new NotificationWindow();

    return a.exec();
}
