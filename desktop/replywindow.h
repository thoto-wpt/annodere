#ifndef REPLYWINDOW_H
#define REPLYWINDOW_H

#include <QWidget>
#include<QString>
#include<QPixmap>
namespace Ui {
class ReplyWindow;
}

class ReplyWindow : public QWidget
{
    Q_OBJECT
private:
    QString contact;
    QPixmap image;
    QString app_name;
    QString messages[];

public:
    explicit ReplyWindow(QWidget *parent = 0);
    ~ReplyWindow();
    void send_reply();
    void close();

private:
    Ui::ReplyWindow *ui;

private slots:
    void on_pB_abbrechen_clicked();

    void on_pB_antworten_clicked();
};

#endif // REPLYWINDOW_H


