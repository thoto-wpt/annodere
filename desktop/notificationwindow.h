#ifndef NOTIFICATION_H
#define NOTIFICATION_H

#include <QWidget>
#include<QString>
#include<QPixmap>

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
    void append_message(QString m);

    friend class ReplyWindow;

private slots:
    void on_pB_antworten_clicked();

    void on_pB_close_clicked();
    void on_notify(QString msg);

private:
    Ui::NotificationWindow *ui;


};

#endif // NOTIFICATIONWINDOW_H
