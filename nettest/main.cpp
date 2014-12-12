// #include<sys/types.h>
// #include<sys/socket.h>
#include<cstdio>
#include<typeinfo>
#include<iostream>
#include<string>

#define UNUSED(expr) (void)(expr)

namespace mhttpd_handler {

	#ifdef __cplusplus
	extern "C"{
	#endif 
	#include <sys/types.h>
	#include <sys/select.h>
	#include <sys/socket.h>
	#include<microhttpd.h>
	#ifdef __cplusplus
	}
	#endif

	using namespace std;


	class Rpc_server{
		private:
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
	void* Rpc_server::get_key(void* cls, enum MHD_ValueKind kind,
			const char* c_key, const char* c_value){
		//...
		UNUSED(cls); UNUSED(kind); UNUSED(c_key); UNUSED(c_value);
		return nullptr;
	}
		
	int Rpc_server::handler(struct MHD_Connection* connection,
			const char* c_url, const char* method,
			const char* version, const char* upload_data,
			size_t* upload_data_size, void ** con_cls){
		UNUSED(method); UNUSED(version);
		UNUSED(upload_data); UNUSED(upload_data_size); UNUSED(con_cls);

		struct MHD_Response* response;
		int ret;
		string data; int code;
		
		string url=c_url;
		printf("URL: %s, %d\n",url.c_str(),port);
		if(url=="/annodere"){
			printf("OK:\n");
			data="{state:true}";
			code=200;
		}else{
			printf("404\n");
			data=generate_error(err_parse);
			code=404;
		}

		response=MHD_create_response_from_buffer(data.length(),
			reinterpret_cast<void*>(const_cast<char*>(data.c_str())),
			MHD_RESPMEM_MUST_COPY);
			//MHD_RESPMEM_PERSISTENT);
		if(MHD_add_response_header(response,"Content-type","application/json")
			==MHD_NO) printf("EEH resp head\n");
		ret=MHD_queue_response(connection, code, response);
		MHD_destroy_response(response);

		return ret; 
	}
	string Rpc_server::generate_error(signed int code){
		string err="{\"jsonrpc\": \"2.0\", \"error\": {\"code\": ";
		string err2=", \"message\": \"";
		string err3="\", \"id\": null}";
		switch(code){
			case err_parse: //-32700:
				err+=to_string(code)+err2+"Parse error"+err3; break;
			case err_method: //-32600:
				err+=to_string(code)+err2+"Invalid Request"+err3; break;
			case err_request: //-32601:
				err+=to_string(code)+err2+"Method not found"+err3; break;
			case err_params: //-32602:
				err+=to_string(code)+err2+"Invalid params"+err3; break;
			default:
			case err_internal: //-32603:
				err+=to_string(code)+err2+"Internal error"+err3; break;
		}
		return err;
	}
		
//-32000: to -32099 	Server error 	Reserved for implementation-defined server-errors.

	Rpc_server::Rpc_server(int pport):port(pport){
		MHD_AccessHandlerCallback c_handler=
			[](void* cls, struct MHD_Connection* connection,
			const char* c_url, const char* method,
			const char* version, const char* upload_data,
			size_t* upload_data_size, void ** con_cls)->int{
				Rpc_server* rpcsrv=static_cast<Rpc_server*>(cls);
				return rpcsrv->handler(connection, c_url, method, version,
					upload_data, upload_data_size, con_cls);
			};

		mhd_daemon=MHD_start_daemon(
			MHD_USE_SELECT_INTERNALLY|MHD_USE_IPv6|MHD_USE_PEDANTIC_CHECKS,
			port, nullptr, nullptr, c_handler, this, MHD_OPTION_END);
			/* port, nullptr, nullptr,
			reinterpret_cast<MHD_AccessHandlerCallback>(&Rpc_server::handler),
			this, MHD_OPTION_END);*/
		if(mhd_daemon==nullptr) 
			printf("EEH: Daemon initialisation failed\n"); 
	}
	Rpc_server::~Rpc_server(){
		MHD_stop_daemon(mhd_daemon);
	}
}


using namespace std;
int main(int argc, char** argv){
	UNUSED(argc);UNUSED(argv);

	mhttpd_handler::Rpc_server* srv=new mhttpd_handler::Rpc_server();
	static_cast<void>(getchar());
	delete srv;
	return 0;
}
