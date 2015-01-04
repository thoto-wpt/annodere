#include"rpc_server.h"

namespace Annodere{
	using namespace std;

	bool Rpc_call::compatible(Rpc_call& def) {
		if(num_params!=def.num_params) return false;
		auto dp=(def.params)->begin();
		for(auto p: *params){
//		for(int i=0;i<num_params;i++){
//			if(!params[i].isConvertibleTo(def.params[i].type())) return false;
			if(p.isConvertibleTo((*dp).type())) return false;
			dp++;
		}
		return true;
	}

/* 	static int* Rpc_call::spdi(void* cls, MHD_ValueKind kind, const char* key,
			const char* filename, const char* content_type,
			const char* transfer_encoding, const char* data, uint64_t off,
			size_t size){// post data iterator
		Rpc_call* t=static_cast<Rpc_call*>(cls);
		return t->pdi(kind, key, filename, content_type, transfer_encoding,
			data, off, size);
	} */
		

	Rpc_call Rpc_server::get_method(Json::Value &json){
		Rpc_call call;

		Json::Value version=json["jsonrpc"];
		Json::Value id=json["id"];
		Json::Value method=json["method"];
		Json::Value params=json["params"];

		if(!(version.isString()&&version.asString()=="2.0")){
			call.err=err_request; return call;
		}

		if(method.isString()) call.method=method.asString();
		else{ call.err=err_request; return call; }

		if(id.isString()||id.isUInt()) call.id=id.asString();
		else if(id.isNull()) call.id_is_null=true;
		else{ call.err=err_request; return call; }

		if(params.isArray()) call.params=new Json::Value(params);
		else if(params.isNull()) call.params=nullptr;
		else{ call.err=err_request; return call; }

		return call;
	}

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
		Json::Reader json_reader;
		Json::Value json_root;
		string request;
		

		string url=c_url;
		printf("URL: %s, %d\n",url.c_str(),port);
		if(url=="/annodere"){
			if(upload_data!=NULL) // C!
				request=string(upload_data, *upload_data_size);
			else request="EMPTY"; 
			printf("\trequest: %s\n", request.c_str());
			if(!json_reader.parse("{ \"jsonrpc\": \"2.0\", \"method\": \"foo\", \"params\": [123, \"foo\"], \"id\": null }", json_root, false)){
				printf("\tParse Error:\n");
				data=generate_error(err_parse);
				code=404;
			}
			else{
				Rpc_call call=get_method(json_root);
				string method=call.method;
				if(call.err!=0x00) {
					printf("\tError:\n");
					data=generate_error(call.err);
					code=400;
				}else {
					printf("\tMethod: %s\n", method.c_str());
					if(call.params==nullptr) printf("\tno parameters given.\n");
					else{
						for(auto p: *call.params){
							if(p.isInt()) printf("0x%x, ",p.asInt());
							else printf("%s, ",p.asString().c_str());
						}
						printf("EOL \n");
					}

					data="{state:true}";
					code=200;
				}
			}
		}else{
			printf("\t404\n");
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
				err+=to_string(code)+err2+"Method not found"+err3; break;
			case err_request: //-32601:
				err+=to_string(code)+err2+"Invalid Request"+err3; break;
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
