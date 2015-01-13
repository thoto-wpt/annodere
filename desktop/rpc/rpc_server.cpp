#include"rpc_server.h"
#include<cassert>
#include<cstring>
#include<stdexcept>

namespace Annodere{
	using namespace std;

	string init_auth_token="1234";
	string init_sess_token;

	/**
	 * Generate valid response of method
	 * @returns string to be sent back
	 **/
	string Rpc_method::generate_result(const Json::Value &id,
			const Json::Value &result){
		static Json::FastWriter writer;
		Json::Value reply(Json::objectValue);
		reply["result"]=result;
		reply["id"]=id;
		reply["jsonrpc"]="2.0";
		return writer.write(reply);
	}

	/**
	 * Method call to register client to server
	 * @returns token / negative int value on failure
	 **/
	string Rpc_method_register::call(const Json::Value &val,
			const Json::Value& id){
		Json::Value ret;
		if(init_auth_token.empty()) ret=-2;
		else if(val[0]==init_auth_token){
			//generate session token
			init_sess_token.resize(33);
			for(int i=0;i<32;i++){
				init_sess_token[i]=(rand()%74)+0x30;
			}
			ret=init_sess_token;
			init_auth_token="";
			printf("Session token: %s \n",init_sess_token.c_str());
		}else {
			ret=-1;
		}
		return generate_result(id, ret);
	}

	/**
	 * constructor for wait RPC call. Intializes IPC mutexes
	 **/
	Rpc_method_wait::Rpc_method_wait(){
		pmutti=(pthread_mutex_t*)malloc(sizeof(pthread_mutex_t));
		pcondi=(pthread_cond_t*)malloc(sizeof(pthread_cond_t));
		pthread_mutex_init(pmutti,NULL);
		pthread_cond_init(pcondi,NULL);
	}

	Rpc_method_wait::~Rpc_method_wait(){
		free(pmutti); free(pcondi);
	}

	/**
	 * Wait for message to be recieved by phone
	 * @returns null if no reply was sent or message to send
	 **/
	string Rpc_method_wait::call(const Json::Value &val,const Json::Value& id){
		Json::Value ret;

		if(val[0]==init_sess_token){
			// time definition to abort wait in order to handle session timeout
			struct timeval now;
			struct timespec timeout;
			gettimeofday(&now,NULL);
			timeout.tv_sec=now.tv_sec+30; // wait 60 seconds
			timeout.tv_nsec=now.tv_usec*1000;

			pthread_mutex_lock(pmutti);
			if(!reply.empty() || // there is a message pending or ...
					//message was recieved
					pthread_cond_timedwait(pcondi,pmutti,&timeout)==0x00){
				ret=reply;
				reply="";
			}else ret=Json::nullValue;
			pthread_mutex_unlock(pmutti);
		}else ret=Json::nullValue;

		return generate_result(id, ret);
	}

	/**
	 * Send reply to every waiting client (session)
	 * @params message message to be sent do client
	 **/
	void Rpc_method_wait::send_reply(const string message){
		if(pmutti!=nullptr&&pcondi!=nullptr){
			pthread_mutex_lock(pmutti);
			reply=message;
			pthread_mutex_unlock(pmutti);
			pthread_cond_broadcast(pcondi);
		}
	}

	/**
	 * Constructor of notify method, which is called by the client if the
	 * desktop should display a notifcation. Must recieve callback to call
	 * when message arrives.
	 * @params cb Callback method to display notification
	 **/
	Rpc_method_notify::Rpc_method_notify(function<void (const string)> cb){
		callback=cb;
	}

	/**
	 * RPC-method to recieve notification and display it
	 **/
	string Rpc_method_notify::call(const Json::Value &val,
			const Json::Value& id){
		Json::Value ret;

		if(val[0]==init_sess_token){
			callback(val[1].asString());
			ret=true;
		}else ret=Json::nullValue;

		return generate_result(id, ret);
	}

	/**
	 * Check compatibility between actual call and method signature
	 * @returns true if you may run this method call
	 **/
	bool Rpc_method::compatible(const Json::Value& params) const {
		auto arguments=get_arguments();
		if(params.isNull() && arguments.size()==0) return true;
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
	 * @params post	the data to be decoded. This data will not stay the same.
	 **/
	void Rpc_call::unescape_post(string& post) const{
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
	int Rpc_call::process(const char* creq,const size_t length){
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

	/**
	 * Parses json data of call and extracts method and params
	 * @returns error code or 0x00 on success
	 **/
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
		if(jv_id.isNull()) id_is_null=true;
		else if(!jv_id.isString()&&!jv_id.isUInt())
			 return Rpc_server::err_request;

		// there may be parameters
		if(jv_params.isArray()||jv_params.isNull())
			params=new Json::Value(jv_params);
		else params=new Json::Value(Json::nullValue);
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
	 * FIXME: test this function and implement further requirements in parser
	 **/
	Rpc_call::Rpc_call(const Json::Value val) : id_is_null(false),post_size(0),
			call_num(0){
		json_root=val;
		params=nullptr;
		json_parse();
	}

	/**
	 * dispatches method call
	 * a method will be called on its object used at register_method call
	 * TODO: call multiple methods with different signatures but same name
	 * @params name method name
	 * @params params parameters for method call
	 * @params id ID of method call in JSON-RPC request
	 * @returns result string
	 **/
	string Rpc_server::dispatch(const string name,const Json::Value params,
			const Json::Value id) const{
		Rpc_method* m;
		try{ // find method
			m=methods.at(name);
		}catch(const std::out_of_range oor){ //method not found
			return generate_error(err_method);
		}

		// check compatibility of parameters
		if(m->compatible(params)) return m->call(params, id);
		else return generate_error(err_params);
	}

	/**
	 * register method to RPC server
	 * @params m pointer to Rpc_method
	 **/
	void Rpc_server::register_method(Rpc_method* m){
		pair<string,Rpc_method*> element(m->get_name(),m);
		methods.insert(element);
	}

	/**
	 * Handles every HTTP request to MHD server instance
	 **/
	int Rpc_server::handler(struct MHD_Connection* connection,
			const char* c_url, const char* method,
			const char* version, const char* upload_data,
			size_t* upload_data_size, void ** con_cls) const{
		UNUSED(method); UNUSED(version);

		struct MHD_Response* response;
		int ret;
		string data; int code;

		string url=c_url;
		if(url=="/annodere"){
			if(*con_cls==NULL){ // new call: create Rpc_call
				printf("URL: %s, %d\n",url.c_str(),port); // FIXME: rem
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
						data=dispatch(call->method,*(call->params),call->jv_id);
						code=200;
					}else{
						data=generate_error(err);
						code=422;
					}
//					printf("\t%s\n",(call->get_json_rpc()).c_str());
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

	/**
	 * return JSON reply for given numeric error id
	 * @params code numeric error id (one of err_parse, err_method, err_request
	 *  err_params or err_internal)
	 * @returns JSON string (not generated by jsoncpp library)
	 **/
	string Rpc_server::generate_error(const signed int code){
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
//-32000: to -32099 	Server error 
// Reserved for implementation-defined server-errors.
		}
		return err;
	}

	/**
	 * create new RPC server
	 * @params port TCP port to listen on
	 **/
	Rpc_server::Rpc_server(const int pport):port(pport){
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
			MHD_USE_THREAD_PER_CONNECTION|MHD_USE_IPv6|MHD_USE_PEDANTIC_CHECKS,
			port, nullptr, nullptr, c_handler, this, MHD_OPTION_END);
		if(mhd_daemon==nullptr)
			printf("EEH: Daemon initialisation failed\n");
	}

	/**
	 * stop RPC server and destroy all methods used
	 **/
	Rpc_server::~Rpc_server(){
		MHD_stop_daemon(mhd_daemon);
		for(auto m: methods){
			delete m.second;
		}
	}

	/**
	 * Constructor of connection_worker. Creates RPC server and registers
	 * methods to it.
	 **/
	Connection_worker::Connection_worker(): rpc_server() {
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
	}
}
