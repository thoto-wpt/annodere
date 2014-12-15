#include "notificationwindow.h"
#include "replywindow.h"
#include<QApplication>
#include<QString>

int main(int argc, char *argv[])
{
    //jonas: hier müsste dann handle_notifications() gestartet werden?
    //jonas: öffnen des Notification Fensters noch vom Dummy
    QApplication a(argc, argv);
    NotificationWindow w;
    w.show();
    ReplyWindow r;
    r.show();

    return a.exec();
}
