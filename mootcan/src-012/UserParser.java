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

import java.io.*;

/**
 * reads lines from an InputStream, and passes them on to an OutputStream. in
 * addition we will search for special messages from the user to the client.
 *
 * @author Sindre S¯rensen
 */
public class UserParser extends Thread {

  private DataInputStream in;
  private DataOutputStream out;
  private DataOutputStream userOut;
	private boolean localecho;


	UserParser(InputStream inputStream, OutputStream outputStream, OutputStream
			userOutputStream, boolean localecho) {

    //    this.inputStream = inputStream;
    //    this.outputStream = outputStream;
    in = new DataInputStream(new BufferedInputStream(inputStream));
    out = new DataOutputStream(new BufferedOutputStream(outputStream));
    userOut = new DataOutputStream(new BufferedOutputStream(userOutputStream));
		this.localecho = localecho;
  }


  public void run() {
    if ((in != null) && (out != null)) {
      try {
        String input;
        while ((input = in.readLine()) != null) {
					input = checkLocalEcho(input);
          out.writeBytes(input + "\n");
          // System.out.println("UserParser wrote " + input + " to Stream");
          out.flush();
        }
      } catch (IOException e) {
        System.err.println(e);
        this.stop();
      }
    }
  }
	
	
  private String checkLocalEcho(String s) {
    if (localecho) {
			try {
				userOut.writeBytes(s + "\n");
				userOut.flush();
			}
			catch (Exception e) {
			  System.err.println(e);
			}
    }
    return s;
  }



  /*
     * New method for autoconnecting a player after enCore Xpress web
      * authentication.  There are probably better ways of doing this, and later we
      * should change it to allow both normal and autoconnections. But for now this
      * will serve it's purpose.
      * Jan Rune.
      */

  public void autoConnect(String login) {
    try {
      out.writeBytes(login + "\r\n");
      out.flush();
      // this.stop();
    } catch (IOException e) {
      // this.stop();
    }
  }


}
