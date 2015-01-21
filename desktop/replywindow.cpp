#include "replywindow.h"
#include "ui_replywindow.h"
#include "notificationwindow.h"
#include "main.h"
#include "connection_worker.h"
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

    this->messages << not_window->get_message();

    //jonas: graphische Ausgabe
    ui->tE_nachrichten_verlauf->setText(this->messages[0]);
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
    QString tmp= "";
    if(ui->tE_antwort->toPlainText() != "") //jonas: leere Nachricht abfangen
    {
        QTime time = QTime::currentTime();
        QString stime = time.toString();
        QString antwort = stime + " " + ui->tE_antwort->toPlainText(); //antwort muss dann gesendet werden

        this->messages << antwort;
        for(int i = 0; i < this->messages.size(); i++)
        {
            tmp = tmp + this->messages[i] +"\n";
        }
        ui->tE_nachrichten_verlauf->setText(tmp);
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
    this->hide(); //jonas: hier fehlt noch das beenden
}

void ReplyWindow::on_pB_abbrechen_clicked()
{
    ReplyWindow::close();
}

void ReplyWindow::on_pB_antworten_clicked()
{
   ReplyWindow::send_reply();
}
