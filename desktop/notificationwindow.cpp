#include "notificationwindow.h"
#include "ui_notificationwindow.h"
#include "replywindow.h"
#include<QMessageBox> //jonas: später obsolet
#include<QTime>
#include<QString>

NotificationWindow::NotificationWindow(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::NotificationWindow)
{
    //jonas: muss dann dynamisch gemacht werden: ein Objekt (pro eingehender Nachricht)
    ui->setupUi(this);

    //jonas: setzen der Nachricht (hier noch für den Dummy)
    QTime time = QTime::currentTime();
    QString stime = time.toString();
    this->message = stime + " Nachricht";

    ui->lbl_nachricht->setText(this->message);

    //jonas: setzen des Logos und der Kontaktinformationen
    QGraphicsScene *scene = new QGraphicsScene();
    this->image.load("Logos/Whatsapp.jpg");
    scene->setBackgroundBrush(this->image.scaled(50,50,Qt::KeepAspectRatio,Qt::SmoothTransformation));
    ui->gV_logo->setScene(scene);
}

NotificationWindow::~NotificationWindow()
{
    delete ui;
}

void NotificationWindow::answer()
{
    //jonas: dynamisch Objekt erzeugen
    ReplyWindow *dialog = new ReplyWindow;
    this->hide(); //jonas: NotificationWindow zuerst schließen
    dialog->show();

}

void NotificationWindow::close()
{
  //jonas: bis jetzt wird das Fenster noch über das von Qt eingefügte X  am oberen Bildschirmrand
  //geschlossen
}

void NotificationWindow::on_pB_antworten_clicked()
{
    NotificationWindow::answer();
}

QString NotificationWindow::get_message()
{
    return this->message;
}

void NotificationWindow::set_message(QString m){
    this->message = m;
}

void NotificationWindow::set_message(std::string m){
    set_message(QString(m.c_str()));
}

