package GUI;

import Components.Sample1;
import Hardware.Screen;
import Libraries.MenusLib;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
	Scene scene;
	Screen screen;
	BorderPane container;
	VBox head;
	MenusLib menu;
	TabPane tabPane;
	Draw draw;

	@Override
	public void start(Stage stage) throws Exception {
		initState();

		scene = new Scene(container, screen.getWidth(), screen.getHeight());
		stage.setScene(scene);
		// stage.setFullScreen(true);
		stage.setTitle("GreenUML");
		stage.centerOnScreen();
		stage.show();

		menu.handB.setOnAction(e -> {
			addNewTab();
		});
		menu.cpointB.setOnAction(e -> {
			System.out.println("Selected Color " + menu.cpikcer.getValue().toString());
			scene.setCursor(Cursor.HAND);
		});
		menu.gHLineB.setOnAction(e -> {
			System.out.println("Tabs " + tabPane.getTabs());
			tabPane.getSelectionModel().getSelectedItem().getContent().setStyle("-fx-background-color:blue;");

		});
		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> arg0, Tab arg1, Tab arg2) {
				System.out.println("Selected Tabs Index : " + tabPane.getSelectionModel().getSelectedIndex());
				Draw draw = (Draw) tabPane.getSelectionModel().getSelectedItem().getContent();
				draw.getArea().addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						// scene.setCursor(Cursor.DEFAULT);
						//draw.getArea().setStyle("-fx-background-color:green;");
						System.out.println("Selected Tool is : " + draw.getCTool());
						if (draw.getCTool().equals("1")) {
							Sample1 sample=new Sample1(e.getX(),e.getY());
							draw.getArea().getChildren().add(sample);
						}
					}
				});
			}
		});

	}

	public void initState() {
		container = new BorderPane();
		screen = new Screen();
		head = new VBox();
		menu = new MenusLib();
		head.getChildren().addAll(menu.bar, menu.sbar);
		tabPane = new TabPane();
		tabPane.setMinWidth(screen.getWidth());
		container.setTop(head);
		ScrollPane sp = new ScrollPane();
		sp.setPrefWidth(screen.getWidth() - 20);
		sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sp.setContent(tabPane);
		container.setCenter(sp);
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	public void addNewTab() {
		Tab tab = new Tab();
		draw = new Draw(scene);
		tab.setContent(draw);
		tab.setText("" + System.currentTimeMillis());
		tabPane.getTabs().add(tab);
		tabPane.setMaxWidth(screen.getWidth() - 500);
	}

}
