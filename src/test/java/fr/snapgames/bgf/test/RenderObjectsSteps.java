package fr.snapgames.bgf.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cucumber.api.java8.En;
import fr.snapgames.bgf.core.Game;
import fr.snapgames.bgf.core.entity.GameEntity;
import fr.snapgames.bgf.core.entity.GameObject;
import fr.snapgames.bgf.core.gfx.Render;

public class RenderObjectsSteps implements En {

	private Render render;
	private Game application;
	private String[] args;
	private Collection<GameEntity> objects;

	public RenderObjectsSteps() {
		Given("^A new Game with viewport set to (\\d+) x (\\d+)$", (Integer width, Integer height) -> {
			args = new String[] { "w=" + width, "h=" + height };
			application = new Game("Render tests", args);
			application.initialize();
		});

		When("^adding (\\d+) GameObject$", (Integer arg1) -> {
			render = application.getRender();
			render.clearRenderingList();
			for (int i = 0; i < arg1; i++) {
				render.addObject(GameObject.builder("testObj_" + i));
			}
		});

		When("^adding a list of (\\d+) GameObject$", (Integer arg1) -> {
			render = application.getRender();
			render.clearRenderingList();
			List<GameEntity> goToAdd = new ArrayList<>();
			for (int i = 0; i < arg1; i++) {
				goToAdd.add(GameObject.builder("testObj_" + i));
			}
			render.addAllObjects(goToAdd);
		});

		Then("^the internal rendering list contains (\\d+) element$", (Integer arg1) -> {
			objects = render.getRenderingList();
			assertTrue(objects.size() == arg1);
		});

	}
}