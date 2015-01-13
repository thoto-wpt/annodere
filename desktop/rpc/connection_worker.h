#ifndef __CONNECTION_WORKER_H
#define __CONNECTION_WORKER_H
#include<string>
#include"rpc_server.h"

namespace Annodere{
	class Connection_worker{
		private:
			Rpc_server rpc_server;
			Rpc_method_wait* method_wait;
			Rpc_method_register* method_register;
		public:
			Connection_worker();
			~Connection_worker();
			void reply(string);
			static void get_notification(string);
	};
}
#endif // __CONNECTION_WORKER_H
