#ifndef NOTIFY_HANDLER_H
#define NOTIFY_HANDLER_H

#include<QObject>
#include<QString>
#include<string>
#include"main.h"

class notify_handler: public QObject {
	Q_OBJECT
	public:
		notify_handler(std::string notification){
			QObject::connect(this,SIGNAL(send_notify(QString)),
				not_window,SLOT(on_notify(QString)));
			emit send_notify(QString(notification.c_str()));	
		}
	signals:
		void send_notify(QString notification);
};
			
	
#endif // NOTIFY_HANDLER_H
