package ikube.gui.panel.wizard;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class EmailWizardStep implements WizardStep {

	@Override
	public String getCaption() {
		return "Email index wizard";
	}

	@Override
	public Component getContent() {
		return new Label("Email wizard content");
	}

	@Override
	public boolean onAdvance() {
		return true;
	}

	@Override
	public boolean onBack() {
		return true;
	}

}
