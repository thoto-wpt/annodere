// #include<sys/types.h>
// #include<sys/socket.h>
#include<cstdio>
#include<typeinfo>
//#include<iostream>
#include<string>

#include"rpc_server.h"
#define UNUSED(expr) (void)(expr)

using namespace std;
int main(int argc, char** argv){
	UNUSED(argc);UNUSED(argv);

	Annodere::Connection_worker* srv=new Annodere::Connection_worker();
	char buffer[80];
	while(fgets(buffer,80,stdin)){
		printf("\t\"%s\"\n",buffer);
		srv->reply(buffer);
	}
	//static_cast<void>(getchar());
	delete srv;
	return 0;
}
