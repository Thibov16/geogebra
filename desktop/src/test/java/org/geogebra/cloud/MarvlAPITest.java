package org.geogebra.cloud;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.geogebra.common.jre.util.Base64;
import org.geogebra.common.move.ggtapi.events.LoginEvent;
import org.geogebra.common.move.ggtapi.models.AuthenticationModel;
import org.geogebra.common.move.ggtapi.models.Chapter;
import org.geogebra.common.move.ggtapi.models.ClientInfo;
import org.geogebra.common.move.ggtapi.models.GeoGebraTubeUser;
import org.geogebra.common.move.ggtapi.models.MarvlAPI;
import org.geogebra.common.move.ggtapi.models.Material;
import org.geogebra.common.move.ggtapi.models.Material.MaterialType;
import org.geogebra.common.move.ggtapi.models.MaterialRequest.Order;
import org.geogebra.common.move.ggtapi.requests.MaterialCallbackI;
import org.geogebra.common.util.StringUtil;
import org.geogebra.desktop.main.AppDNoGui;
import org.geogebra.desktop.main.LocalizationD;
import org.geogebra.desktop.move.ggtapi.models.AuthenticationModelD;
import org.geogebra.desktop.move.ggtapi.models.LoginOperationD;
import org.geogebra.desktop.util.UtilD;
import org.junit.Assert;
import org.junit.Test;

public class MarvlAPITest {

	@Test
	public void testAuth() {
		if (System.getProperty("marvl.auth.basic") == null) {
			return;
		}
		GeoGebraTubeUser usr = new GeoGebraTubeUser("");
		MarvlAPI api = authAPI();
		api.authorizeUser(usr,
				new LoginOperationD(new AppDNoGui(new LocalizationD(3), false)),
				true);
		pause(5000);
		Assert.assertEquals("GGBTest-Student", usr.getRealName());
	}

	private static void pause(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static MarvlAPI authAPI() {
		MarvlAPI ret = new MarvlAPI("http://notes.dlb-dev01.alp-dlg.net/api");
		ret.setBasicAuth(Base64.encodeToString(
				System.getProperty("marvl.auth.basic").getBytes(), false));
		return ret;
	}

	@Test
	public void testUpload() {
		if (System.getProperty("marvl.auth.basic") == null) {
			return;
		}

		MarvlAPI api = authAPI();
		doUpload(api, "Test material");
	}

	private static void doUpload(MarvlAPI api, String title) {

		final ArrayList<String> titles = new ArrayList<>();
		final ArrayList<String> errors = new ArrayList<>();
		api.uploadMaterial(0, "S", title,
				Base64.encodeToString(UtilD.loadFileIntoByteArray(
						"src/test/resources/slides.ggs"), false),
				new MaterialCallbackI() {

					public void onLoaded(List<Material> result,
							ArrayList<Chapter> meta) {
						titles.add(result.get(0).getTitle());
					}

					public void onError(Throwable exception) {
						errors.add("Upload: " + exception.getMessage());

					}
				},
				MaterialType.ggs);
		pause(5000);
		Assert.assertEquals("", StringUtil.join(",", errors));
		Assert.assertEquals(title, StringUtil.join(",", titles));
	}

	@Test
	public void testOpen(){
		if (System.getProperty("marvl.auth.basic") == null) {
			return;
		}
		final MarvlAPI api = authAPI();
		final AppDNoGui appd = new AppDNoGui(new LocalizationD(3), false);
		api.setClient(getClient(appd));
		deleteAll(api);

		final String title = "OpenTest" + System.currentTimeMillis();
		doUpload(api, title);
		final ArrayList<String> titles = new ArrayList<>();
		final ArrayList<String> errors = new ArrayList<>();

		api.getUsersOwnMaterials(new MaterialCallbackI() {

			public void onLoaded(List<Material> result,
					ArrayList<Chapter> meta) {
				for (int i = 0; i < result.size(); i++) {
					if (title.equals(result.get(i).getTitle())) {
						titles.add(title);
						try {
							appd.getGgbApi().openFile(result.get(i).getFileName());
						}catch(Exception e){
							errors.add(e.getMessage());
						}

					}
				}
			}

			public void onError(Throwable exception) {
				errors.add(exception.getMessage());
			}
		}, Order.title);
		pause(5000);
		Assert.assertEquals("", StringUtil.join(",", errors));
		Assert.assertEquals(title, StringUtil.join(",", titles));
	}

	private static void deleteAll(final MarvlAPI api) {
		api.getUsersOwnMaterials(new MaterialCallbackI() {

			public void onLoaded(List<Material> result,
					ArrayList<Chapter> meta) {
				for (int i = 0; i < result.size(); i++) {
					api.deleteMaterial(result.get(i), new MaterialCallbackI() {

						public void onLoaded(List<Material> result,
								ArrayList<Chapter> meta) {
							// TODO Auto-generated method stub

						}

						public void onError(Throwable exception) {
							// TODO Auto-generated method stub

						}
					});
				}
			}

			public void onError(Throwable exception) {
				//
			}
		}, Order.title);
		pause(5000);

	}

	private ClientInfo getClient(AppDNoGui appd) {

		ClientInfo client = TubeAPITest.getClient();
		AuthenticationModel auth = appd.getLoginOperation() != null
				? appd.getLoginOperation().getModel()
				: new AuthenticationModelD();

		GeoGebraTubeUser user = new GeoGebraTubeUser("");
		user.setUserId(5);
		auth.onEvent(new LoginEvent(user, true, true, "{}"));
		client.setModel(auth);
		return client;

	}

	@Test
	public void testCopy() {
		if (System.getProperty("marvl.auth.basic") == null) {
			return;
		}
		allowMethods("PATCH");
		final MarvlAPI api = authAPI();
		final ArrayList<String> titles = new ArrayList<>();
		final ArrayList<String> errors = new ArrayList<>();
		final LocalizationD loc = new LocalizationD(3);
		api.uploadMaterial(0, "S", "Test material",
				Base64.encodeToString(UtilD.loadFileIntoByteArray(
						"src/test/resources/slides.ggs"), false),
				new MaterialCallbackI() {

					public void onLoaded(List<Material> result,
							ArrayList<Chapter> meta) {
						api.copy(result.get(0),
								MarvlAPI.getCopyTitle(loc,
										result.get(0).getTitle()),
								new MaterialCallbackI() {

							public void onLoaded(List<Material> resultCopy,
									ArrayList<Chapter> metaCopy) {
								titles.add(resultCopy.get(0).getTitle());
							}

							public void onError(Throwable exception) {
								errors.add(exception.getMessage());
							}

						});

					}

					public void onError(Throwable exception) {
						errors.add(exception.getMessage());

					}
				}, MaterialType.ggs);
		pause(10000);
		Assert.assertEquals("", StringUtil.join(",", errors));
		Assert.assertEquals("Copy of Test material",
				StringUtil.join(",", titles));
	}

	// Hack Java to understand PATCH method:
	// https://stackoverflow.com/a/46323891
	private static void allowMethods(String... methods) {
		try {
			Field methodsField = HttpURLConnection.class
					.getDeclaredField("methods");

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(methodsField,
					methodsField.getModifiers() & ~Modifier.FINAL);

			methodsField.setAccessible(true);

			String[] oldMethods = (String[]) methodsField.get(null);
			Set<String> methodsSet = new LinkedHashSet<>(
					Arrays.asList(oldMethods));
			methodsSet.addAll(Arrays.asList(methods));
			String[] newMethods = methodsSet.toArray(new String[0]);

			methodsField.set(null/* static field */, newMethods);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void copyTitles(){
		LocalizationD loc = new LocalizationD(3);
		Assert.assertEquals("Copy of A", MarvlAPI.getCopyTitle(loc,
				"A"));
		Assert.assertEquals("Copy of A (2)",
				MarvlAPI.getCopyTitle(loc, "Copy of A"));
		Assert.assertEquals("Copy of A (3)",
				MarvlAPI.getCopyTitle(loc, "Copy of A (2)"));
	}

}
