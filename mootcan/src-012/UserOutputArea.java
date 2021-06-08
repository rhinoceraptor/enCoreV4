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


import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * gives the user a textual representation of a stream.
 * the TextArea scrolls pretty slow on the implementations of jvm that I've
 * seen. the problem is solved by not updating the TextArea until we've read
 * all lines currently pending in the stream.
 *
 * it may seem like overkill to add a new panel just for the TextArea here. the
 * reason I did this was to make it possible to try to add a java-1.1 TextArea
 * which is superior to the java-1.0 one. (we are allowed to construct it
 * without scrollbars). if the java-1.1 TextArea is not found, we fall back to
 * the java-1.0 one.
 *
 * @author Sindre S¯rensen
 */
public class UserOutputArea extends Panel implements Runnable {
  private TextArea outputArea;
  private PipedOutputStream outputStream;
  private PipedInputStream inputStream;
  //  private BufferedReader in; //jdk-1.1
  private DataInputStream in; //jdk-1.0
  private BufferedInputStream buffIn;
  private Thread userOutputThread = new Thread(this, "UserOutput");
  private CommandTextField inputArea;
	/*	
	 *	on Netscape-4.5/Windows, the TextArea seems to hold only 24655 chars.
	 *			anyway; stress testing Netscape-4.5/Windows shows that it can't even
	 *			handle 20000, we are setting it to 10000 (maxMax=12000) for now.
	 */
  int maxHistoryLength = 10000;
  int maxMaxHistoryLength = maxHistoryLength + 2000;
  // Vector historyBuffer = new Vector(maxHistoryLength);
  // Vector inputBuffer = new Vector();
  private int numberOfRows;
  // String screenBuffer = new String("");
  String history = new String("");
  String buffer = new String("");
	private boolean ancientAWT;

  /**
   * @param font the font of the scrolling text
   *
   * the next parameter is necessary because we use select() to scroll down,
   * and that also makes us catch the keyboard focus of the applet
   *
   * @param inputArea what should be given the focus after we've written
   * something to the outputArea
   */
  UserOutputArea (Font font, CommandTextField inputArea) {
    this.inputArea = inputArea;
    // we'll try to get rid of superfluous scrollbars, and making the TextArea wrap by using java-1.1 construction:
    try {
      //outputArea = new TextArea("\n", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY); // java-1.1
      outputArea = new TextArea("\n", 0, 0,
                                TextArea.SCROLLBARS_VERTICAL_ONLY); // java-1.1
    }
    // fallback to java-1.0:
    catch (NoSuchMethodError e) {
      outputArea = new TextArea("\n");
			ancientAWT = true;
    }

    outputArea.setFont(font);
    outputArea.setBackground(Color.white);
    /*
		 * some people find it useful that the outputarea is editable, change false to true in the next line to enable that "feature"
		 * this will probably have unexpected effects on focusing-behaviour
		 */
    outputArea.setEditable(false);

    // maxHistoryLength = outputArea.getRows();

    inputStream = new PipedInputStream();
    try {
      outputStream = new PipedOutputStream(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // System.out.println(outputArea.getPreferredSize());
    buffIn = new BufferedInputStream(inputStream);
    in = new DataInputStream(buffIn); // java-1.0
    userOutputThread.start();
    //in = new BufferedReader(buffin); // java-1.1
    setLayout(new BorderLayout());
    add("Center", outputArea);

    numberOfRows = outputArea.getRows();
    //numberOfRows = 15;
  }

	
  public OutputStream getOutputStream() {
    return outputStream;
  }


	private void scrollToBottom() {
		// System.out.println(history.length() + " " + outputArea.getText().length());
		if (ancientAWT) {	
			/*
			 * poor mans hack to scroll to the bottom of a TextArea :
			 * select the last character of the outputArea 
			 * this will lead to trouble with Netscape-4.5 for windows (length()
			 * not being correct etc.):
			 *   outputArea.select(outputArea.getText().length(),
			 *                    outputArea.getText().length());
			 * but this works (also for Netscape-4.5/Mac):
			 */
			outputArea.select(maxMaxHistoryLength, maxMaxHistoryLength);
											 // java-1.0
			
			inputArea.requestFocus();
		}
		/*
		 * this is necessary for unix-versions of Netscape (and maybe some others):
		 * (the argument will be bumped down to what java.awt.TextComponent thinks is the size of the text)
		 */
		else {
			try {
				outputArea.setCaretPosition(maxMaxHistoryLength);
			}
			catch(IllegalComponentStateException icse) {
				// this seems to happen if the TextAreas's peer hasn't yet gotten around to creating itself
			}
		}
	}
	

  /** put the tail of the history onto the screen */
  private synchronized void refreshScreen() {
    history += buffer;

    if (history.length() >= maxMaxHistoryLength) {

			// shorten history to maxHistoryLength:
      history = history.substring(history.length() -
                                  maxHistoryLength);
			// make sure that the topmost line isn't split:
			history = history.substring(history.indexOf("\n") + 1, history.length());
			
			// actually replace the text:
			outputArea.replaceText(history, 0,
                             outputArea.getText().length());
    } else {
      outputArea.appendText(buffer);
    }

    buffer = "";
		scrollToBottom();
  }


	// public void append(String s) {
  //   buffer += s;
  //   refreshScreen();
	// }


  public void run() {
		
		// to properly update things:
		buffer = outputArea.getText();
		refreshScreen();
		
		// System.out.println(maxHistoryLength + " " + maxMaxHistoryLength);
		
		
    String line;
    try {
      while ((line = in.readLine()) != null) {

        buffer += "\n" + line;
        /* we've just read a line, we could wait here for a moment to see if
            * there's more lines coming at us. But it seems not to be necessary.
            * the stream is already buffered in both ends...
            *
            *	try {
            *         userOutputThread.sleep(50);
            *       }
            *       catch (InterruptedException ie) {
            *         ie.printStackTrace();
            *       }
            */

        /* we are checking the buffer here instead to check for pending lines.
         * the reason for all this fuzz is to please java.awt.TextArea which is
         * quite resource-hungry, especially on some jvm-implementations
         */

        while (buffIn.available() > 0) {
          line = in.readLine();
          buffer += "\n" + line;
        }
        refreshScreen();
      }
    } catch (IOException e) {
      // no big deal, let's print a StackTrace
      e.printStackTrace();
    }
  }

}
