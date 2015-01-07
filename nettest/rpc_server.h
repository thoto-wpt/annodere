#ifdef __cplusplus
extern "C"{
#endif 
// #include<sys/types.h>
// #include<sys/select.h>
// #include<sys/socket.h>
#include<microhttpd.h>
#ifdef __cplusplus
}
#endif
#include<string>
#include<queue>
#include<jsoncpp/json/json.h>

#define UNUSED(expr) (void)(expr)

namespace Annodere{
	using namespace std;

	class Rpc_method{
		public:
			const string name;
			const vector<Json::ValueType> arguments;
			virtual Json::Value call(Json::Value)=0;
			bool compatible(Json::Value& params);
	};

	class Rpc_method_register: public Rpc_method{
		public:
			const string name="register";
			const vector<Json::ValueType> arguments=
				{Json::intValue,Json::stringValue};
			Json::Value call(Json::Value);
	};

	class Rpc_call{
		public:
			Rpc_call();
			Rpc_call(Json::Value);
			~Rpc_call(){ if(params!=nullptr) delete params; }

			string method;
			Json::Value* params;
			bool id_is_null;
			string id;
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
			//static int handler(void *cls, struct MHD_Connection* connection,
			int handler(struct MHD_Connection* connection,
				const char* cc_url, const char* method, const char* version,
				const char* upload_data, size_t* upload_data_size,
				void ** con_cls);
			string generate_error(signed int code);
			struct MHD_Daemon* mhd_daemon;
			int port;
		public:
			static const signed int err_parse=-32700;
			static const signed int err_method=-32601;
			static const signed int err_request=-32600;
			static const signed int err_params=-32602;
			static const signed int err_internal=-32603;
			Rpc_server(int port=10080);
			~Rpc_server();
	};
}
