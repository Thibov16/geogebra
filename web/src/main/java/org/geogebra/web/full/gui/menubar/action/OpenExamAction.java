package org.geogebra.web.full.gui.menubar.action;

import org.geogebra.web.full.gui.menubar.MenuAction;
import org.geogebra.web.full.main.AppWFull;

public class OpenExamAction implements MenuAction<Void> {
	@Override
	public boolean isAvailable(Void item) {
		return true;
	}

	@Override
	public void execute(Void item, AppWFull app) {
		app.openSearchInExamMode();
	}
}
