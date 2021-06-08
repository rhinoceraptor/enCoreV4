/*
 *  The MOOtcan MOO-client
 *  Copyright (C) 1999 Sindre S¯rensen
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


import java.applet.*;

/**
 * a thread that deals with resizing and the browser leaving the applet.
 * idea from <A HREF=http://www.javaworld.com/javatips/jw-javatip8.html>JavaWorld, Java Tip 8: Threads, Netscape, and the resize problem</A>
 */
class KillAllThread extends Thread {

  private Applet applet;
  final int INTERVAL = 60000; // the browser gets 60 seconds to resize, or get back to the applet before we kill everything


  public KillAllThread(Applet applet) {
    super("killAllThread");
    this.applet = applet;
  }


  public void run() {
    try {
      sleep(INTERVAL);
    } catch (InterruptedException e) {
    }
    applet.destroy();
  }


}
