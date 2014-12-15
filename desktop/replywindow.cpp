#include "replywindow.h"
#include "ui_replywindow.h"
#include "globals.h"
#include "notificationwindow.h"
#include<QMessageBox>
#include<QTime>
#include<QGraphicsScene>
#include<QPixmap>

ReplyWindow::ReplyWindow(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::ReplyWindow)
{
    //muss dann dynamisch gemacht werden: ein neues Objekt pro Aufruf
    ui->setupUi(this);

    //this->contact=;
    //this->app_name=;

    QGraphicsScene *scene = new QGraphicsScene();
    this->image.load("Logos/Whatsapp.jpg");
    scene->setBackgroundBrush(this->image.scaled(50,50,Qt::KeepAspectRatio,Qt::SmoothTransformation));

    //this->messages[0] = NotificationWindow::get_message(); jonas: stehe gerade auf dem Schlauch

    //jonas: graphische Ausgabe
    ui->tE_nachrichten_verlauf->setText(nachricht_string);
    ui->pB_antworten->setDefault(true); //AntwortButton lässt sich mit Enter betätigen
    ui->tE_antwort->setFocus();


    ui->gV_logo->setScene(scene);
}

ReplyWindow::~ReplyWindow()
{
    delete ui;
}


void ReplyWindow::send_reply()
{
    if(ui->tE_antwort->toPlainText() != "") //jonas: leere Nachricht abfangen
    {
        QTime time = QTime::currentTime();
        QString stime = time.toString();
        QString antwort = stime + " " + ui->tE_antwort->toPlainText(); //antwort muss dann gesendet werden

        nachricht_string = nachricht_string + "\n" +antwort;
        ui->tE_nachrichten_verlauf->setText(nachricht_string);
        ui->tE_antwort->setText("");
    }
    else
    {
        QMessageBox msgBox;
        msgBox.setText("Eine leere Nachricht kann nicht gesendet werden.");
        msgBox.setStandardButtons(QMessageBox::Ok);
        msgBox.setDefaultButton(QMessageBox::Ok);
        msgBox.setWindowTitle("Fehler");
        msgBox.exec();
    }
}

void ReplyWindow::close()
{
    this->close(); //jonas: macht segmentation fault
}

void ReplyWindow::on_pB_abbrechen_clicked()
{
    ReplyWindow::close();
}

void ReplyWindow::on_pB_antworten_clicked()
{
   ReplyWindow::send_reply();
}
