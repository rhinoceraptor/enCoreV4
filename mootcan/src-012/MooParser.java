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


import java.io.*;
import java.net.*;
import java.applet.*;
import java.awt.*;

/**
 * reads lines from an InputStream, and passes them on to an OutputStream.
 * in addition we will search for special messages from MOO to client. (Surf'n
 * Turf - style URLs / MCP etc.
 *
 * @author Sindre S¯rensen
 *
 * Modified to allow different targets for urls from the MOO. See method surfnTurf() below for details.
 * Jan Rune Holmevik, 3/31/01
 */

public class MooParser extends Thread {

  //  private InputStream inputStream;
  //  private OutputStream outputStream;
  private DataInputStream in;
  private DataOutputStream out;
  private AppletContext context;
  private Socket socket;
  private Applet parent;
	private int urlSpread = 0;
	private static int maxUrlSpread = 32; // the maximum number of lines to look for uncompleted URLs
	private String uncompletedURL = new String("");
	private String sayPrefix = new String("");
	
  final static int bell = 7;
  final static int tab = 9;
  final static int cr = 13;
  final static int space = 32;
	
  //final static String bellString = new String(bell);
  //final static String crString = new String(bell);

  MooParser(InputStream inputStream, OutputStream outputStream,
            Socket socket, Applet parent, String sayPrefix) {
    this.parent = parent;
    this.context = parent.getAppletContext();
		this.sayPrefix = sayPrefix;
    in = new DataInputStream(new BufferedInputStream(inputStream));
    out = new DataOutputStream(new BufferedOutputStream(outputStream));
  }

  public void run() {
    if ((in != null) && (out != null)) {
      try {
        String input = new String("");
        while ((input = in.readLine()) != null) {
          // System.out.println("MooParser read " + input);

          /* The mechanism for parsing and translating and performing actions
           * upon strings from the MOO should be made more flexible. Maybe by
           * maintaining a list of the tests that are enabled, and running
           * through that list for every input-line. */

          input = userSays(input);
          input = surfnTurf(input);
          input = ansiBell(input);
          if (input != "") {
            out.writeBytes(input + "\n");
            out.flush();
            // System.out.println("MooParser wrote to stream");
          }
        }
        // out.writeBytes("\nYou are no longer connected to the MOO. Press shift+reload on your browser to try reconnecting\n\n");
        out.flush();
        parent.destroy();
      }
      catch (IOException e) {
        try {
          e.printStackTrace();
          socket.close();
        } catch (IOException closingProblem) {
          // out.writeBytes(closingProblem.toString());
          closingProblem.printStackTrace();
        }
      }
    }
  }


  private String userSays(String s) {
    if (s.startsWith("You say,")) {
			// System.out.println("sayprefix " + sayPrefix);
      s = sayPrefix + s;
    }
    return s;
  }

	
	private void showURL(String s, String target) {
		try {
			
			// remove tabs, bells and carriage returns resulting from wrapping etc:
			s = removeAll(s, tab);
			s = removeAll(s, bell);
			s = removeAll(s, space);
			s = removeAll(s, cr);
			
			URL u = new URL(s);
			
			context.showDocument(u, target);
			
		} catch (Exception e) {
			
			try {
				out.writeBytes("Error when trying to show document in browser: " +e);
			} catch (IOException userOutputProblem) {
				userOutputProblem.printStackTrace();
			}
			
		}
	}
	

  private String surfnTurf(String s) {
		// are there any uncompleted URLs (resulting from URL's spread over several lines)?:
		// modified this method so that the actual URL token is not shown to the user.
		// this method can now also determine if a url should be displayed in the main Xpress
		// window, or in a new browser window.
		// Jan Rune Holmevik, 3/31/01

  		String target = "web_frame"; // default target 
 		String last = ">."; // default url ending

		if (uncompletedURL.length() > 0) {
			urlSpread += 1;
			uncompletedURL += s;

			if (urlSpread == maxUrlSpread) {
				String errorMessage = "MOOtcan found a URL that was not completed within " + maxUrlSpread + "lines: " + uncompletedURL;
				urlSpread = 0;
				uncompletedURL = "";
				return s;
				// return errorMessage;
			}
			
			if (s.indexOf(">.") > -1) {
				if (s.indexOf("_blank>.") > -1) {
					target = "_BLANK";
					last = "_blank>.";						
				}

				String urlstring = uncompletedURL.substring(0, uncompletedURL.lastIndexOf(last));

				showURL(urlstring, target);
				uncompletedURL = "";
				// return what was after the URL:
				return s.substring(s.lastIndexOf(last) + last.length(), s.length());
			}
			// return nothing if we are in the middle of a URL:
			//return "";
			//return s;
		}
		else {
			if (s.indexOf("<http") > -1) {
				uncompletedURL = "";
				// is the URL completed on the same line?:
				if (s.indexOf(">.") > -1) {
					if (s.indexOf("_blank>.") > -1) {
						target = "_BLANK";
						last = "_blank>.";						
					}

					String urlstring = s.substring(s.indexOf("<http") + 1, s.lastIndexOf(last));
					showURL(urlstring, target);
					// return what was in front of and after the URL:
					return s.substring(0, s.indexOf("<http")) +
				        s.substring(s.lastIndexOf(last) + last.length(), s.length());
					//return s;
				}
				// the URL was not completed on this line:
				else {
					uncompletedURL = s.substring(s.indexOf("<http") + 1, s.length());
					urlSpread = 1;
					//return what was in front the URL:
					return s.substring(0, s.indexOf("<http"));

					//return s;
				}
			}
		}
		return s;
	}


	/** remove all occurences of ch from s */
	/** @param s the String to be cleaned */
	/** @param ch what to remove from the String */
	private String removeAll(String s, int ch) {
	  while (s.indexOf(ch) > -1) {
      s = s.substring(0, s.indexOf(ch)) +
            s.substring(s.indexOf(ch) + 1, s.length());
		}
		return s;
	}


  private String ansiBell(String s) {
		 
    if (s.indexOf(bell) > -1) {

      // this would maybe make a beep on a normal console?
      //System.out.print("\007");
      //System.out.flush();

      try {
        java.awt.Toolkit.getDefaultToolkit().beep();
      } catch (NoSuchMethodError e) {
      }

      return s.substring(0, s.indexOf(bell)) +
              s.substring(s.indexOf(bell) + 1, s.length());

    }
    return s;
  }


  public void swallowLogin() {
    String input = "";
    try {
      // we are waiting for one of two magic strings to appear from the server:
      while (input.lastIndexOf("***") <= 0) {
        input = in.readLine();
      }
      out.writeBytes(input + '\n');
      out.flush();
    } catch (Exception e) {
      System.out.println("swallowException");
    }

  }
	
	
	/* for checking numeric values of chars: 
	 * char[] charray = s.toCharArray();
	 * int t;
	 * System.out.println();
	 * for (t = 0; t < s.length(); t++) {
	 *   int chint = Character.getNumericValue(charray[t]);
	 *   System.out.print(chint + ",");
	 * }
	 */
	
	
}
