#ifndef RPC_SERVER_H
#define RPC_SERVER_H

#include<QString>

class rpc_server
{
private:
    QString server_socket;
    QString callback;
public:
    virtual void listen()=0;
    virtual void rpc_serverX(QString callback)=0;
};

class rpc_server_passive : public rpc_server
{
public:
    rpc_server_passive();
    virtual void listen();
    virtual void rpc_serverX(QString callback);

};

class rpc_server_active : public rpc_server
{
public:
    rpc_server_active();
    virtual void listen();
    virtual void rpc_serverX(QString callback);

};

#endif // RPC_SERVER_H
