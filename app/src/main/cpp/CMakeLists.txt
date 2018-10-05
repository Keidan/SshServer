# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.4.1)

set(jni_library native-process-ptm)

#################### Library
set(SOURCE_FILES fr_ralala_sshd_net_ptm_NativeProcessPTM.cpp)
# Creates the project's shared lib: libnative-process-ptm.so.
# The lib is loaded by this project's Java code in fr/ralala/sshd/net/ptm/NativeProcessPTM.java:
#     System.loadLibrary("native-process-ptm");
# The lib name in both places must match.
add_library(${jni_library} SHARED ${SOURCE_FILES})
