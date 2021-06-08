MOOtcan
-------

MOOtcan is a simple MOO-client applet. It currently has support for showing URLs in a
browser frame.

Sindre Sørensen was the developer and maintainer of versions up until 0.1.16. Versions 0.1.20 and up has been developed and is maintained by Jan Rune Holmevik.

With the advent of Mac OS X 10.1, MOOtcan would no longer run properly under Internet Explorer. (version 5.1) I determined the cause to be the Java 1.0 event handling used by MOOtcan. To solve this problem, I have rewritten the program to use the Java 1.1 event handling scheme. The new program is being released as an alpha with version number 0.50. The program has not yet been optimized and I have noticed a slowdown in the client's TextArea performance.

Under Mac OS X MSIE 5.1, the MOOtcan applet is not always placed inside the Xpress java frame. Sometimes it will appear in the top left corner of the Xpress screen causing display problems. This issue does not appear to be a problem with MOOtcan, rather, it looks like the problem is caused by MSIE and the way it displays applets.

I am currently shipping both the latest stable version 0.1.20 and the 0.50 alpha version. enCore administrators can choose which one to run by editing the $xpress_client.java_client_archive property inside their MOO.

To run version 0.1.20 type

	@set $xpress_client.java_client_archive to "mootcan-012.jar"

To run version 0.50 type

	@set $xpress_client.java_client_archive to "mootcan-050.jar"
 
Note: MOOtcan will not work in browsers running on Mac OS X 10.0 - 10.0.4. These versions of Mac OS X does not support applets at all.


* Sindre Sørensen's ReadMe Notes for older versions.

much work has been done to make it work under as many Java Virtual Machines as
possible.

it has support for history, by pressing page-up and page-down (or ctrl-up and
ctrl-down). the history-items are editable; if you are currently editing some
text, and you want to refer to something you wrote earlier, the applet will
remember what you were editing.

I did some work to get updating of the outputarea as fast as possible.
java.awt.TextArea updates very slow, and even slower on some implementations.
the workaround is to update it in chunks, instead of updating it for every
line. now it scrolls about at the same speed as TinyFugue (MOO/MUD client for
text terminals).

the code is mixed java-1.0 & java-1.1. when run under java-1.1 the TextAreas
are a lot nicer, i.e. we get rid of superfluous scrollbars etc. but it should
work under java-1.0 (except under Netscape 3.x for unix).

currently it is line-oriented only, so it's probably not usable for
character-oriented MUDs etc.

see COPYING for copyright information. see INSTALL for info about installing
and running.

the latest MOOtcan source code and binaries are available from:

	http://cmc.uib.no/~sindre/mootcan/


---------------
Sindre S¯rensen
