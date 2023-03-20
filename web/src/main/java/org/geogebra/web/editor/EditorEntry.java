package org.geogebra.web.editor;

import org.geogebra.web.html5.bridge.RenderGgbElement;
import org.geogebra.web.resources.StyleInjector;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.himamis.retex.editor.web.JlmEditorLib;
import com.himamis.retex.renderer.web.CreateLibrary;
import com.himamis.retex.renderer.web.JlmApi;
import com.himamis.retex.renderer.web.font.opentype.Opentype;

import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

public class EditorEntry implements EntryPoint {

 	@Override
	public void onModuleLoad() {
		initFontAndCss();
		initJlmLibrary();
		RenderGgbElement.setRenderGGBElement(new RenderEditor());
		RenderGgbElement.renderGGBElementReady();
	}

	private void initJlmLibrary() {
		CreateLibrary.exportLibrary(new JlmApi(new JlmEditorLib()));
	}

	private void initFontAndCss() {
		String baseUrl = getBaseUrl();
		new StyleInjector(baseUrl)
				.inject("css", "editor");
		Opentype.setFontBaseUrl(baseUrl);
	}

	private String getBaseUrl() {
		elemental2.dom.Element script = DomGlobal.document
				.querySelector("[src$=\"editor.nocache.js\"]");
		String baseUrl = GWT.getModuleBaseURL();
		if (script != null && !isSuperDev()) {
			baseUrl = script.getAttribute("src");
			baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);
		}
		return baseUrl;
	}

	private boolean isSuperDev() {
		return Js.asPropertyMap(DomGlobal.window).has("__gwt_sdm");
	}
}
