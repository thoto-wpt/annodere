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
#include<jsoncpp/json/json.h>

#define UNUSED(expr) (void)(expr)

namespace Annodere{
	using namespace std;

	// enum class Rpc_type{...};
	class Rpc_call{
		public:
			Rpc_call() : err(0),num_params(0),id_is_null(false){};
			int err;
			string method;
			int num_params;
			Json::Value* params;
			bool id_is_null;
			string id;
			bool compatible(Rpc_call& def);
			int* pdi(enum MHD_ValueKind kind, const char* key,
				const char* filename, const char* content_type,
				const char* transfer_encoding, const char* data, uint64_t off,
				size_t size);
		private:
			string qstring;
	};

	class Rpc_server{
		private:
			Rpc_call get_method(Json::Value &call);
			//static int handler(void *cls, struct MHD_Connection* connection,
			int handler(struct MHD_Connection* connection,
				const char* cc_url, const char* method, const char* version,
				const char* upload_data, size_t* upload_data_size,
				void ** con_cls);
			void* get_key(void*, enum MHD_ValueKind, const char*, const char*);
			string generate_error(signed int code);
			struct MHD_Daemon* mhd_daemon;
			int port;
			static const signed int err_parse=-32700;
			static const signed int err_method=-32601;
			static const signed int err_request=-32600;
			static const signed int err_params=-32602;
			static const signed int err_internal=-32603;
		public:
			Rpc_server(int port=10080);
			~Rpc_server();
	};
}
