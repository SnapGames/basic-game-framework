/**
 * SnapGames
 * 
 * @since 2018
 * @see https://github.com//SnapGames/basic-game-framework/wiki
 */

package fr.snapgames.bgf.core.gfx.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import fr.snapgames.bgf.core.entity.GameObject;
import fr.snapgames.bgf.core.gfx.Render;

/**
 * 
 * @author Frédéric Delorme
 *
 */
public class UIText extends GameObject {

	private String text = "";
	private Font font;
	private int thickness;

	/**
	 * @param name
	 */
	public UIText(String name) {
		super(name);
	}

	/**
	 * @param name
	 * @param x
	 * @param y
	 * @param image
	 */
	public UIText(String name, int x, int y, BufferedImage image) {
		super(name, x, y, image);
	}

	@Override
	public void render(Graphics2D g) {
		g.setFont(font);

		Render.drawOutlinedString(g, (int)position.x, (int)position.y, text, thickness, Color.WHITE, Color.BLACK);
	}

	public UIText setThickness(int thickness) {
		this.thickness = thickness;
		return this;
	}

	public UIText setText(String text) {
		this.text = text;
		return this;
	}

	public UIText setFont(Font font) {
		this.font = font;
		return this;
	}

	public static UIText builder(String name) {
		return new UIText(name);
	}
}
