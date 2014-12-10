#ifndef NOTIFICATION_H
#define NOTIFICATION_H

#include <QDialog>
#include<QString>
#include<QPixmap>

namespace Ui {
class Notification;
}

class Notification : public QDialog
{
    Q_OBJECT
private:
    QString contact;
    QPixmap image;
    QString app_name;

public:
    explicit Notification(QWidget *parent = 0);
    ~Notification();
    void answer();

private slots:
    void on_pB_antworten_clicked();

private:
    Ui::Notification *ui;
};

#endif // NOTIFICATION_H
