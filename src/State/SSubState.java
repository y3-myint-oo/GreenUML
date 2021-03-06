package State;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class SSubState extends Rectangle {

	private StringProperty data;
	public Text label;
	public Line br;
	public TextField field;

	public SSubState(double x, double y, Color color) {
		super(x, y, 200, 300);
		setArcWidth(10);
		setArcHeight(10);
		data = new SimpleStringProperty("Sub State");
		setFill(color);
		setStroke(Color.LIGHTGRAY);

		label = new Text(labelProperty().get());
		label.setFont(Font.font("Arial", FontWeight.BLACK, 14));
		label.textProperty().bind(labelProperty());

		label.xProperty().bind(xProperty().add(10));
		label.yProperty().bind(yProperty().add(20));

		br = new Line();
		br.setStroke(Color.LIGHTGRAY);
		br.startXProperty().bind(xProperty());
		br.startYProperty().bind(yProperty().add(30));
		br.endXProperty().bind(xProperty().add(getWidth()));
		br.endYProperty().bind(yProperty().add(30));

		field = new TextField(labelProperty().get());
		field.layoutXProperty().bind(xProperty().subtract(20));
		field.layoutYProperty().bind(yProperty().add(5));
		field.textProperty().bindBidirectional(labelProperty());

		addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				setX(e.getX());
				setY(e.getY());
			}
		});

		label.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				System.out.println("Click On Label");
				field.setText(labelProperty().get());
				field.setVisible(true);
			}
		});

		DoubleProperty width = new SimpleDoubleProperty();
		field.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				width.set(label.layoutBoundsProperty().getValue().getWidth());
				label.xProperty().bind(xProperty().add(widthProperty().getValue() / 2)
						.subtract(label.layoutBoundsProperty().getValue().getWidth() / 2));
				widthProperty().bind(width.add(20));
				br.endXProperty().bind(xProperty().add(getWidth()));
				if (e.getCode() == KeyCode.ENTER) {
					field.setVisible(false);
				}
			}
		});
	}

	public TextField getText(boolean isShow) {
		field.setText(labelProperty().get());
		if (isShow) {
			field.setVisible(isShow);
		} else {
			field.setVisible(false);
		}
		return field;
	}

	public void setTextInVisible() {
		field.setVisible(false);
	}

	public Text getLabel() {
		return label;
	}

	public final StringProperty labelProperty() {
		return data;
	}

	public Line getBr() {
		return br;
	}

}
