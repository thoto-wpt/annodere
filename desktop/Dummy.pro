#-------------------------------------------------
#
# Project created by QtCreator 2014-11-10T14:37:04
#
#-------------------------------------------------

 QT -= gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = Dummy
TEMPLATE = app

 DEPENDPATH += .
 INCLUDEPATH += .
DESTDIR = $$PWD //Für relative Pfade

 CONFIG += qdbus
 win32:CONFIG += console


SOURCES += main.cpp\
    globals.cpp \
    mainclass.cpp \
    rpc_server.cpp \
    replywindow.cpp \
    notificationwindow.cpp \
    connection_worker.cpp

HEADERS  += \
    globals.h \
    mainclass.h \
    rpc_server.h \
    replywindow.h \
    notificationwindow.h \
    connection_worker.h

FORMS    += \
    replywindow.ui \
    notificationwindow.ui
