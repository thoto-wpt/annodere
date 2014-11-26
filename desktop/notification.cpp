#include "notification.h"
#include "ui_notification.h"
#include "globals.h"
#include "dialog.h"
#include<QTime>
#include<QString>

Notification::Notification(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::Notification)
{
    ui->setupUi(this);

    //jonas: setzen der Nachricht
    QTime time = QTime::currentTime();
    QString stime = time.toString();
    nachricht_string = stime + " Nachricht";

    ui->lbl_nachricht->setText(nachricht_string);

    //jonas: setzen des Logos und der Kontaktinformationen
    QGraphicsScene *scene = new QGraphicsScene();
    QPixmap m("Logos/Whatsapp.jpg");
    scene->setBackgroundBrush(m.scaled(50,50,Qt::KeepAspectRatio,Qt::SmoothTransformation));
    ui->gV_logo->setScene(scene);
}

Notification::~Notification()
{
    delete ui;
}

void Notification::answer()
{

}


void Notification::on_pB_antworten_clicked()
{
    Dialog dialog;
    dialog.setModal(true);
    this->close(); //jonas: mainwindow zuerst schließen
    dialog.exec();
}
