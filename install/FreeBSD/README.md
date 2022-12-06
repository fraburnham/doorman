# Installing on FreeBSD

This is a somewhat specific installation setup. It configures doorman to start sshd when it hears a valid knock and the login user to stop sshd on logout.

## Daemon

1. Install some version of [[https://www.freebsd.org/java/][openjdk]] (there are some post-install steps to make procfs and devfs ready)
1. Create daemon user
   1. `doas pw useradd -n _doorman -L daemon -s /sbin/nologin -m -h -`
1. Put `doorman.jar` and `.doorman.edn` in `_doorman`'s home folder
1. Install script for daemon to run and configure doas
   1. `cp start-sshd.sh /usr/home/_doorman/`
1. The script should be r-x
	  1. `chmod 555 /usr/home/doorman/start-sshd.sh`
1. The daemon user should have permission to run it without a password but no other doas permissions
	  1. `permit nopass _doorman as root cmd /usr/home/doorman/start-sshd.sh`
1. Install the start script to be called by rc script
   1. `cp start.sh /usr/home/_doorman/`
1. Install the rc script
   1. `cp doorman.sh /etc/rc.d/doorman`
1. Install the logout script (my user's shell is tcsh, your logout script location may differ)
   1. `cp logout ~/.logout`
   1. doas permissions to make it quicker
	  1. `permit nopass <USER> as root cmd service`
1. Enable the server
   1. `doas service doorman enable`
1. Start doorman
   1. `doas service doorman start`
1. Profit

## Client

1. Install some version of [[https://www.freebsd.org/java/][openjdk]] (there are some post-install steps to make procfs and devfs ready)
1. Put `.doorman.edn` in your home folder
1. Knock!
   1. `java -jar path/to/doorman.jar help`
