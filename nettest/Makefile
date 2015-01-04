LDFLAGS+= -lmicrohttpd -ljsoncpp
CXXFLAGS+= -g -Wall -Wunused -Wextra -pedantic -std=c++11

rpcmain: rpc_server.o main.o
	$(CXX) $(CXXFLAGS) $^ -o $@ $(LDFLAGS)

main.o: rpc_server.h

rpc_server.o: rpc_server.h

all: rpcmain

clean:
	rm *.o rpcmain
