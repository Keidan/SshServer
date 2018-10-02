SshServer
===

(GPL) Android SSH server (SSH/SCP/SFTP) is a FREE software.

This application allows you to create several instances of SSH servers (SSH/SCP/SFTP).

For non-rooted devices, the functionalities are severely limited (permission issues).

To limit (delete) issues related to /system/bin/sh and tty (below)

* /system/bin/sh: can't find: tty fd No such device or address.
* /system/bin/sh: warning: won't have full job control.

The process management is done directly via a native code with the direct use of the character file /dev/ptm.


__IMPORTANT:__ I don't know why, but, the code doesn't work properly if the build variant is not set to "debug".


Instructions
============


download the software :

	mkdir devel
	cd devel
	git clone git@github.com:Keidan/SshServer
	cd SshServer
 	Use with android studio

	
/!\ To compile this project, you must also install the CMAKE, LLDB (debug only) and the NDK packages.

(see https://codelabs.developers.google.com/codelabs/android-studio-cmake)

License (like GPL)
==================
