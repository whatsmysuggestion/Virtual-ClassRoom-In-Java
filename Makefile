#
#  Babylon Chat
#  Copyright (C) 1997-2005 J. Andrew McLaughlin
# 
#  Makefile
#

CC = javac
AR = jar
SIGN = jarsigner
JFLAGS = # -O -deprecation -verbose
KEYSTORE = visopsys.keystore
KEYALIAS = visopsys

CLIENT_DEPENDS = babylonBmpIO.class \
		babylonBugReportDialog.class \
		babylonBugReporter.class \
		babylonCanvas.class \
		babylonChatRoom.class \
		babylonClient.class \
		babylonCommand.class \
		babylonConstraints.class \
		babylonCreateRoomDialog.class \
		babylonInfoDialog.class \
		babylonInstantMessageDialog.class \
		babylonMessagingDialog.class \
		babylonMessage.class \
		babylonPanel.class \
		babylonPasswordDialog.class \
		babylonPasswordEncryptor.class \
		babylonRoomControlDialog.class \
		babylonRoomInfo.class \
		babylonRoomsDialog.class \
		babylonSettingsDialog.class \
		babylonStringManager.class \
		babylonTextDialog.class \
		babylonUser.class \
		babylonWindow.class

SERVER_DEPENDS	= babylonClientSocket.class \
		babylonCommand.class \
		babylonPasswordDialog.class \
		babylonPasswordEncryptor.class \
		babylonServerWindow.class \
		babylonStringManager.class \
		babylonUser.class \
		babylonUserTool.class \
		babylonUserToolDialog.class

all: babylon.jar 

signed: all
	${SIGN} -keystore ${KEYSTORE} babylon.jar ${KEYALIAS}

clean: 
	rm -Rf *.class *.jar *.log *.chatlog *~ User.passwords Messages.saved core

distclean: clean
	rm -f *.zip

babylon.jar: babylon.class babylonApplet.class babylonServer.class
	${AR} cf babylon.jar *.class *.properties

babylon.class: ${CLIENT_DEPENDS} babylon.java
	${CC} ${JFLAGS} babylon.java

babylonApplet.class: ${CLIENT_DEPENDS} babylonApplet.java
	${CC} ${JFLAGS} babylonApplet.java

babylonServer.class: ${SERVER_DEPENDS} babylonServer.java
	${CC} ${JFLAGS} babylonServer.java

%.class: %.java
	${CC} ${JFLAGS} $<
