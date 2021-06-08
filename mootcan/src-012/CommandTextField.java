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
//import java.awt.event.*; // java-1.1
import java.util.*;
import java.io.*;

/**
 * a textfield to enter commands has support for history.  sends the
 * 'commands' into a pipe. for now it only sends strings, but it can probably
 * be expanded to send other types.
 *
 * it may seem like overkill to add a new panel just for the TextArea here. the
 * reason I did this was to make it possible to try to add a java-1.1 TextArea
 * which is superior to the java-1.0 one. (we are allowed to construct it
 * without scrollbars). if the java-1.1 TextArea is not found, we fall back to
 * the java-1.0 one.
 *
 * @author Sindre S¯rensen
 */

//public class CommandTextField extends TextArea implements KeyListener { // java-1.1
public class CommandTextField extends Panel { // java-1.0


  private int histSize;
  private int caretPosition;
	private UserOutputArea outputArea;
  HistoryList history;
  String issuedCommand;
	boolean PGDNonKeyDown;

  // netscape 4.5 / mac needs special treatment:
  boolean ancientAWT;

  PipedOutputStream outputStream;
  PipedInputStream inputStream;
  DataOutputStream out;
  TextComponent commandArea;
	Vector modes;
		
	Mode currentMode;

  CommandTextField(Font font, int histSize, Vector modes) {
    // we'll try to get rid of superfluous scrollbars, and making the TextArea wrap by using java-1.1 construction:
		this.modes = modes;
    this.outputArea = outputArea;
		this.currentMode = (Mode) modes.firstElement();
    try {
      commandArea = new TextArea("", 3, 20, TextArea.SCROLLBARS_NONE);
      // java-1.1
    }
    // fallback to java-1.0; we might as well use a TextField instead of a
    // TextArea with ugly scrollbars
    catch (NoSuchMethodError e) {
      //commandArea = new TextField(""); // java-1.0
      commandArea = new TextArea("", 3, 20); // java-1.0
      ancientAWT = true;
    }

    this.histSize = histSize;
    outputStream = new PipedOutputStream();

    setLayout(new BorderLayout());

    commandArea.setFont(font);
    add("Center", commandArea);
    show();
    try {
      inputStream = new PipedInputStream(outputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }

    out = new DataOutputStream(outputStream);

    history = new HistoryList("", histSize);
    //addKeyListener(this);  // java-1.1
    //setLocale(Locale.GERMAN);
    //System.out.println(getLocale());
    //System.out.println(Locale.getDefault());
    //System.out.println(getLocale());
  }


  public void requestFocus() {
    if (ancientAWT)
      commandArea.select(commandArea.getText().length(),
                         commandArea.getText().length());
		else 
      commandArea.requestFocus();
  }

	
	/*
	 * not in use: meant as a workaround for focus-problems with ancientAWTs but
	 * this doesn't work either
	 *
   * public void rememberCaretPosition() {
   *   caretPosition = commandArea.getCaretPosition();
   * }
	 *
	 *
   * public void restoreCaretPosition() {
   *  commandArea.setCaretPosition(caretPosition);
	 * }
	 */

	 
	public PipedInputStream getInputStream() {
		return inputStream;
 	}


  private void sendIssuedCommand(String s) {
    //issuedCommand=str;
    // System.out.println("You wrote " + str);
    try {
      out.writeBytes(s + "\n");
      out.flush();

			
      // System.out.println("CommandTextField wrote " + str + " to Stream");
    } catch (IOException e) {
      e.printStackTrace();
    }
		
	}
  


  public String getIssuedCommand() {
    return issuedCommand;
  }


  public void clean() {
    commandArea.setText(currentMode.getMooCommand());
		try {
			commandArea.setCaretPosition(commandArea.getText().length());
    } catch (NoSuchMethodError e) {
		}
  }


  private void cycleUp() {
    history.replaceCurrentItem(commandArea.getText());
    commandArea.setText(history.getPreviousItem().toString());

    // another jdk1.1-only feature:
    try {
      commandArea.setCaretPosition(commandArea.getText().length());
    } catch (NoSuchMethodError e) {
    }
  }


  private void cycleDown() {
    history.replaceCurrentItem(commandArea.getText());
    commandArea.setText(history.getNextItem().toString());

    // another jdk1.1-only feature:
    try {
      commandArea.setCaretPosition(commandArea.getText().length());
    } catch (NoSuchMethodError e) {
    }
  }


  private void issueCommand() {
		String command = commandArea.getText();
		if (command.trim().equals(currentMode.getMooCommand().trim()) &&
		(currentMode.getMooCommand() != "")) {
			return;
		}
    history.insertItem(command);
    sendIssuedCommand(command);
    clean();
  }


	/* 
	 *keyUp is used for explorer / windows / page up and down
	 */
	public boolean keyUp(Event event, int keycode) {
	  if (! PGDNonKeyDown) {
			switch (keycode) {
				case Event.PGUP:
					cycleUp();
					return true;
				case Event.PGDN:
					cycleDown();
					return true;
			}
		}
    return false;
  }

  public boolean keyDown(Event event, int keycode) {
    switch (keycode) {
      case 10: // it seems that there is no static for the enter-key in java-1.0. 10 should do?
        issueCommand();
        return true;
      case Event.PGUP:
        cycleUp();
				PGDNonKeyDown = true;
        return true;
      case Event.PGDN:
        cycleDown();
				PGDNonKeyDown = true;
        return true;
      case Event.UP:
        if (event.modifiers == Event.CTRL_MASK) {
          cycleUp();
          return true;
        }
      case Event.DOWN:
        if (event.modifiers == Event.CTRL_MASK) {
          cycleDown();
          return true;
        }
    }
    return false;
  }


	public void setMode(Mode mode) {
		this.currentMode = mode;
		this.requestFocus();
		this.clean();
	}


	public void setMode(String smode) {

		Mode mode = (Mode) modes.firstElement();
		for (Enumeration e = modes.elements(); e.hasMoreElements(); ) {
			Mode thisMode = (Mode) e.nextElement();
			if ( thisMode.getCaption().equals(smode) ) {
				mode = thisMode;
				break;
			}
		}
		setMode(mode);
	}


	public Mode getMode() {
		return this.currentMode;
	}

  /* the rest is all java-1.1 code. maybe some other time...


       public void keyTyped(KeyEvent e) {
       }


       public void keyPressed(KeyEvent e) {
         // System.out.println(e);
         switch (e.getKeyCode()) {
           case KeyEvent.VK_ENTER:
     	history.insertItem(getText());
     	sendIssuedCommand(getText());
     	break;
           case KeyEvent.VK_PAGE_UP:
     	setText(history.getPreviousItem().toString());
     	break;
           case KeyEvent.VK_PAGE_DOWN:
     	setText(history.getNextItem().toString());
         }
       }


       public void keyReleased(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_ENTER) clean();
       }
     */

  /*
    public boolean keyUp(Event event, int keycode) {
      if (keycode == 10) {
        clean();
        return true;
      }
      return false;
    }*/

}
