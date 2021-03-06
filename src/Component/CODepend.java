package Component;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class CODepend extends Line {

	// private Color color;
	public Path top;
	public Path bot;
	private Color color;

	public CODepend(double startx, double starty, double endx, double endy, Color color) {
		super(startx, starty, endx, endy);
		if (color.toString().equals("0xffffffff"))
			color = Color.BLACK;

		setStroke(color);
		this.color = color;
		getStrokeDashArray().addAll(10d, 10d);

		top = new Path();
		top.setStroke(color);
		bot = new Path();
		bot.setStroke(color);
	}

	public Path getTop() {
		return top;
	}

	public Path getBot() {
		return bot;
	}

	public void recalculatePoint() {
		double x, y, length;
		length = Math.sqrt((getEndX() - getStartX()) * (getEndX() - getStartX())
				+ (getEndY() - getStartY()) * (getEndY() - getStartY()));
		x = (getEndX() - getStartX()) / length;
		y = (getEndY() - getStartY()) / length;
		Point2D base = new Point2D(getEndX() - x * 10, getEndY() - y * 10);
		Point2D back_top = new Point2D(base.getX() - 10 * y, base.getY() + 10 * x);
		Point2D back_bottom = new Point2D(base.getX() + 10 * y, base.getY() - 10 * x);

		top.getElements().clear();
		top.getElements().add(new MoveTo(getEndX(), getEndY()));
		top.getElements().add(new LineTo(back_top.getX(), back_top.getY()));

		bot.getElements().clear();
		bot.getElements().add(new MoveTo(getEndX(), getEndY()));
		bot.getElements().add(new LineTo(back_bottom.getX(), back_bottom.getY()));
	}

}
