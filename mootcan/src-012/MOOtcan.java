/*
 *  The MOOtcan MOO-client
 *  Copyright (C) 1999 Sindre S¯rensen
 *  Copyright (C) 2001 Jan Rune Holmevik
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
import java.awt.*;
import java.util.*;
import java.net.*;
import java.io.*;


/**
 * a simple MOO-client.
 *
 * it is currently only usable for MOO-connections as it sends and read lines,
 * not characters, through and from the socket. it may be modified to support
 * character-oriented connections.  this class glues together a GUI (currently
 * only a panel inside a frame)  and a network-connection.
 *
 * it supports processing of the stream from the MOO, so that things like
 * Surf'n Turf's browser interaction and MacMOOse's Object Browser can be
 * implemented.
 *
 * two pipes are set up (marked with ->):
 *   (the users input) -> (the parser) - (the network socket)
 *   (the MOO output) -> (the moo parser) - (the users terminal)
 *
 * maybe it will be possible to send objects through the MOO-server, maybe
 * storing them in MOO-objects, and rebuilding them at another client !? this
 * could be usable for exhcanging objects like texts / pictures / sounds, you
 * name it.
 *
 * for security, the applet and its threads are killed if the browser leaves *
 * the applet-page for more than 10 seconds. the way we do this, is to start a
 * thread that is waiting for the user to get back. there is a link to a
 * description of this method in @see KillAllThread.java. it may be fascist, but
 * browsers may be fascist too, and a user may forget that she is connected.
 *
 * @author Sindre S¯rensen
 */

public class MOOtcan extends Applet {

  final static String version = "0.1.20";
  String hostname = new String("");
  int port = 7777;
  String login = new String("");
  String MOOname = new String("");
  String locale = new String("");
  // String enCoreIntegration = "";
  String font = new String("");
  int fontsize = 0;
	boolean localecho;
	String disconnectMessage = new String("");
	String reconnectMessage = new String("");
	String sayPrefix = new String("");

  Socket socket;

  OutputStream socketOutput, userOutput;
  InputStream socketInput, userInput;
  DataOutputStream tmpUserStream;

  String urlstring;
  // boolean enCore;
  MOOtcanPanel MOOtcanPanel;
  MooParser mooParser;
  UserParser userParser;
  KillAllThread killAllThread;


  public void init() {

    setLayout(new BorderLayout());


    // parameters from the html applet tag:


    try {
      hostname = getParameter("hostname");
    } catch (Exception e) {
      hostname = "";
    }


    try {
      port = Integer.parseInt(getParameter("port"));
    } catch (Exception e) {
      hostname = "";
    }


    try {
      MOOname = getParameter("MOOname");
    } catch (Exception e) {
      MOOname = "";
    }


    try {
      locale = getParameter("locale");
    } catch (Exception e) {
      locale = "";
    }


    try {
      login = getParameter("login");
    } catch (Exception e) {
      login = "";
    }


    try {
      font = getParameter("font");
    } catch (Exception e) {
      font = "";
    }


    try {
      fontsize = Integer.parseInt(getParameter("fontsize"));
    } catch (Exception e) {
      fontsize = 0;
    }

    try {
      String s = getParameter("localecho");
			if (s.toLowerCase().indexOf("true") > -1) {
			  localecho = true;
			}
    } catch (Exception e) {
			System.out.println("problem parsing APPLET-parameter, we'll try to go on");
      localecho = false;
    }


    try {
      disconnectMessage = getParameter("disconnect_message");
		}
    catch (Exception e) {
			System.out.println("problem parsing APPLET-parameter, we'll try to go on");
    }


    try {
      reconnectMessage = getParameter("reconnect_message");
		}
    catch (Exception e) {
			System.out.println("problem parsing APPLET-parameter, we'll try to go on");
    }
		
		
    try {
      sayPrefix = getParameter("sayprefix");
		}
    catch (Exception e) {
			System.out.println("problem parsing APPLET-parameter sayprefix, we'll try to go on");
    }
		/* try {
          * enCoreIntegration = getParameter("enCoreIntegration");
        * }
          * catch (Exception e) {
        * enCoreIntegration = "";
        *}
        */

    // setting the parameters that should always be set:
    if ((hostname.length() == 0) || (hostname == null)) 
      hostname = getCodeBase().getHost();
    if (port == 0)
      port = 7777;
    if ((MOOname == "") || (MOOname == null))
      MOOname = "unnamed";
    if ((font == "") || (font == null))
      font = "Courier";
    if (fontsize == 0)
      fontsize = 12;
    // if (enCoreIntegration == "yes") enCore = true;
		
		if ((disconnectMessage == "") || (disconnectMessage == null)) {
			disconnectMessage = "\nThe connection to the MOO was closed.\n" +
			"Please press shift+reload on your browser if you would like to try reconnecting.\n";
		}

		if ((reconnectMessage == "") || (reconnectMessage == null)) {
			reconnectMessage = "Please press shift+reload on your browser if you'd like to try again";
		}

		if (sayPrefix == null) {
			sayPrefix	= "";
		}

		Font ourFont = new Font(font, Font.PLAIN, fontsize);

    MOOtcanPanel = new MOOtcanPanel(ourFont);
    add("Center", MOOtcanPanel);
    MOOtcanPanel.show(); // jdk-1.0
    userOutput = MOOtcanPanel.getOutputStream();
    userInput = MOOtcanPanel.getInputStream();
    tmpUserStream = new DataOutputStream(
                      new BufferedOutputStream(userOutput));

    // connecting:
    try {
      tmpUserStream.writeBytes("MOOtcan version " + version +
                               "\n" + "Copyright (C) 1999-2001 Sindre S\u00f8rensen and \n" +
			       "Jan Rune Holmevik. All Rights Reserved.\n\n" +
                               "MOOtcan comes with ABSOLUTELY NO WARRANTY \n" +
                               "This is free software, and you are " +
                               "welcome to redistribute it " +
                               "under certain conditions. " +
                               "(See the file COPYING in the source-code distribution, " +
		               "or the GNU GENERAL PUBLIC LICENSE Version 2, June 1991, at " +
			       "http://www.gnu.org/copyleft/gpl.html)\n\n");

      tmpUserStream.writeBytes("Trying to connect to\n " +
                               hostname + " on port " + port + "\n");
      tmpUserStream.flush();
      socket = new Socket(hostname, port);
      socketOutput = socket.getOutputStream();
      socketInput = socket.getInputStream();
    } catch (Exception e) {
      try {
        tmpUserStream.writeBytes(
						"\nSorry. Couldn't connect. Details: " + "\n" + e + "\n" + "\n" +
						"This may be due to a proxy at your site limiting access to " +
						"outside servers, or to a misconfiguration of the remote system.\n"
					);
					e.printStackTrace();
					tmpUserStream.flush();
					
      } catch (IOException userOutputProblem) {
        userOutputProblem.printStackTrace();
      }
      destroy();
    }


  }

  public void start() {
    /*
        * if we were in the middle of kill thread (caused by resizing)
        * stop the killthread and start (i.e. resume) our normal threads.
        * otherwise just start as normal
        */
    if (killAllThread != null) {
      killAllThread.stop();
      System.out.println ("KILLTHREAD STOPPED");
      killAllThread = null;
    }


    // Now start running normal threads
    if (mooParser == null) {
      mooParser =
        new MooParser(socketInput, userOutput, socket, this, sayPrefix);
    } else {
      mooParser.resume();
    }


    if (userParser == null) {
			userParser = new UserParser(userInput, socketOutput, userOutput, localecho);
      // System.out.println(enCore);
			
      if ((login != "") && (login != null)) {
        // System.out.println(login);
        userParser.autoConnect(login);
        mooParser.swallowLogin();
      }
			
      userParser.start();
      mooParser.start();
			
    } else {
      userParser.resume();
    }


  }


  public void stop() {
    /*
        * I think that it may be necessary to kill the connection when the browser
        * leaves the applet. Even if the browser keeps the applet going and keeps
        * the socket open, the user might forget that she is connected.
        */

    // First suspend our normal threads
    if ((mooParser != null) || (userParser != null)) {
      if (mooParser.isAlive()) {
        mooParser.suspend();
      }
			if (userParser.isAlive()) {
				userParser.suspend();
			}

			/* Fire up a killthread, which will kill the normal threads
		 	 * in 10 seconds unless start is called
			 */
			if (killAllThread != null)
				System.out.println ("ERROR - killAllThread shouldn't exist");
			else {
				killAllThread = new KillAllThread(this);
				killAllThread.start();
			}
		}
  }


  public void destroy() {
    if (mooParser != null) {

			// telling the user about destruction:
			try {
				DataOutputStream emergencyUserStream =
					new DataOutputStream(userOutput);
        emergencyUserStream.writeBytes(disconnectMessage);
				emergencyUserStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
			
			// telling the MOO about destruction:
      try {
				DataOutputStream emergencyMooStream =
					new DataOutputStream(socketOutput);
        emergencyMooStream.writeBytes("@quit\n");
				emergencyMooStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
			
      mooParser.stop();
      mooParser = null;
    }
    if (userParser != null) {
      userParser.stop();
      userParser = null;
    }
    if (killAllThread != null) {
      killAllThread.stop();
      killAllThread = null;
    }
  }


}
