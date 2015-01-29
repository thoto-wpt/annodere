#include"connection_worker.h"
#include"main.h"

namespace Annodere{
	/**
	 * Constructor of connection_worker. Creates RPC server and registers
	 * methods to it.
	 **/
	Connection_worker::Connection_worker(): rpc_server(10080) {
		method_wait=new Rpc_method_wait();
		rpc_server.register_method(new Rpc_method_register());
		rpc_server.register_method(method_wait);
		rpc_server.register_method(new Rpc_method_notify(get_notification));
	}
	Connection_worker::~Connection_worker(){}

	/**
	 * send reply to clients via rpc wait reply
	 * @params msg message to send
	 **/
	void Connection_worker::reply(string msg){
		method_wait->send_reply(msg);
	}

	/**
	 * handle notification
	 * @params msg message recieved
	 **/
	void Connection_worker::get_notification(string msg){
		printf("got message: %s\n",msg.c_str());
		not_window->set_message(msg);
	}
}
