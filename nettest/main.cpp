// #include<sys/types.h>
// #include<sys/socket.h>
#include<cstdio>
#include<typeinfo>
#include<iostream>
#include<string>

#include"rpc_server.h"
#define UNUSED(expr) (void)(expr)

using namespace std;
int main(int argc, char** argv){
	UNUSED(argc);UNUSED(argv);

	Annodere::Rpc_server* srv=new Annodere::Rpc_server();
	static_cast<void>(getchar());
	delete srv;
	return 0;
}
