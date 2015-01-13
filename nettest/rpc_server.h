#ifdef __cplusplus
extern "C"{
#endif 
// #include<sys/types.h>
// #include<sys/select.h>
// #include<sys/socket.h>
#include<microhttpd.h>
#include<pthread.h>
#ifdef __cplusplus
}
#endif
#include<string>
#include<queue>
#include<unordered_map>
#include<functional>
#include<jsoncpp/json/json.h>

#define UNUSED(expr) (void)(expr)

namespace Annodere{
	using namespace std;

	class Rpc_method{
		public:
			virtual string get_name()=0;
			virtual vector<Json::ValueType> get_arguments()=0;
			virtual string call(Json::Value&,Json::Value&){return "";};
			virtual ~Rpc_method(){};
			bool compatible(Json::Value& params);
		protected:
			string generate_result(Json::Value&, Json::Value&);
	};

	class Rpc_method_notify: public Rpc_method{
		private:
			function<void (const string)> callback;
		public:
			Rpc_method_notify(function<void (const string)> cb);
			string get_name(){return "notify";};
			vector<Json::ValueType> get_arguments(){
				vector<Json::ValueType> a={Json::stringValue,Json::stringValue};
				return a;
			};

			string call(Json::Value&, Json::Value&);
	};

	class Rpc_method_register: public Rpc_method{
		public:
			string get_name(){return "register";};
			vector<Json::ValueType> get_arguments(){
				vector<Json::ValueType> a={Json::stringValue};
				return a;
			};

			string call(Json::Value&, Json::Value&);
	};
	class Rpc_method_wait: public Rpc_method{
		private:
			string reply;
			pthread_mutex_t* pmutti;
			pthread_cond_t* pcondi;
		public:
			Rpc_method_wait();
			~Rpc_method_wait();
			string get_name(){return "wait";};
			vector<Json::ValueType> get_arguments(){
				vector<Json::ValueType> a={Json::stringValue};
				return a;
			};

			string call(Json::Value&, Json::Value&);
			void send_reply(string);
	};


	class Rpc_call{
		public:
			Rpc_call();
			Rpc_call(Json::Value);
			virtual ~Rpc_call(){
				 if(params!=nullptr) delete params; }

			string method;
			Json::Value* params;
			bool id_is_null;
			Json::Value jv_id;

			int process(const char*,size_t);
			signed int parse();
			string get_json_rpc();
		private:
			//! storage for post data chunks
			queue<string> post_data;
			//! JSON-RPC call as string
			string callstr;
			//! number of chunks FIXME: Type (64 bits?)
			int post_size; 
			//! number of latest chunk
			int call_num;
			//! basis of method data
			Json::Value json_root;
			signed int json_parse();
			void unescape_post(string&);
	};

	class Rpc_server{
		private:
			Rpc_call get_method(Json::Value &call);
			int handler(struct MHD_Connection* connection,
				const char* cc_url, const char* method, const char* version,
				const char* upload_data, size_t* upload_data_size,
				void ** con_cls);
			struct MHD_Daemon* mhd_daemon;
			int port;
			//unordered_multimap<string,Rpc_method*> methods;
			unordered_map<string,Rpc_method*> methods;
			string dispatch(string,Json::Value,Json::Value);
		public:
			void register_method(Rpc_method*);
			static string generate_error(signed int code);
			static const signed int err_parse=-32700;
			static const signed int err_method=-32601;
			static const signed int err_request=-32600;
			static const signed int err_params=-32602;
			static const signed int err_internal=-32603;
			Rpc_server(int port=10080);
			~Rpc_server();
	};

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
