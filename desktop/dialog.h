#ifndef DIALOG_H
#define DIALOG_H

#include <QDialog>
#include<QString>
#include<QPixmap>

namespace Ui {
class Dialog;
}

class Dialog : public QDialog
{
    Q_OBJECT

private:
    QString contact;
    QPixmap image;
    QString app_name;
    QString messages[];

public:
    explicit Dialog(QWidget *parent = 0);
    ~Dialog();
    void send_reply();
    void close();

private slots:
    void on_pB_abbrechen_clicked();

    void on_pB_antworten_clicked();

private:
    Ui::Dialog *ui;
};

#endif // DIALOG_H
