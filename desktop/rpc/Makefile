LDFLAGS+= -lmicrohttpd -ljsoncpp
CXXFLAGS+= -g -Wall -Wunused -Wextra -pedantic -std=c++11

rpctest: rpc_server.o test.o
	$(CXX) $(CXXFLAGS) $^ -o $@ $(LDFLAGS)

test.o: rpc_server.h

rpc_server.o: rpc_server.h

all: rpc_server.o

clean:
	rm *.o
	test -f rpctest && rm rpctest
