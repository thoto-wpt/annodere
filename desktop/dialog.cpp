#include "dialog.h"
#include "ui_dialog.h"
#include "globals.h"
#include<QMessageBox>
#include<QTime>
#include<QGraphicsScene>
#include<QPixmap>

Dialog::Dialog(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::Dialog)
{
    ui->setupUi(this);

    ui->tE_nachrichten_verlauf->setText(nachricht_string);
    ui->pB_antworten->setDefault(true); //AntwortButton lässt sich mit Enter betätigen
    ui->tE_antwort->setFocus();

    //jonas: setzen des Logos und der Kontaktinformationen
    QGraphicsScene *scene = new QGraphicsScene();
    QPixmap m("Logos/Whatsapp.jpg");
    scene->setBackgroundBrush(m.scaled(50,50,Qt::KeepAspectRatio,Qt::SmoothTransformation));
    ui->gV_logo->setScene(scene);
}

Dialog::~Dialog()
{
    delete ui;
}

void Dialog::answer()
{

}

void Dialog::on_pB_abbrechen_clicked()
{
    this->close();
}

void Dialog::on_pB_antworten_clicked()
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
