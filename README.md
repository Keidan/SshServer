SshServer
===

(GPL) Android SSH server (SSH/SCP/SFTP) is a FREE software.

This application create some instances of SSH servers (SSH/SCP/SFTP).

Note that access to the'sh' file (/system/bin/sh) seems to require a routed device.


Without a routed device, the message below appears and the features are severely degraded:
* /system/bin/sh: can't find: tty fd No such device or address.
* /system/bin/sh: warning: won't have full job control.

It is also possible that a new version of busybox may need to be installed to extend the shell's functionality.


Instructions
============


download the software :

	mkdir devel
	cd devel
	git clone git@github.com:Keidan/SshServer
	cd SshServer
 	Use with android studio

License (like GPL)
==================
