# SshServer 
[![Build Status](https://img.shields.io/travis/Keidan/SshServer/master.svg?style=plastic)](https://travis-ci.org/Keidan/SshServer)
[![GitHub license](https://img.shields.io/github/license/Keidan/SshServer.svg?style=plastic)](https://github.com/Keidan/SshServer/blob/master/license.txt)



(GPL) Android SSH server (SSH/SCP/SFTP) is a FREE software.

This application allows you to create several instances of SSH servers (SSH/SCP/SFTP).

:exclamation::warning::exclamation: Due to the use of apache MINA 2.0 which uses java methods that are only implemented since Android 8.0, this branch is not compatible with an Android version < 8.0

:warning: Due to the depredation of encryption providers (algorithms) [see](https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html) and without a solution at the mina-sshd API level, this application will no longer be supported after Android 9.0.

## Notice

For non-rooted devices, the functionalities are severely limited (permission issues).

To limit (delete) issues related to /system/bin/sh and tty (below)

* /system/bin/sh: can't find: tty fd No such device or address.
* /system/bin/sh: warning: won't have full job control.

The process management is done directly via a native code with the direct use of the character file /dev/ptm.


:exclamation::exclamation::exclamation: I don't know why, but, the code doesn't work properly if the build variant is not set to "debug" :anger:.

Since /system/bin/ls and other basic binaries are not available without a root device, 
SCP and SFTP features will not be available for these devices.



## Instructions


Download the software :

	mkdir devel
	cd devel
	git clone git@github.com:Keidan/SshServer
	cd SshServer
 	Use with android studio

	
:warning: To compile this project, you must also install the CMAKE, LLDB (debug only) and the NDK packages.

See [android studio cmake](https://codelabs.developers.google.com/codelabs/android-studio-cmake)


## License

[GPLv3](https://github.com/Keidan/SshServer/blob/master/license.txt)
