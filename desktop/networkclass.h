#ifndef NETWORKCLASS_H
#define NETWORKCLASS_H

#include<QString>

class NetworkClass
{
private:
    QString my_adress;
    QString connected_adress;
    bool connected;

public:
    NetworkClass();
    void receive_notification();
    void forward_message();
};

#endif // NETWORKCLASS_H
