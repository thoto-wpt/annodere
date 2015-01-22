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
    ui->setupUi(this);
    setWindowFlags( Qt::CustomizeWindowHint );

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
    printf("delete NotificationWindow");
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
  //jonas: not_window wird nur versteckt
    printf("delete NotificationWindow");
    this->hide();
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
    QTime time = QTime::currentTime();
    QString stime = time.toString();
    this->message = stime + " " +m;
    ui->lbl_nachricht->setText(this->message);
}

void NotificationWindow::set_message(std::string m){
    set_message(QString(m.c_str()));
}


void NotificationWindow::on_pB_close_clicked()
{
    NotificationWindow::close();
}
