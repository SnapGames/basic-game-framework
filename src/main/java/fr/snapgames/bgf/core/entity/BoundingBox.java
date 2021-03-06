/**
 * SnapGames
 * 
 * Game Development Java
 * 
 * basic-game-framework
 * 
 * @year 2018
 */
package fr.snapgames.bgf.core.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * This object is a Bounding box for any GameObject.
 * 
 * @author Frédéric Delorme
 *
 */
public class BoundingBox {

	/**
	 * Bounding Box Type for this object ?
	 * <ul>
	 * <li>RECTANGLE for rectangle shapes,</li>
	 * <li>CIRCLE for ... CIRCLE shapes.</li>
	 * </ul>
	 * 
	 * @author Frédéric Delorme
	 *
	 */
	public enum BoundingBoxType {
		RECTANGLE, CIRCLE
	}

	private Rectangle2D.Float box;
	private BoundingBoxType type;
	private Color color = Color.ORANGE;

	BoundingBox(BoundingBoxType type) {
		this.type = type;
		box = new Rectangle2D.Float();
	}

	public void setBox(int x, int y, int width, int height) {
		box.x = x;
		box.y = y;
		box.width = width;
		box.height = height;
	}

	public Rectangle getBox() {
		return box.getBounds();
	}

	public BoundingBoxType getType() {
		return type;
	}

	/**
	 * Update the BoundingBox according to its linked GameEntity.
	 * 
	 * @param ge
	 */
	public void update(GameEntity ge) {
		GameObject go = (GameObject) ge;
		this.box.x = go.position.x;
		this.box.y = go.position.y;
		this.box.width = go.size.x;
		this.box.height = go.size.y;
	}

	/**
	 * render the bounding box. mainly used for Debug purpose.
	 * 
	 * @param g
	 */
	public void render(Graphics2D g) {
		g.setColor(this.color);
		switch (type) {
		case CIRCLE:
			g.drawArc((int) box.x, (int) box.y, (int) box.width, (int) box.height, 0, 360);
			break;
		case RECTANGLE:
			g.drawRect((int) box.x, (int) box.y, (int) box.width, (int) box.height);
			break;
		default:
			break;
		}
	}

	/**
	 * set the type of BoundingBox for this BoundingBox.
	 * 
	 * @param type
	 */
	public void setType(BoundingBoxType type) {
		this.type = type;
	}
}
