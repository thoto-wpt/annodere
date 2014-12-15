#ifndef CONNECTION_WORKER_H
#define CONNECTION_WORKER_H

#include<QString>

class connection_worker
{
private:
    QString my_adress;
    QString connected_adress;
    bool connected;
public:
    connection_worker();
    void receive_notification();
    void forward_message();
};

#endif // CONNECTION_WORKER_H

