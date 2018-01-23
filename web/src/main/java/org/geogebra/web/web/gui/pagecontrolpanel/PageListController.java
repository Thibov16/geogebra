package org.geogebra.web.web.gui.pagecontrolpanel;

import java.util.ArrayList;

import org.geogebra.common.main.Feature;
import org.geogebra.common.util.debug.Log;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.html5.main.GgbFile;
import org.geogebra.web.html5.main.PageListControllerInterface;
import org.geogebra.web.web.gui.applet.GeoGebraFrameBoth;

/**
 * controller for page actions, such as delete or add slide
 * 
 * @author csilla
 *
 */
public class PageListController implements PageListControllerInterface {
	/**
	 * application {@link AppW}
	 */
	protected AppW app;
	/**
	 * list of slides (pages)
	 */
	protected ArrayList<PagePreviewCard> slides;

	/**
	 * @param app
	 *            {@link AppW}
	 */
	public PageListController(AppW app) {
		this.app = app;
		slides = new ArrayList<>();
	}

	/**
	 * @return list of slides
	 */
	public ArrayList<PagePreviewCard> getSlides() {
		return slides != null ? slides : new ArrayList<PagePreviewCard>();
	}

	/**
	 * loads the slide with index i from the list
	 * 
	 * @param curSelCard
	 *            currently selected card
	 * 
	 * @param i
	 *            index of the slide to load
	 * @param newPage
	 *            true if slide is new slide
	 */
	public void loadSlide(PagePreviewCard curSelCard, int i, boolean newPage) {
		if (slides == null) {
			return;
		}
		// save file status of currently selected card
		curSelCard.setFile(app.getGgbApi().createArchiveContent(false));
		try {
			if (newPage) {
				// new file
				app.fileNew();
			} else {
				// load last status of file
				app.resetPerspectiveParam();
				Log.debug("[PCP] loading page " + i);
				app.loadGgbFile(slides.get(i).getFile());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Duplicates slide
	 * 
	 * @param sourceCard
	 *            to duplicate.
	 * @return the new, duplicated card.
	 */
	public PagePreviewCard duplicateSlide(PagePreviewCard sourceCard) {
		int dupIdx = sourceCard.getPageIndex() + 1;
		PagePreviewCard dup = new PagePreviewCard(app, dupIdx,
				sourceCard.getFile().duplicate());
		boolean lastSlide = (dupIdx == slides.size());
		slides.add(dupIdx, dup);
		if (!lastSlide) {
			updatePageIndexes(dupIdx);
		}
		return dup;

	}

	/**
	 * adds a new slide to the list
	 * 
	 * @return index of the added slide
	 */
	public PagePreviewCard addSlide() {
		if (slides == null) {
			slides = new ArrayList<>();
		}
		PagePreviewCard previewCard = new PagePreviewCard(
				app, slides.size(), new GgbFile());
		slides.add(previewCard);
		return previewCard;
	}

	/**
	 * removes the slide with given index from the list
	 * 
	 * @param index
	 *            of the slide to be removed
	 */
	public void removeSlide(int index) {
		if (slides == null || index >= slides.size()) {
			return;
		}
		slides.remove(index);
	}

	/**
	 * gets the number of slides in the list
	 * 
	 * @return number of slides
	 */
	public int getSlidesAmount() {
		return slides.size();
	}

	@Override
	public void resetPageControl() {
		if (!app.has(Feature.MOW_MULTI_PAGE)) {
			return;
		}
		// clear preview card list
		slides = new ArrayList<>();
		// clear gui
		((GeoGebraFrameBoth) app.getAppletFrame()).getPageControlPanel()
				.reset();
	}
	
	private void updatePageIndexes(int masterIdx) {
		for (int i = masterIdx; i < slides.size(); i++) {
			slides.get(i).setPageIndex(i);
		}
	}
}
