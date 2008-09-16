#
# Gererated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc.exe
CCC=g++.exe
CXX=g++.exe
FC=

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/javafx_release/Cygwin-Windows

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/src/configuration.o \
	${OBJECTDIR}/src/javafxc.o \
	${OBJECTDIR}/src/util.o \
	${OBJECTDIR}/src/javafxdoc.o \
	${OBJECTDIR}/src/javafx.o

# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=-mno-cygwin -s
CXXFLAGS=-mno-cygwin -s

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/Release/javafx.exe

dist/Release/javafx.exe: ${OBJECTFILES}
	${MKDIR} -p dist/Release
	${LINK.cc} -o dist/Release/javafx ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/src/configuration.o: src/configuration.cpp 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.cc) -O2 -DPROJECT_JAVAFX -o ${OBJECTDIR}/src/configuration.o src/configuration.cpp

${OBJECTDIR}/src/javafxc.o: src/javafxc.cpp 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.cc) -O2 -DPROJECT_JAVAFX -o ${OBJECTDIR}/src/javafxc.o src/javafxc.cpp

${OBJECTDIR}/src/util.o: src/util.cpp 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.cc) -O2 -DPROJECT_JAVAFX -o ${OBJECTDIR}/src/util.o src/util.cpp

${OBJECTDIR}/src/javafxdoc.o: src/javafxdoc.cpp 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.cc) -O2 -DPROJECT_JAVAFX -o ${OBJECTDIR}/src/javafxdoc.o src/javafxdoc.cpp

${OBJECTDIR}/src/javafx.o: src/javafx.cpp 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.cc) -O2 -DPROJECT_JAVAFX -o ${OBJECTDIR}/src/javafx.o src/javafx.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/javafx_release
	${RM} dist/Release/javafx.exe

# Subprojects
.clean-subprojects:
