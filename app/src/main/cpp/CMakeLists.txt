# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.4.1)


#find_library(# Defines the name of the path variable that stores the
#              # location of the NDK library.
#              log-lib
#              # Specifies the name of the NDK library that
#              # CMake needs to locate.
#              log)

set(SOURCE_FILES fr_ralala_sshd_net_ptm_NativeProcessPTM.cpp)
# Creates the project's shared lib: libnative-process-ptm.so.
# The lib is loaded by this project's Java code in fr/ralala/sshd/net/ptm/NativeProcessPTM.java:
#     System.loadLibrary("native-process-ptm");
# The lib name in both places must match.
add_library(native-process-ptm SHARED ${SOURCE_FILES})

# Links your native library against one or more other native libraries.
#target_link_libraries( # Specifies the target library.
#                       native-process-ptm
#                       # Links the log library to the target library.
#                       ${log-lib} )