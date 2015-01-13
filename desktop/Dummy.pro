#-------------------------------------------------
#
# Project created by QtCreator 2014-11-10T14:37:04
# #------------------------------------------------- 
# QT -= gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = Dummy
TEMPLATE = app

 DEPENDPATH += .
 INCLUDEPATH += .
DESTDIR = $$PWD //Für relative Pfade

 CONFIG += qdbus
 win32:CONFIG += console


SOURCES += main.cpp\
    mainclass.cpp \
    replywindow.cpp \
    notificationwindow.cpp \
    rpc/rpc_server.cpp \
    rpc/connection_worker.cpp
#    connection_worker.cpp

HEADERS  += \
    mainclass.h \
    replywindow.h \
    notificationwindow.h \
    rpc/rpc_server.h \
    rpc/connection_worker.h
#     connection_worker.h

FORMS    += \
    replywindow.ui \
    notificationwindow.ui

LIBS += -lmicrohttpd -ljsoncpp
QMAKE_CXXFLAGS += -g -Wall -Wunused -Wextra -pedantic -std=c++11
QMAKE_CXXFLAGS_RELEASE += -std=c++11

