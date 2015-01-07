#include"rpc_server.h"
#include<cassert>
#include<cstring>

namespace Annodere{
	using namespace std;

	/**
	 * Method call to register client to server
	 * @returns token
	 **/
	Json::Value Rpc_method_register::call(Json::Value){
		return nullptr;
	}

	/**
	 * Check compatibility between actual call and method signature
	 * @returns true if you may run this method call
	 **/
	bool Rpc_method::compatible(Json::Value& params) {
		if(params==nullptr && arguments.size()==0) return true;
		else if(!params.isArray())
			return arguments.size()==1&&params.isConvertibleTo(arguments[0]);
		if(params.size()!=arguments.size()) return false;
		
		int i=0;
		for(auto p: params){
			if(!p.isConvertibleTo(arguments[i])) return false;
		}
		return true; 
	}

	/**
	 * get JSON RPC call for debugging reasons
	 * @returns RPC request in JSON fromat
	 **/
	string Rpc_call::get_json_rpc(void){
		return callstr;
	}

	/**
	 * decodes POST-encoded data
	 * @params post	the data to be decoded
	 **/
	void Rpc_call::unescape_post(string& post){
		size_t i,j; // FIXME: Type?
		string res;
		i=0x00;j=-0x01; // start and end of chunk
		res.reserve(post.length()); // urldecoded can't be longer
		while((j=post.find_first_of("+%",j+1))!=string::npos){
			if(post[j]=='+') post[j]=' ';// replace + by space
			else if(post[j]=='%') { // decode urlencoded
				post[j]=stoul(post.substr(j+1,2),nullptr,16); // hex!
				res.append(post.substr(i,j-i+1));
				i=j+3;
			}
		} res.append(post.substr(i,string::npos)); // fill up with rest
		post=res;
	}
	
	/**
	 * processes chunk of post data sent by MHD
	 * @param creq		request data
	 * @param length	length of data chunk
	 * @return sequence of data chunk
	 **/
	int Rpc_call::process(const char* creq,size_t length){
		post_data.emplace(creq,length);
		post_size+=length;
		return call_num++;
	}

	/**
	 * parses POST request and parses JSON if request data found
	 * @warning may consume large amounts of memory if large request gets in
	 * @returns error code or 0x00 on success
	 **/
	signed int Rpc_call::parse(){
		// TODO: prevent DOS by limiting request size
		string post_string;

		// ===
		// put post request data together
		post_string.reserve(post_size);
		while(!post_data.empty()){
			post_string+=post_data.front();
			post_data.pop();
		}

		// ===
		// parse post data
		size_t from, to, end;// FIXME Type? (64 bits)
		from=0x00; // start of var name
		to=post_string.find("="); // end of var name
		while(to!=string::npos){
			assert(from!=string::npos);
			end=post_string.find("&",to); // find seperator to next variable

			// we just look for request variable and store it
			if(post_string.substr(from,to-from)=="request")
				callstr=post_string.substr(to+1,
					(end==string::npos)?end:end-to-1);

			// set start of variable name to end of current variable content
			from=(end==string::npos)?end:end+1;
			to=post_string.find("=",from); // look for start of variable content
		}
		if(callstr.empty()) // unable to get request data
			return Rpc_server::err_request;

		// ===
		// decode request
		unescape_post(callstr);

		// ===
		// finally parse JSON data
		Json::Reader json_reader;
		if(!json_reader.parse(callstr, json_root, false))
			return Rpc_server::err_parse;

		if(json_root.isArray()) {
			return Rpc_server::err_internal; // TODO
		}else return json_parse();
	}

	signed int Rpc_call::json_parse(){
		Json::Value jv_version=json_root["jsonrpc"];
		jv_id=json_root["id"]; // declared at object
		Json::Value jv_method=json_root["method"];
		Json::Value jv_params=json_root["params"];

		// version string must match
		if(!(jv_version.isString()&&jv_version.asString()=="2.0"))
			return Rpc_server::err_request;

		// method must be string
		if(!jv_method.isString()) return Rpc_server::err_request;
		else method=jv_method.asString();

		// id must be present as null value or as unsigned integer or string
		if(jv_id.isString()||jv_id.isUInt()) id=jv_id.asString();
		else if(jv_id.isNull()) id_is_null=true;
		else return Rpc_server::err_request;

		// there may be parameters
		if(jv_params.isArray()) params=new Json::Value(jv_params);
		else if(jv_params.isNull()) params=nullptr;
		return 0x00;
	}

	/**
	 * Rpc_call constructor
	 **/
	Rpc_call::Rpc_call() : id_is_null(false),post_size(0), call_num(0){
		params=nullptr;
	}

	/**
	 * Rpc_call constructor for multi method calls
	 **/
	Rpc_call::Rpc_call(Json::Value val) : id_is_null(false),post_size(0),
			call_num(0){
		json_root=val;
		params=nullptr;
		json_parse();
	}

	/**
	 * Handles every HTTP request to MHD server instance
	 **/
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
		if(url=="/annodere"){
			if(*con_cls==NULL){ // new call: create Rpc_call
				printf("URL: %s, %d\n",url.c_str(),port);
				Rpc_call* call=new Rpc_call();
				*con_cls=static_cast<void*>(call);
				return MHD_YES;
			}else{
				// process post data
				Rpc_call* call=static_cast<Rpc_call*>(*con_cls);
				 if(*upload_data_size){
					call->process(upload_data,*upload_data_size);
					*upload_data_size=0x00;
					return MHD_YES;
				}else{ // recieving post data finished: process request.
					signed int err;
					if((err=call->parse())==0x00){
						data="{state:true}";
						code=200;
					}else{
						data=generate_error(err);
						code=422;
					}
					printf("\t%s\n",(call->get_json_rpc()).c_str());
					delete call;
				}
			}
		}else{ // this is not a rpc call
			printf("\t404\n");
			data=generate_error(err_parse);
			code=404;
		}

		response=MHD_create_response_from_buffer(data.length(),
			reinterpret_cast<void*>(const_cast<char*>(data.c_str())),
			MHD_RESPMEM_MUST_COPY);
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
		if(mhd_daemon==nullptr) 
			printf("EEH: Daemon initialisation failed\n"); 
	}
	Rpc_server::~Rpc_server(){
		MHD_stop_daemon(mhd_daemon);
	}
}
