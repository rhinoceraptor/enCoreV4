import java.awt.*;
import java.util.*;

public class ControlPanel extends Panel {


	CheckboxGroup cbg;
	Checkbox nc;
	Checkbox tc;
	Checkbox ec;
	CommandTextField ctf;
	Vector modes;


	ControlPanel(CommandTextField ctf, Vector modes) {
		this.modes = modes;

		this.ctf = ctf;
		this.setLayout(new GridLayout(3, 1));
		cbg = new CheckboxGroup();
		System.out.println("addad checkbox");

		boolean enableCheckbox = true;
		for (Enumeration e = modes.elements(); e.hasMoreElements(); ) {
				
			add(new Checkbox(((Mode)e.nextElement()).getCaption(), cbg, enableCheckbox));

			// enable only the first checkbox:
			enableCheckbox = false;

		}

		// lockButton.setSize(inputModeChoice.getSize());
	}


	/* this is written in Netscape and Mac compability mode... */
	public boolean action(Event evt, Object what) {
		if (evt.target instanceof Checkbox) {
				
			ctf.setMode(((Checkbox)evt.target).getLabel());
			return true;
		}
		ctf.requestFocus();
    return false;
  }


/* for > java-1.0.2, doesn't work on Netscape-4.6 / mac
	public void itemStateChanged(ItemEvent ie) {
		System.out.println(ie);
		if (ie.getItem().equals("Normal")) {
			ctf.setMode(ctf.NORMALMODE);
			//this.setBackground(ctf.getBackground());
		}
		if (ie.getItem().equals("Talk")) {
			ctf.setMode(ctf.TALKMODE);
			//this.setBackground(Color.red);
		}
		if (ie.getItem().equals("Emote")) {
			ctf.setMode(ctf.EMOTEMODE);
			//this.setBackground(Color.orange);
		}
			
	}

 */

}
