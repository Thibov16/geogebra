package org.geogebra.web.html5;

import java.util.Locale;

import org.geogebra.common.util.AsyncOperation;
import org.geogebra.common.util.DoubleUtil;
import org.geogebra.common.util.StringUtil;
import org.geogebra.common.util.debug.Log;
import org.geogebra.gwtutil.NavigatorUtil;
import org.geogebra.web.html5.bridge.GeoGebraJSNativeBridge;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.Window.Navigator;

import elemental2.core.Function;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.Uint8Array;
import elemental2.dom.Blob;
import elemental2.dom.BlobPropertyBag;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.URL;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class Browser {
	public static final String ACTION_RESET_URL = "{\"action\": \"resetUrl\"}";
	private static Boolean webglSupported = null;

	/**
	 * Check if browser is Safari. Note: user agent string contains Safari also
	 * in Chrome =&gt; use vendor instead
	 *
	 * check isiOS() && isSafari() if you want just iOS browser & not webview
	 *
	 * @return true if Safari browser
	 */
	public static native boolean isSafariByVendor() /*-{
		return "Apple Computer, Inc." === $wnd.navigator.vendor;
	}-*/;

	public native static boolean externalCAS() /*-{
		return typeof $wnd.evalGeoGebraCASExternal == 'function'
				&& $wnd.evalGeoGebraCASExternal("1+1") == "2";
	}-*/;

	/**
	 *
	 * @return true if WebAssembly supported
	 */
	public static boolean webAssemblySupported() {
		return hasGlobal("WebAssembly");
	}

	public static boolean hasGlobal(String propertyName) {
		return hasProperty(DomGlobal.window, propertyName);
	}

	public static boolean hasProperty(Object base, String propertyName) {
		return base != null && Js.isTruthy(Js.asPropertyMap(base).get(propertyName));
	}

	public static boolean supportsPointerEvents() {
		return hasGlobal("PointerEvent");
	}

	private static boolean isHTTP() {
		return !"file:".equals(Location.getProtocol());
	}

	/**
	 * Check this to avoid exceptions thrown from Storage.get*StorageIfSupported
	 *
	 * @return whether session storage is supported
	 */
	public static boolean supportsSessionStorage() {
		return !NavigatorUtil.isIE() || Browser.isHTTP();
	}

	/**
	 * @param thumb
	 *            original URL
	 * @return URL using appropriate protocol (data or https)
	 */
	public static String normalizeURL(String thumb) {
		if (thumb.startsWith("data:") || (thumb.startsWith("http://")
				&& "http:".equals(Location.getProtocol()))) {
			return thumb;
		}
		String url;
		if (thumb.startsWith("http://") || thumb.startsWith("file://")) {
			url = thumb.substring("http://".length());
		} else if (thumb.startsWith("https://")) {
			url = thumb.substring("https://".length());
		} else if (thumb.startsWith("//")) {
			url = thumb.substring("//".length());
		} else {
			url = thumb;
		}

		return "https://" + url;
	}

	/**
	 * @return whether WebGL is supported
	 */
	public static boolean supportsWebGL() {
		if (webglSupported == null) {
			webglSupported = supportsWebGLNative();
		}
		return webglSupported.booleanValue();
	}

	/**
	 * For tests, circumvent the WebGL availability check
	 */
	public static void mockWebGL() {
		webglSupported = true;
	}

	/**
	 * Native check for WebGL support based on
	 * http://stackoverflow.com/questions/11871077/proper-way-to-detect-webgl-
	 * support
	 *
	 * @return whether WebGL is supported
	 */
	public static native boolean supportsWebGLNative()/*-{
		try {
			var canvas = $doc.createElement('canvas');
			var ret = !!$wnd.WebGLRenderingContext
					&& (canvas.getContext('webgl') || canvas
							.getContext('experimental-webgl'));
			return !!ret;
		} catch (e) {
			return false;
		}
	}-*/;

	/**
	 * @return whether we are running on geogebra.org
	 */
	public static boolean isGeoGebraOrg() {
		String host = Location.getHost();
		return host != null && host.contains("geogebra.org");
	}

	/**
	 * @return right now GraspableMath is only enabled on geogebra.org and
	 * development hosts
	 */
	public static boolean isGraspableMathEnabled() {
		String host = Location.getHost();
		return host != null
				&& (host.contains("geogebra.org")
					|| host.contains("localhost")
					|| host.contains("apps-builds.s3-eu-central-1.amazonaws.com"));
	}

	public native static String navigatorLanguage() /*-{
		return $wnd.navigator.language || "en";
	}-*/;

	public static native boolean isAndroidVersionLessThan(double d) /*-{
		var navString = $wnd.navigator.userAgent.toLowerCase();
		if (navString.indexOf("android") < 0) {
			return false;
		}
		if (parseFloat(navString.substring(navString.indexOf("android") + 8)) < d) {
			return true;
		}
		return false;

	}-*/;

	/**
	 * @param parent
	 *            element to be scaled
	 * @param externalScale
	 *            scale
	 * @param x
	 *            origin x-coord in %
	 * @param y
	 *            origin y-coord in %
	 */
	public static void scale(Element parent, double externalScale, int x, int y) {
		if (externalScale < 0 || parent == null) {
			return;
		}

		if (isSafariByVendor()) {
			zoom(parent, externalScale);
			return;
		}

		String transform = "scale(" + externalScale + ")";
		parent.addClassName("ggbTransform");

		if (DoubleUtil.isEqual(externalScale, 1)) {
			transform = "none";
		}
		String pos = x + "% " + y + "%";

		Style style = parent.getStyle();
		if (style != null) {
			style.setProperty("transform", transform);
			style.setProperty("transformOrigin", pos);
		}
	}

	private static void zoom(Element parent, double externalScale) {
		Style style = parent.getStyle();
		if (style == null) {
			return;
		}
		style.setProperty("transform", "none");
		int zoomPercent = (int) Math.round(externalScale * 100);
		style.setProperty("zoom", zoomPercent + "%");
	}

	/**
	 * @return whether webcam input is supported in the browser
	 */
	public static boolean supportsWebcam() {
		return DomGlobal.window.navigator.mediaDevices != null;
	}

	/**
	 * @return true if Javascript CAS is supported.
	 */
	public static boolean supportsJsCas() {
		return !Browser.isAndroidVersionLessThan(4.0);
	}

	/**
	 * @return CSS pixel ratio
	 */
	public static native double getPixelRatio() /*-{
		var testCanvas = $doc.createElement("canvas"), testCtx = testCanvas
				.getContext("2d");
		devicePixelRatio = $wnd.devicePixelRatio || 1;
		backingStorePixelRatio = testCtx.webkitBackingStorePixelRatio
				|| testCtx.mozBackingStorePixelRatio
				|| testCtx.msBackingStorePixelRatio
				|| testCtx.oBackingStorePixelRatio
				|| testCtx.backingStorePixelRatio || 1;
		return devicePixelRatio / backingStorePixelRatio;
	}-*/;

	/**
	 *
	 * Returns a base64 encoding of the specified (binary) string
	 *
	 * extra encoding needed for file from Generator: Adobe Illustrator 11.0,
	 * SVG Export Plug-In
	 *
	 * xpacket begin='\uFEFF'
	 *
	 * @param svg
	 *            A binary string (obtained for instance by the FileReader API)
	 * @return a base64 encoded string.
	 */
	public static String encodeSVG(String svg) {
		// can't use data:image/svg+xml;utf8 in IE11 / Edge
		// so encode as Base64
		return StringUtil.svgMarker + DomGlobal.btoa(
				Global.unescape(Global.encodeURIComponent(svg)));
	}

	/**
	 * Download an exported file/image
	 * @param url data URL
	 * @param title title
	 */
	public static void exportImage(String url, String title) {
		String extension;

		//global function in Chrome Kiosk App
		if (GeoGebraGlobal.getGgbExportFile() != null) {
			GeoGebraGlobal.getGgbExportFile().call(DomGlobal.window, url, title);
			return;
		}

		boolean base64encoded = true;

		if (url.startsWith(StringUtil.pngMarker)) {
			extension = "image/png";
		} else if (url.startsWith(StringUtil.svgMarker)) {
			extension = "image/svg+xml";
		} else if (url.startsWith(StringUtil.gifMarker)) {
			extension = "image/gif";
		} else if (url.startsWith(StringUtil.pdfMarker)) {
			extension = "application/pdf";
		} else if (url.startsWith(StringUtil.txtMarker)) {
			extension = "text/plain";
			base64encoded = false;
		} else if (url.startsWith(StringUtil.txtMarkerForSafari)) {
			extension = "application/octet-stream";
			base64encoded = false;
		} else if (url.startsWith(StringUtil.htmlMarker)) {
			extension = "text/html";
			base64encoded = false;
		} else {
			Log.debug("unknown extension " + url.substring(0, 30));
			return;
		}

		GeoGebraJSNativeBridge bridge = GeoGebraJSNativeBridge.get();
		if (bridge != null) {
			bridge.share(url, title, extension);
			return;
		}

		// no downloading on iOS so just open image/file in new tab
		if (NavigatorUtil.isiOS()) {
			Browser.openWindow(url);
			return;
		}

		// Chrome limits to 2Mb so use Blob
		// https://stackoverflow.com/questions/695151/data-protocol-url-size-limitations/41755526#41755526
		// https://stackoverflow.com/questions/38781968/problems-downloading-big-filemax-15-mb-on-google-chrome/38845151#38845151
		// idea from http://stackoverflow.com/questions/16245767/creating-a-blob-from-a-base64-string-in-javascript/16245768#16245768
		if (DomGlobal.navigator.userAgent.toLowerCase(Locale.US).contains("chrome")) {

			String byteCharacters = url.substring(url.indexOf(',') + 1);

			if (base64encoded) {
				byteCharacters = DomGlobal.atob(byteCharacters);
			}
			int sliceSize = 512;
			JsArray<Blob.ConstructorBlobPartsArrayUnionType> byteArrays = JsArray.of();
			for (int offset = 0; offset < byteCharacters.length(); offset += sliceSize) {
				String slice = byteCharacters.substring(offset, Math.min(byteCharacters.length(),
						offset + sliceSize));

				double[] byteNumbers = new double[slice.length()];
				for (int i = 0; i < slice.length(); i++) {
					byteNumbers[i] = slice.charAt(i);
				}

				Uint8Array byteArray = new Uint8Array(byteNumbers);
				byteArrays.push(Blob.ConstructorBlobPartsArrayUnionType.of(byteArray));
			}
			BlobPropertyBag bag = BlobPropertyBag.create();
			bag.setType(extension);
			Blob blob = new Blob(byteArrays, bag);
			downloadURL(URL.createObjectURL(blob), title);
		} else {
			downloadURL(url, title);
		}
	}

	/**
	 * @param url URL, supports both data: and blob: schemes
	 * @param title resulting filename
	 */
	public static void downloadURL(String url, String title) {
		HTMLAnchorElement a = Js.uncheckedCast(DomGlobal.document.createElement("a"));
		a.href = Global.encodeURI(url);
		a.download = title;
		a.onclick = (evt) -> {
			DomGlobal.requestAnimationFrame(ignore -> URL.revokeObjectURL(url));
			return true;
		};
		DomGlobal.setTimeout((ignore) -> {
			Function click = Js.uncheckedCast(Js.asPropertyMap(a).get("click"));
			click.call(a);
		}, 10);
	}

	/**
	 * Change URL if we are running on geogebra.org
	 *
	 * @param string
	 *            new URL
	 */
	public static void changeUrl(String string) {
		if (isAppsServer() || string.startsWith("?")) {
			nativeChangeUrl(string);
		}
	}

	private static boolean isAppsServer() {
		String host = Location.getHost();
		return host != null
				&& (host.contains("geogebra.org") || host.equals("localhost"))
				&& !Location.getPath().contains(".html");
	}

	/**
	 * Change title and OpenGraph title using a global function from app.html
	 * @param title document title
	 */
	public static void changeMetaTitle(String title) {
		Function changeTitle = GeoGebraGlobal.getChangeMetaTitle();
		if (changeTitle != null) {
			changeTitle.call(DomGlobal.window, title);
		}
	}

	private static void nativeChangeUrl(String name) {
		if (!StringUtil.empty(name)) {
			try {
				DomGlobal.history.pushState(JsPropertyMap.of(), "GeoGebra", name);
			} catch (Exception e) {
				// on dev server trying to push production URL
			}
		}
	}

	/**
	 * resets url to base: no materials or query string.
	 */
	public static void resetUrl() {
		DomGlobal.window.parent.postMessage(ACTION_RESET_URL, "*");
	}

	/**
	 * Opens GeoGebraTube material in a new window
	 *
	 * @param url
	 *            GeoGebraTube url
	 */
	public static void openWindow(String url) {
		DomGlobal.window.open(url, "_blank");
	}

	/**
	 * Returns a string based on base 64 encoded value
	 *
	 * @param base64
	 *            a base64 encoded string
	 *
	 * @return decoded string
	 */
	public static native String decodeBase64(String base64)/*-{
		return decodeURIComponent(escape($wnd.atob(base64)));
	}-*/;

	public static void removeDefaultContextMenu(Element element) {
		setAllowContextMenu(element, false);
	}

	/**
	 * Allow or diallow context menu for an element.
	 *
	 * @param element
	 *            element
	 * @param allow
	 *            whether to allow context menu
	 */
	public static native void setAllowContextMenu(Element element,
			boolean allow) /*-{
		if (element.addEventListener) {
			element.addEventListener("MSHoldVisual", function(e) {
				allow ? e.stopPropagation() : e.preventDefault();
			}, false);
			element.addEventListener('contextmenu', function(e) {
				allow ? e.stopPropagation() : e.preventDefault();
			}, false);
		}
	}-*/;

	public native static boolean isAndroid()/*-{
		var userAgent = $wnd.navigator.userAgent;
		if (userAgent) {
			return userAgent.indexOf("Android") != -1;
		}
		return false;
	}-*/;

	/**
	 * @deprecated you most likely want to use isiOS() instead
	 * @return check this is an iPad browser
	 */
	@Deprecated
	public native static boolean isIPad()/*-{
		var userAgent = $wnd.navigator.userAgent;
		if (userAgent) {
			return userAgent.indexOf("iPad") != -1;
		}
		return false;
	}-*/;

	/**
	 * it's true for android phones and iPads but false for iPhone
	 *
	 * @deprected isMobile might work better, alternatively isiOS() ||
	 *            isAndroid()
	 * @return whether this is iPad or Android
	 */
	@Deprecated
	public static boolean isTabletBrowser() {
		return isAndroid() || NavigatorUtil.isiOS();
	}

	public static native int getScreenWidth() /*-{
		return $wnd.screen.width;
	}-*/;

	public static native int getScreenHeight() /*-{
		return $wnd.screen.height;
	}-*/;

	public static native boolean isEdge() /*-{
		return $wnd.navigator.userAgent.indexOf("Edge") > -1;
	}-*/;

	/**
	 * @param full
	 *            whether to go fullscreen
	 * @param element
	 *            element to be scaled
	 */
	public static native void toggleFullscreen(boolean full,
			JavaScriptObject element)/*-{
		var el = element || $doc.documentElement;
		if (full) { // current working methods
			if (el.requestFullscreen) {
				el.requestFullscreen();
			} else if ($doc.documentElement.msRequestFullscreen) {
				el.msRequestFullscreen();
			} else if ($doc.documentElement.mozRequestFullScreen) {
				el.mozRequestFullScreen();
			} else if ($doc.documentElement.webkitRequestFullScreen) {
				el.style.setProperty("width", "100%", "important");
				el.style.setProperty("height", "100%", "important");
				el.webkitRequestFullScreen();
				//Element.ALLOW_KEYBOARD_INPUT);
			}
		} else {
			if ($doc.exitFullscreen) {
				$doc.exitFullscreen();
			} else if ($doc.msExitFullscreen) {
				$doc.msExitFullscreen();
			} else if ($doc.mozCancelFullScreen) {
				$doc.mozCancelFullScreen();
			} else if ($doc.webkitCancelFullScreen) {
				$doc.webkitCancelFullScreen();
			}
		}
	}-*/;

	/**
	 * @return event name for fullscreen event.
	 */
	public static String getFullscreenEventName() {
		if (!hasProperty(DomGlobal.document, "onfullscreenchange")) {
			return "webkitfullscreenchange";
		}
		return "fullscreenchange";
	}

	/**
	 * @return whether applet fullscreen is used
	 */
	public static Boolean isFullscreen() {
		return Js.isTruthy(DomGlobal.document.fullscreenElement)
				|| hasProperty(DomGlobal.document, "webkitFullscreenElement");
	}

	/**
	 * @return whether current window covers whole screen
	 */
	public static native boolean isCoveringWholeScreen()/*-{
		var height = $wnd.innerHeight;
		var width = $wnd.innerWidth;

		var screenHeight = screen.height - 5;
		var screenWidth = screen.width - 5;

		//$wnd.console.log("height: " + height, screenHeight);
		//$wnd.console.log("width: " + width, screenWidth);

		return height >= screenHeight && width >= screenWidth;
	}-*/;

	/**
	 * Add mutation observer to element and all its parents.
	 *
	 * @param el
	 *            target element
	 * @param asyncOperation
	 *            callback
	 */
	public static native void addMutationObserver(Element el,
			AsyncOperation<String> asyncOperation) /*-{
		try {
			var current = el;
			while (current) {
				var observer = new MutationObserver(
						function(mutations) {
							mutations
									.forEach(function(mutation) {
										asyncOperation.@org.geogebra.common.util.AsyncOperation::callback(*)(mutation.type);
									});
						});
				observer.observe(current, {
					attributes : true,
					attributeFilter : [ "class", "style" ]
				});
				current = current.parentElement;
			}
		} catch (ex) {
			//Mutation observer not supported
		}
	}-*/;

	/**
	 * gets keycodes of iOS arrow keys iOS arrows have a different identifier
	 * than win and android
	 *
	 * @param event
	 *            native key event
	 * @return JavaKeyCodes of arrow keys, -1 if pressed key was not an arrow
	 */
	public native static int getIOSArrowKeys(NativeEvent event) /*-{

		var key = event.key;
		@org.geogebra.common.util.debug.Log::debug(Ljava/lang/Object;)("KeyDownEvent: " + key);
		switch (key) {
		case "UIKeyInputUpArrow":
			return @com.himamis.retex.editor.share.util.GWTKeycodes::KEY_UP;
		case "UIKeyInputDownArrow":
			return @com.himamis.retex.editor.share.util.GWTKeycodes::KEY_DOWN;
		case "UIKeyInputLeftArrow":
			return @com.himamis.retex.editor.share.util.GWTKeycodes::KEY_LEFT;
		case "UIKeyInputRightArrow":
			return @com.himamis.retex.editor.share.util.GWTKeycodes::KEY_RIGHT;
		default:
			return -1;
		}
	}-*/;

	/**
	 * @return whether current browser is Chrome
	 */
	public static boolean isChrome() {
		// yep, Edge UA string contains Chrome too
		return Navigator.getUserAgent().matches(".*Chrome/.*") && !isEdge();
	}

	/**
	 * @return whether we're running in a Mac browser
	 */
	public static boolean isMacOS() {
		return Navigator.getUserAgent().contains("Macintosh")
				|| Navigator.getUserAgent().contains("Mac OS");
	}

	/**
	 * @param txt
	 *            plain text file in UTF-8
	 * @return valid data URL, browser dependent
	 */
	public static String addTxtMarker(String txt) {
		return NavigatorUtil.isiOS() && isSafariByVendor()
				? StringUtil.txtMarkerForSafari + Global.encodeURIComponent(txt)
				: StringUtil.txtMarker + txt;
	}

	/**
	 * Checks for screen readers that don't support keyboard event handling in
	 * canvas.
	 *
	 * @return whether emulator of tab handler is needed
	 */
	public static boolean needsAccessibilityView() {
		return NavigatorUtil.isMobile();
	}

	/**
	 * @return whether the browser is online
	 */
	public static boolean isOnline() {
		return DomGlobal.navigator == null || DomGlobal.navigator.onLine;
	}
}
