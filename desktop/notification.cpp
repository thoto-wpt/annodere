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
    //jonas: muss dann dynamisch gemacht werden: ein Objekt (pro eingehender Nachricht)
    ui->setupUi(this);

    //jonas: setzen der Nachricht (hier noch für den Dummy)
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
    //jonas: dynamisch Objekt erzeugen
    Dialog dialog;
    dialog.setModal(true);
    this->close(); //jonas: NotificationWindow zuerst schließen
    dialog.exec();
}

void Notification::close()
{
  //jonas: bis jetzt wird das Fenster noch über das von Qt eingefügte X  am oberen Bildschirmrand
  //geschlossen
}

void Notification::on_pB_antworten_clicked()
{
    answer();
}
