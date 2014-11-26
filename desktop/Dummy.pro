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
    dialog.cpp \
    globals.cpp \
    notification.cpp \
    networkclass.cpp \
    mainclass.cpp

HEADERS  += \
    dialog.h \
    globals.h \
    notification.h \
    networkclass.h \
    mainclass.h

FORMS    += \
    dialog.ui \
    notification.ui
