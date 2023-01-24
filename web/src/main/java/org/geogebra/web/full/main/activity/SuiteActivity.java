package org.geogebra.web.full.main.activity;

import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.settings.config.AppConfigCas;
import org.geogebra.common.main.settings.config.AppConfigGeometry;
import org.geogebra.common.main.settings.config.AppConfigGraphing;
import org.geogebra.common.main.settings.config.AppConfigGraphing3D;
import org.geogebra.common.main.settings.config.AppConfigProbability;
import org.geogebra.common.main.settings.config.AppConfigUnrestrictedGraphing;
import org.geogebra.web.full.gui.view.algebra.AlgebraViewW;
import org.geogebra.web.full.gui.view.algebra.MenuItemCollection;
import org.geogebra.web.full.gui.view.algebra.contextmenu.AlgebraMenuItemCollection3D;
import org.geogebra.web.full.gui.view.algebra.contextmenu.AlgebraMenuItemCollectionCAS;
import org.geogebra.web.html5.main.AppW;

/**
 * Activity class for the GeoGebra Suite app
 */
public class SuiteActivity extends BaseActivity {

	/**
	 * New Suite activity
	 */
	public SuiteActivity(String subAppCode, boolean casDisabled) {
		super(getAppConfig(subAppCode, casDisabled));
	}

	@Override
	public void start(AppW app) {
		super.start(app);
		if (app.getConfig() != null
				&& app.getConfig().getSubAppCode() != null) {
			switch (app.getConfig().getSubAppCode()) {
			case GeoGebraConstants.CAS_APPCODE:
				app.getKernel().getGeoGebraCAS().initCurrentCAS();
				break;
			default:
				break;
			}
		}
	}

	private static AppConfig getAppConfig(String subAppCode, boolean casDisabled) {
		switch (subAppCode) {
		default:
		case GeoGebraConstants.GRAPHING_APPCODE:
			if (casDisabled) {
				return new AppConfigGraphing(GeoGebraConstants.SUITE_APPCODE);
			} else {
				return new AppConfigUnrestrictedGraphing(GeoGebraConstants.SUITE_APPCODE);
			}
		case GeoGebraConstants.GEOMETRY_APPCODE:
			return new AppConfigGeometry(GeoGebraConstants.SUITE_APPCODE);
		case GeoGebraConstants.CAS_APPCODE:
			return new AppConfigCas(GeoGebraConstants.SUITE_APPCODE);
		case GeoGebraConstants.G3D_APPCODE:
			return new AppConfigGraphing3D(GeoGebraConstants.SUITE_APPCODE);
		case GeoGebraConstants.PROBABILITY_APPCODE:
			return new AppConfigProbability(GeoGebraConstants.SUITE_APPCODE);
		}
	}

	@Override
	public MenuItemCollection<GeoElement> getAVMenuItems(AlgebraViewW view) {
		String subAppCode = getConfig().getSubAppCode();
		assert subAppCode != null;
		switch (subAppCode) {
		case GeoGebraConstants.CAS_APPCODE:
			return new AlgebraMenuItemCollectionCAS(view);
		case GeoGebraConstants.G3D_APPCODE:
			return new AlgebraMenuItemCollection3D(view);
		default:
			return super.getAVMenuItems(view);
		}
	}
}
