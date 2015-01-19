#ifndef NOTIFICATION_H
#define NOTIFICATION_H

#include <QWidget>
#include<QString>
#include<QPixmap>
#include<string>

namespace Ui {
class NotificationWindow;
}

class NotificationWindow : public QWidget
{
    Q_OBJECT
private:
    QString contact;
    QPixmap image;
    QString app_name;
    QString message;

public:
    explicit NotificationWindow(QWidget *parent = 0);
    explicit NotificationWindow(std::string m);
    ~NotificationWindow();
    void answer();
    void close();
    QString get_message();
    void set_message(QString m);
    void set_message(std::string m);

    friend class ReplyWindow;

private slots:
    void on_pB_antworten_clicked();

private:
    Ui::NotificationWindow *ui;


};

#endif // NOTIFICATIONWINDOW_H
