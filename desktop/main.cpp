#include "notification.h"
#include<QApplication>
#include<QString>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    Notification w;
    w.show();

    return a.exec();
}
