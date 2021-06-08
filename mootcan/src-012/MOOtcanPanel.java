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
 * puts together components for a MOOclient usable in a frame
 *
 * @author Sindre S¯rensen
 */
class MOOtcanPanel extends Panel {

  UserOutputArea outputArea;
  CommandTextField inputArea;
	ControlPanel controlPanel;
	Panel commandPanel;
	
  // Font font = new Font("Courier", Font.PLAIN, 10);



	Vector modes = new Vector(3);

  MOOtcanPanel(Font f) {
		modes.addElement(new Mode("normal", ""));
		modes.addElement(new Mode("say", "say "));
		modes.addElement(new Mode("emote", "emote "));
    inputArea = new CommandTextField(f, 100, modes);
    outputArea = new UserOutputArea(f, inputArea);
    setLayout(new BorderLayout());
		//try {
      int netscapeTest = TextArea.SCROLLBARS_NONE;
			/*
			 * if netscape on mac had TextArea.SCROLLBARS_NONE, it will maybe do
			 * this to: 
			 */
			controlPanel = new ControlPanel(inputArea, modes);
			commandPanel = new Panel();
			commandPanel.setLayout(new BorderLayout());
			commandPanel.add("Center", inputArea);
			commandPanel.add("East", controlPanel);
			add("South", commandPanel);
		//}
		//catch (Exception e) {
		//		add("South", inputArea);
		//	}
    add("Center", outputArea);
    // outputArea.show();
    // inputArea.show();
    inputArea.requestFocus();
  }


  public InputStream getInputStream() {
    return inputArea.getInputStream();
  }


  public OutputStream getOutputStream() {
    return outputArea.getOutputStream();
  }


}
