package GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import Activity.AAction;
import Activity.AEdge;
import Activity.AEndNode;
import Activity.AInitNode;
import Activity.ARegion;
import Activity.ATime;
import Boxs.BExport;
import Boxs.BNewDiagram;
import Boxs.BNewProject;
import Boxs.BOpen;
import Boxs.BPrintPreview;
import ClassD.ClassAbstract;
import ClassD.ClassD;
import ClassD.ClassDataBox;
import ClassD.ClassDepen;
import ClassD.ClassFunBox;
import ClassD.ClassInterface;
import ClassD.EditClassDataBox;
import Component.COArtifact;
import Component.COComponent;
import Component.CODepend;
import Component.COLibrary;
import Component.COPackage;
import Component.COSComponent;
import Development.DComponent;
import Development.DDatabase;
import Development.DDeivce;
import Development.DFile;
import Development.DProtocal;
import Development.DSoftware;
import Development.DSystem;
import GTool.GLabel;
import Hardware.Screen;
import Libraries.MenusLib;
import Libraries.OS;
import Libraries.Pack;
import Libraries.Region;
import Libraries.Tool;
import Sequence.SEActivation;
import Sequence.SEDActivation;
import Sequence.SENActivation;
import Sequence.SERole;
import State.SFinalState;
import State.SHistory;
import State.SStartState;
import State.SState;
import State.SSubState;
import UseCase.UCActor;
import UseCase.UCBoundary;
import UseCase.UCExtend;
import UseCase.UCGeneral;
import UseCase.UCInclude;
import UseCase.UCProcess;
import UseCase.UCRelation;
import XMLFactory.CopyXML;
import XMLFactory.UCXml;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class Main extends Application {
	Scene scene;
	Screen screen;
	BorderPane container;
	VBox head;
	MenusLib menu;
	TabPane tabPane;
	SingleSelectionModel<Tab> tabSelect;
	Draw draw;
	CopyXML copyxml;
	File iFile;

	String osType;
	File folder; // Temp Diagrams Folder
	File tempPIC;
	Region region;
	// UseCase

	UCRelation ucrelation;
	UCGeneral ucgeneral;
	UCInclude ucinclude;
	UCExtend ucextend;

	SEActivation seactivation;
	SENActivation senactivation;
	SEDActivation sedactivation;

	AEdge aedge;

	CODepend codepend;

	DProtocal dprotocal;

	ClassDepen cdepen;
	Stage stage;
	private PageLayout pageLayout;
	public Printer printer;

	@Override
	public void start(Stage stage) throws Exception {
		initState();
		this.stage = stage;
		scene = new Scene(container, screen.getWidth(), screen.getHeight());
		stage.setScene(scene);
		// stage.setFullScreen(true);
		stage.setTitle("GreenUML");
		stage.centerOnScreen();
		stage.show();

		menu.printB.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				PrinterJob job = PrinterJob.createPrinterJob();
				JobSettings setting = job.getJobSettings();
				setting.setPageLayout(pageLayout);
				Draw draw = (Draw) tabPane.getSelectionModel().getSelectedItem().getContent();
				boolean isPrint = job.showPrintDialog(draw.getArea().getScene().getWindow());
				if (isPrint) {
					// Scale
					double scaleX = pageLayout.getPrintableWidth() / draw.getArea().getLayoutBounds().getWidth();
					double scaleY = pageLayout.getPrintableHeight() / draw.getArea().getLayoutBounds().getHeight();
					double min = Math.min(scaleX, scaleY);
					Scale scale = new Scale(min, min);
					try {
						draw.getArea().getTransforms().add(scale);
						job.printPage(draw.getArea());
						job.endJob();
						draw.getArea().getTransforms().remove(scale);
						System.out.println("***Print Success***");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		menu.rSelectB.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				scene.setCursor(Cursor.SE_RESIZE);
			}
		});
		menu.closeAll.setOnAction(e -> {
			closeAllTab();
		});

		menu.clean.setOnAction(e -> {
			clean();
		});
		menu.gridLine.setOnAction(e -> {
			if (!tabPane.getSelectionModel().isEmpty()) {
				Draw draw = (Draw) tabPane.getSelectionModel().getSelectedItem().getContent();
				if (menu.isgBLine) {
					draw.getArea().getChildren().add(menu.gridPane);
					menu.gridPane.toBack();
					menu.isgBLine = false;
				} else {
					menu.isgBLine = true;
					draw.getArea().getChildren().remove(menu.gridPane);
				}
			}
		});
		menu.cpointB.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				scene.setCursor(Cursor.HAND);
			}
		});
		menu.deleteB.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				scene.setCursor(Cursor.CROSSHAIR);
			}
		});
		menu.selectB.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				scene.setCursor(Cursor.OPEN_HAND);
			}
		});
		menu.gBLineB.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				menu.gridLine.fire();
			}
		});

		stage.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent key) {
				if (menu.pasteKey.match(key)) {
					menu.dbFactory = DocumentBuilderFactory.newInstance();
					try {
						menu.dBuilder = menu.dbFactory.newDocumentBuilder();
						menu.doc = menu.dBuilder.parse("Temp/Copy.xml");
						Draw draw = (Draw) tabPane.getSelectionModel().getSelectedItem().getContent();
						switch (menu.doc.getElementsByTagName("diagram").item(0).getTextContent()) {
						case "UCProcess":
							double x = Double
									.parseDouble(menu.doc.getElementsByTagName("centerx").item(0).getTextContent());
							double y = Double
									.parseDouble(menu.doc.getElementsByTagName("centery").item(0).getTextContent());
							double xr = Double
									.parseDouble(menu.doc.getElementsByTagName("xradius").item(0).getTextContent());
							double yr = Double
									.parseDouble(menu.doc.getElementsByTagName("yradius").item(0).getTextContent());
							String text = menu.doc.getElementsByTagName("label").item(0).getTextContent();
							String color = menu.doc.getElementsByTagName("color").item(0).getTextContent();

							UCProcess process = new UCProcess(stage, x + 10, y + 10, Color.web(color));
							process.setRadiusX(xr);
							process.setRadiusY(yr);
							process.data.set(text);
							process.label.layoutXProperty().bind(process.centerXProperty()
									.subtract(process.label.layoutBoundsProperty().getValue().getWidth() / 2));
							draw.getArea().getChildren().addAll(process, process.getLabel(), process.getText(false));
							draw.objects.add(process);
							break;
						case "UCActor":
							double ucax = Double
									.parseDouble(menu.doc.getElementsByTagName("centerx").item(0).getTextContent());
							double ucay = Double
									.parseDouble(menu.doc.getElementsByTagName("centery").item(0).getTextContent());
							String ucatext = menu.doc.getElementsByTagName("label").item(0).getTextContent();
							String ucacolor = menu.doc.getElementsByTagName("color").item(0).getTextContent();
							UCActor actor = new UCActor(stage, ucax + 10, ucay + 10, Color.web(ucacolor));
							actor.data.set(ucatext);
							draw.getArea().getChildren().addAll(actor, actor.getBody(), actor.getLeg(), actor.getLeg2(),
									actor.getLeg3(), actor.getLeg4(), actor.getLabel(), actor.getText(false));
							draw.objects.add(actor);
							break;
						case "UCBoundary":
							System.out.println("UCBoundary Paste");
							double ucbx = Double
									.parseDouble(menu.doc.getElementsByTagName("x").item(0).getTextContent());
							double ucby = Double
									.parseDouble(menu.doc.getElementsByTagName("y").item(0).getTextContent());
							double ucbw = Double
									.parseDouble(menu.doc.getElementsByTagName("width").item(0).getTextContent());
							double ucbh = Double
									.parseDouble(menu.doc.getElementsByTagName("height").item(0).getTextContent());
							String ucbtext = menu.doc.getElementsByTagName("label").item(0).getTextContent();
							String ucbcolor = menu.doc.getElementsByTagName("color").item(0).getTextContent();
							UCBoundary boundary = new UCBoundary(stage, ucbx + 40, ucby + 40, Color.web(ucbcolor));
							boundary.setWidth(ucbw);
							boundary.setHeight(ucbh);
							boundary.data.set(ucbtext);

							draw.getArea().getChildren().addAll(boundary, boundary.resizeVB, boundary.resizeHB);
							boundary.toBack();
							draw.objects.add(boundary);

							break;
						}
					} catch (Exception e) {

					}
				} else if (menu.saveKey.match(key)) {
					menu.save.fire();
				} else if (menu.cutKey.match(key)) {
					Draw draw = (Draw) tabPane.getSelectionModel().getSelectedItem().getContent();
					for (int i = 0; i < draw.objects.size(); i++) {
						Object obj = draw.objects.get(i);
						CopyXML copy = null;
						try {
							copy = new CopyXML();
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (obj instanceof UCProcess) {
							UCProcess uc = (UCProcess) obj;
							if (uc.isHover()) {
								copy.copyUCProcess(uc.getCenterX(), uc.getCenterY(), uc.getRadiusX(), uc.getRadiusY(),
										uc.data.get(), uc.getFill().toString());
								draw.getArea().getChildren().removeAll(uc, uc.getLabel(), uc.getText(false));
							} else {
								System.out.println("Not Pressed");
							}
						}
					}
				}
			}
		});

		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> arg0, Tab arg1, Tab arg2) {
				if (tabPane.getTabs().size() > 0) {

					Draw draw = (Draw) tabPane.getSelectionModel().getSelectedItem().getContent();
					draw.getArea().addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							Object obj = e.getTarget();
							Color color = Color.web(menu.cpikcer.getValue().toString());
							if (obj instanceof Pane && scene.getCursor() == Cursor.SE_RESIZE) {
								System.out.println("Region Draw");
								region = new Region();
								region.setX(e.getX());
								region.setY(e.getY());
								draw.getArea().getChildren().addAll(region);
								menu.isRegionDraw = true;
							} else

							if (obj instanceof Pane || (obj instanceof UCBoundary && draw.getCTool() != Tool.POINTER
									&& draw.getCTool() != Tool.UCBOUNDARY)) {
								switch (draw.getCTool()) {
								case GLabel:
									GLabel label = new GLabel(e.getX(), e.getY(), menu.Fonts.fonts);
									draw.getArea().getChildren().addAll(label, label.getText(false),
											label.getTool(false));
									draw.objects.add(label);
									break;
								case UCPROCESS:
									UCProcess process = new UCProcess(stage, e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(process, process.getLabel(),
											process.getText(false));
									draw.objects.add(process);
									break;
								case UCACTOR:
									UCActor actor = new UCActor(stage, e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(actor, actor.getBody(), actor.getLeg(),
											actor.getLeg2(), actor.getLeg3(), actor.getLeg4(), actor.getLabel(),
											actor.getText(false));
									draw.objects.add(actor);
									break;
								case UCREALATION:
									ucrelation = new UCRelation(stage, e.getX(), e.getY(), e.getX(), e.getY());
									draw.getArea().getChildren().addAll(ucrelation);
									menu.isUCRelation = true;
									break;
								case UCGENERAL:
									ucgeneral = new UCGeneral(e.getX(), e.getY(), e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(ucgeneral);
									menu.isUCGeneral = true;
									break;
								case UCBOUNDARY:
									UCBoundary ucboundary = new UCBoundary(stage, e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(ucboundary, ucboundary.resizeVB,
											ucboundary.resizeHB);
									ucboundary.toBack();
									draw.objects.add(ucboundary);
									break;
								case UCINCLUDE:
									ucinclude = new UCInclude(e.getX(), e.getY(), e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(ucinclude);
									menu.isUCInclude = true;
									break;
								case UCEXTEND:
									ucextend = new UCExtend(e.getX(), e.getY(), e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(ucextend);
									menu.isUCExtend = true;
									break;
								case SEROLE:
									SERole role = new SERole(e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(role, role.label, role.line,
											role.getText(false));
									role.line.toBack();
									break;
								case SEACTIVATION:
									seactivation = new SEActivation(e.getX(), e.getY(), e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(seactivation);
									menu.isActivation = true;
									break;
								case SENEWACTIVATION:
									senactivation = new SENActivation(e.getX(), e.getY(), e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(senactivation);
									menu.isNActivation = true;
									break;
								case SEDESTROYACTIVATION:
									sedactivation = new SEDActivation(e.getX(), e.getY(), e.getX(), e.getY());
									draw.getArea().getChildren().addAll(sedactivation);
									menu.isDActivation = true;
									break;
								case CLASS:
									ClassD classd = new ClassD(e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(classd, classd.dataBox, classd.funBox,
											classd.label, classd.getText(false), classd.resizeB);
									// classd.resizeB.toBack();
									classd.dataBox.addEventFilter(MouseEvent.MOUSE_CLICKED,
											new EventHandler<MouseEvent>() {
												@Override
												public void handle(MouseEvent e) {
													ClassDataBox box = new ClassDataBox(stage);
													box.sizeToScene();
													container.setDisable(true);
													box.setAlwaysOnTop(true);
													box.showAndWait();
													if (box.condition.equals("create")) {
														classd.addData(box.label);
														Text data = new Text();
														if (box.isStatic) {
															data.setUnderline(true);
														}
														// TextFlow
														// flow=coloredClassDataLabel(box.label);

														int size = classd.getDatas().size();
														data.textProperty()
																.bindBidirectional(classd.getDatas().get(--size));
														data.setLayoutX(classd.dataBox.getX() + 10);
														data.setLayoutY(
																classd.dataBox.getY() + classd.dataBox.getHeight());

														data.layoutXProperty().bind(classd.dataBox.xProperty().add(10));
														data.layoutYProperty().bind(classd.dataBox.yProperty()
																.add(classd.dataBox.getHeight()));
														classd.dataBox.setHeight(classd.dataBox.getHeight() + 20);

														if ((data.layoutBoundsProperty().getValue().getWidth()
																+ 10) >= classd.dataBox.getWidth()) {
															classd.dataBox.setWidth(
																	data.layoutBoundsProperty().getValue().getWidth()
																			+ 30);
														}
														draw.getArea().getChildren().add(data);

														data.addEventFilter(MouseEvent.MOUSE_PRESSED,
																new EventHandler<MouseEvent>() {
																	@Override
																	public void handle(MouseEvent e) {
																		EditClassDataBox box = new EditClassDataBox(
																				stage, data.getText().trim());
																		box.sizeToScene();
																		container.setDisable(true);
																		box.setAlwaysOnTop(true);
																		box.showAndWait();
																		if (box.condition.equals("Delete")) {
																			draw.getArea().getChildren().remove(data);
																			// classd.dataBox.setHeight(
																			// classd.dataBox.getHeight()
																			// -
																			// 20);
																		} else if (box.condition.equals("Update")) {
																			data.setText("Changed");
																		}
																		container.setDisable(false);
																	}
																});

													}
													container.setDisable(false);
												}
											});
									classd.funBox.addEventFilter(MouseEvent.MOUSE_CLICKED,
											new EventHandler<MouseEvent>() {
												@Override
												public void handle(MouseEvent e) {
													ClassFunBox box = new ClassFunBox(stage);
													box.sizeToScene();
													container.setDisable(true);
													box.setAlwaysOnTop(true);
													box.showAndWait();
													if (box.condition.equals("create")) {
														classd.addFunction(box.label);
														Text data = new Text();
														int size = classd.getFunctions().size();
														data.textProperty()
																.bindBidirectional(classd.getFunctions().get(--size));
														data.setLayoutX(classd.funBox.getX() + 10);
														data.setLayoutY(
																classd.funBox.getY() + classd.funBox.getHeight());

														data.layoutXProperty().bind(classd.funBox.xProperty().add(10));
														data.layoutYProperty().bind(classd.funBox.yProperty()
																.add(classd.funBox.getHeight()));

														classd.funBox.setHeight(classd.funBox.getHeight() + 20);

														if ((data.layoutBoundsProperty().getValue().getWidth()
																+ 10) >= classd.funBox.getWidth()) {
															classd.funBox.setWidth(
																	data.layoutBoundsProperty().getValue().getWidth()
																			+ 30);
														}
														draw.getArea().getChildren().add(data);
													}
													container.setDisable(false);
												}
											});

									break;
								case CABSTRACT:
									ClassAbstract cabstract = new ClassAbstract(e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(cabstract, cabstract.funBox, cabstract.label,
											cabstract.getText(false), cabstract.resizeB);
									cabstract.funBox.addEventFilter(MouseEvent.MOUSE_CLICKED,
											new EventHandler<MouseEvent>() {
												@Override
												public void handle(MouseEvent e) {
													ClassFunBox box = new ClassFunBox(stage);
													box.sizeToScene();
													container.setDisable(true);
													box.setAlwaysOnTop(true);
													box.showAndWait();
													if (box.condition.equals("create")) {
														cabstract.addFunction(box.label);
														Text data = new Text();
														data.setFont(Font.font("Arial", FontWeight.NORMAL,
																FontPosture.ITALIC, 13));
														int size = cabstract.getFunctions().size();
														data.textProperty().bindBidirectional(
																cabstract.getFunctions().get(--size));
														data.setLayoutX(cabstract.funBox.getX() + 10);
														data.setLayoutY(
																cabstract.funBox.getY() + cabstract.funBox.getHeight());

														data.layoutXProperty()
																.bind(cabstract.funBox.xProperty().add(10));
														data.layoutYProperty().bind(cabstract.funBox.yProperty()
																.add(cabstract.funBox.getHeight()));

														cabstract.funBox.setHeight(cabstract.funBox.getHeight() + 20);

														if ((data.layoutBoundsProperty().getValue().getWidth()
																+ 10) >= cabstract.funBox.getWidth()) {
															cabstract.funBox.setWidth(
																	data.layoutBoundsProperty().getValue().getWidth()
																			+ 30);
														}
														draw.getArea().getChildren().add(data);
													}
													container.setDisable(false);
												}
											});
									break;
								case CINTERFACE:
									ClassInterface inter = new ClassInterface(e.getX(), e.getY(), color);
									draw.getArea().getChildren().addAll(inter, inter.head, inter.dataBox, inter.funBox,
											inter.label, inter.getText(false), inter.resizeB);
									inter.dataBox.addEventFilter(MouseEvent.MOUSE_CLICKED,
											new EventHandler<MouseEvent>() {
												@Override
												public void handle(MouseEvent e) {
													ClassDataBox box = new ClassDataBox(stage);
													box.sizeToScene();
													container.setDisable(true);
													box.setAlwaysOnTop(true);
													box.showAndWait();
													if (box.condition.equals("create")) {
														inter.addData(box.label);
														Text data = new Text();
														if (box.isStatic) {
															data.setUnderline(true);
														}
														// TextFlow
														// flow=coloredClassDataLabel(box.label);

														int size = inter.getDatas().size();
														data.textProperty()
																.bindBidirectional(inter.getDatas().get(--size));
														data.setLayoutX(inter.dataBox.getX() + 10);
														data.setLayoutY(
																inter.dataBox.getY() + inter.dataBox.getHeight());

														data.layoutXProperty().bind(inter.dataBox.xProperty().add(10));
														data.layoutYProperty().bind(inter.dataBox.yProperty()
																.add(inter.dataBox.getHeight()));

														inter.dataBox.setHeight(inter.dataBox.getHeight() + 20);

														if ((data.layoutBoundsProperty().getValue().getWidth()
																+ 10) >= inter.dataBox.getWidth()) {
															inter.dataBox.setWidth(
																	data.layoutBoundsProperty().getValue().getWidth()
																			+ 30);
														}
														draw.getArea().getChildren().add(data);
													}
													container.setDisable(false);
												}
											});
									inter.funBox.addEventFilter(MouseEvent.MOUSE_CLICKED,
											new EventHandler<MouseEvent>() {
												@Override
												public void handle(MouseEvent e) {
													ClassFunBox box = new ClassFunBox(stage);
													box.sizeToScene();
													container.setDisable(true);
													box.setAlwaysOnTop(true);
													box.showAndWait();
													if (box.condition.equals("create")) {
														inter.addFunction(box.label);
														Text data = new Text();
														int size = inter.getFunctions().size();
														data.textProperty()
																.bindBidirectional(inter.getFunctions().get(--size));
														data.setLayoutX(inter.funBox.getX() + 10);
														data.setLayoutY(inter.funBox.getY() + inter.funBox.getHeight());

														data.layoutXProperty().bind(inter.funBox.xProperty().add(10));
														data.layoutYProperty().bind(
																inter.funBox.yProperty().add(inter.funBox.getHeight()));

														inter.funBox.setHeight(inter.funBox.getHeight() + 20);

														if ((data.layoutBoundsProperty().getValue().getWidth()
																+ 10) >= inter.funBox.getWidth()) {
															inter.funBox.setWidth(
																	data.layoutBoundsProperty().getValue().getWidth()
																			+ 30);
														}
														draw.getArea().getChildren().add(data);
													}
													container.setDisable(false);
												}
											});
									break;
								case CDEPENDENCY:
									cdepen = new ClassDepen(e.getX(), e.getY(), e.getX(), e.getY(), color);
									draw.getChildren().add(cdepen);
									menu.isCDepend = true;
									break;
								case STATESTART:
									SStartState start = new SStartState(e.getX(), e.getY(), color);
									draw.getChildren().add(start);
									break;
								case STATEFINAL:
									SFinalState sfinal = new SFinalState(e.getX(), e.getY(), color);
									draw.getChildren().addAll(sfinal.outer, sfinal);
									break;
								case STATE:
									SState state = new SState(e.getX(), e.getY(), color);
									draw.getChildren().addAll(state, state.label, state.getText(true));
									break;
								case SUBSTATE:
									SSubState substate = new SSubState(e.getX(), e.getY(), color);
									draw.getChildren().addAll(substate, substate.label, substate.br,
											substate.getText(false));
									break;
								case STATEHISTORY:
									SHistory hisState = new SHistory(e.getX(), e.getY(), color);
									draw.getChildren().addAll(hisState, hisState.label, hisState.hlabel, hisState.br1,
											hisState.br2);
									break;
								case STATETRANSITION:
									// STransition stateTran=new STransition();
									break;
								case AACTION:
									AAction aaction = new AAction(e.getX(), e.getY(), color);
									draw.getChildren().addAll(aaction, aaction.getText(false), aaction.label);
									break;
								case AEDGE:
									System.out.println("Activiy edge");
									aedge = new AEdge(e.getX(), e.getY(), e.getX(), e.getY(), color);
									draw.getChildren().addAll(aedge);
									menu.isAEdge = true;
									break;
								case AENDNODE:
									AEndNode aendnode = new AEndNode(e.getX(), e.getY(), color);
									draw.getChildren().addAll(aendnode.outer, aendnode);
									break;
								case AINITNODE:
									AInitNode ainitnode = new AInitNode(e.getX(), e.getY(), color);
									draw.getChildren().addAll(ainitnode);
									break;
								case AREGION:
									ARegion aregion = new ARegion(e.getX(), e.getY(), color);
									draw.getChildren().addAll(aregion, aregion.label, aregion.getText(false));
									break;
								case ATIME:
									ATime atime = new ATime(e.getX(), e.getY(), color);
									draw.getChildren().addAll(atime, atime.l0, atime.l1, atime.l2, atime.l3);
									break;
								case COARTEFACT:
									COArtifact coartifact = new COArtifact(e.getX(), e.getY(), color);
									draw.getChildren().addAll(coartifact, coartifact.head, coartifact.label,
											coartifact.field);
									break;
								case COCOMPONENT:
									COComponent cocomponent = new COComponent(e.getX(), e.getY(), color);
									draw.getChildren().addAll(cocomponent, cocomponent.label, cocomponent.node1,
											cocomponent.node2);
									break;
								case CODEPEND:
									codepend = new CODepend(e.getX(), e.getY(), e.getX(), e.getY(), color);
									menu.isCODepend = true;
									draw.getChildren().add(codepend);
									break;
								case COLIBRARY:
									COLibrary colibrary = new COLibrary(e.getX(), e.getY(), color);
									draw.getChildren().addAll(colibrary, colibrary.label, colibrary.getText(true));
									break;
								case COPACKAGE:
									COPackage copackage = new COPackage(e.getX(), e.getY(), color);
									draw.getChildren().addAll(copackage, copackage.getLabel(), copackage.getText(true));
									break;
								case COSCOMPONENT:
									COSComponent coscompnent = new COSComponent(e.getX(), e.getY(), color);
									draw.getChildren().addAll(coscompnent, coscompnent.node1, coscompnent.node2,
											coscompnent.label);
									break;
								case DCOMPONENT:
									DComponent dcomponent = new DComponent(e.getX(), e.getY(), color);
									draw.getChildren().addAll(dcomponent, dcomponent.data, dcomponent.label,
											dcomponent.file, dcomponent.node1, dcomponent.node2);
									break;
								case DDATABASE:
									DDatabase ddatebase = new DDatabase(e.getX(), e.getY(), color);
									draw.getChildren().addAll(ddatebase.shape, ddatebase, ddatebase.data,
											ddatebase.label);
									break;
								case DDEVICE:
									DDeivce ddevice = new DDeivce(e.getX(), e.getY(), color);
									draw.getChildren().addAll(ddevice.shape, ddevice, ddevice.data, ddevice.label);
									break;
								case DFILE:
									DFile dfile = new DFile(e.getX(), e.getY(), color);
									draw.getChildren().addAll(dfile, dfile.data, dfile.label);
									break;
								case DPROTOCAL:
									dprotocal = new DProtocal(e.getX(), e.getY(), e.getX(), e.getY(), color);
									draw.getChildren().addAll(dprotocal);
									menu.isDprotocal = true;
									break;
								case DSOFTWARE:
									DSoftware dsoftware = new DSoftware(e.getX(), e.getY(), color);
									draw.getChildren().addAll(dsoftware.shape, dsoftware, dsoftware.data,
											dsoftware.label);
									break;
								case DSYSTEM:
									DSystem dsystem = new DSystem(e.getX(), e.getY(), color);
									draw.getChildren().addAll(dsystem, dsystem.label);
									break;
								default:
									break;

								}
								draw.setCTool(Tool.POINTER);

							}
							if (scene.getCursor() == Cursor.HAND) {

								if (obj instanceof UCProcess) {
									UCProcess ucprocess = (UCProcess) obj;
									ucprocess.setFill(color);
								} else if (obj instanceof UCActor) {
									UCActor actor = (UCActor) obj;
									actor.setFill(color);
								} else if (obj instanceof UCBoundary) {
									UCBoundary boundary = (UCBoundary) obj;
									boundary.setFill(color);
								} else if (obj instanceof SERole) {
									SERole role = (SERole) obj;
									role.setFill(color);
								}

							}

							if (scene.getCursor() == Cursor.CROSSHAIR) {
								System.out.println("Check Delete");
								if (obj instanceof GLabel) {
									GLabel label = (GLabel) obj;
									draw.getArea().getChildren().removeAll(label, label.getText(false),
											label.getTool(false));
									draw.objects.remove(label);
								} else if (obj instanceof UCProcess) {
									UCProcess ucprocess = (UCProcess) obj;
									draw.getArea().getChildren().removeAll(ucprocess, ucprocess.getLabel(),
											ucprocess.getText(false));
									draw.objects.remove(ucprocess);
								} else if (obj instanceof UCActor) {
									UCActor actor = (UCActor) obj;
									draw.getArea().getChildren().removeAll(actor, actor.getBody(), actor.getLeg(),
											actor.getLeg2(), actor.getLeg3(), actor.getLeg4(), actor.getLabel(),
											actor.getText(false));
									draw.objects.remove(actor);
								} else if (obj instanceof UCBoundary) {
									UCBoundary boundary = (UCBoundary) obj;
									draw.getArea().getChildren().removeAll(boundary);
									draw.objects.remove(boundary);
								} else {
									// For Line
									Point2D point = new Point2D(e.getX(), e.getY());
									checkLineForDelete(point);
								}

							}

							scene.setCursor(Cursor.DEFAULT);
						}
					});

					draw.getArea().addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							scene.setCursor(Cursor.MOVE);
							if (menu.isUCRelation) {
								ucrelation.setEndX(e.getX());
								ucrelation.setEndY(e.getY());
							} else if (menu.isUCGeneral) {
								ucgeneral.setEndX(e.getX());
								ucgeneral.setEndY(e.getY());
							} else if (menu.isUCInclude) {
								ucinclude.setEndX(e.getX());
								ucinclude.setEndY(e.getY());
							} else if (menu.isUCExtend) {
								ucextend.setEndX(e.getX());
								ucextend.setEndY(e.getY());
							} else if (menu.isActivation) {
								seactivation.setEndX(e.getX());
								seactivation.setEndY(e.getY());
							} else if (menu.isNActivation) {
								senactivation.setEndX(e.getX());
								senactivation.setEndY(e.getY());
							} else if (menu.isDActivation) {
								sedactivation.setEndX(e.getX());
								sedactivation.setEndY(e.getY());
							} else if (menu.isCDepend) {
								cdepen.setEndX(e.getX());
								cdepen.setEndY(e.getY());
							} else if (menu.isRegionDraw) {
								if (e.getX() > region.getX() + region.getWidth())
									region.setWidth(region.getWidth() + 4);
								else if (e.getX() < region.getX() + region.getWidth())
									region.setWidth(region.getWidth() - 4);

								if (e.getY() > region.getY() + region.getHeight())
									region.setHeight(region.getHeight() + 4);
								else if (e.getY() < region.getY() + region.getHeight())
									region.setHeight(region.getHeight() - 4);

							} else if (menu.isAEdge) {
								aedge.setEndX(e.getX());
								aedge.setEndY(e.getY());
							} else if (menu.isCODepend) {
								codepend.setEndX(e.getX());
								codepend.setEndY(e.getY());
							} else if (menu.isDprotocal) {
								dprotocal.setEndX(e.getX());
								dprotocal.setEndY(e.getY());
							}
						}
					});

					draw.getArea().addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							scene.setCursor(Cursor.DEFAULT);
							if (menu.isUCRelation) {
								ucrelation.setEndX(e.getX());
								ucrelation.setEndY(e.getY());
								draw.getArea().getChildren().addAll(ucrelation.snode, ucrelation.enode,
										ucrelation.mnode);
								draw.objects.add(ucrelation);
								menu.isUCRelation = false;
								ucrelation = null;
							} else if (menu.isUCGeneral) {
								ucgeneral.setEndX(e.getX());
								ucgeneral.setEndY(e.getY());
								ucgeneral.calculateTri();
								draw.getArea().getChildren().addAll(ucgeneral.getSnode(), ucgeneral.getEnode(),
										ucgeneral.getTri());
								draw.objects.add(ucgeneral);
								menu.isUCGeneral = false;
								ucgeneral = null;
							} else if (menu.isUCInclude) {
								ucinclude.setEndX(e.getX());
								ucinclude.setEndY(e.getY());
								ucinclude.update();
								draw.getArea().getChildren().addAll(ucinclude.getSnode(), ucinclude.getEnode(),
										ucinclude.top, ucinclude.label);
								draw.objects.add(ucinclude);
								menu.isUCInclude = false;
								ucinclude = null;
							} else if (menu.isUCExtend) {
								ucextend.setEndX(e.getX());
								ucextend.setEndY(e.getY());
								ucextend.update();
								draw.getArea().getChildren().addAll(ucextend.getSnode(), ucextend.getEnode(),
										ucextend.top, ucextend.label);
								draw.objects.add(ucextend);
								menu.isUCExtend = false;
								ucextend = null;
							} else if (menu.isActivation) {
								if (seactivation.getStartX() < seactivation.getEndX()) {
									draw.getArea().getChildren().addAll(seactivation.top, seactivation.bot,
											seactivation.activate, seactivation.rLine, seactivation.rtop,
											seactivation.rbot, seactivation.msg, seactivation.snode,
											seactivation.getText(false));
									seactivation.snode.toBack();
								}

								menu.isActivation = false;
								seactivation = null;
							} else if (menu.isNActivation) {
								if (senactivation.getStartX() < senactivation.getEndX()) {
									draw.getArea().getChildren().addAll(senactivation.top, senactivation.bot,
											senactivation.newOb, senactivation.nLine, senactivation.label,
											senactivation.lifeB, senactivation.rLine, senactivation.rtop,
											senactivation.rbot, senactivation.getText(false));
									menu.isNActivation = false;
									senactivation = null;
								}
							} else if (menu.isDActivation) {
								if (sedactivation.getStartX() < sedactivation.getEndX()) {
									draw.getArea().getChildren().addAll(sedactivation.top, sedactivation.bot,
											sedactivation.c1, sedactivation.c2, sedactivation.enode);
									sedactivation.enode.toBack();
									menu.isDActivation = false;
									sedactivation = null;
								}
							} else if (menu.isCDepend) {
								if (cdepen.filterLine()) {
									draw.getArea().getChildren().addAll(cdepen.l1, cdepen.l2, cdepen.l3, cdepen.node1,
											cdepen.node2, cdepen.startNode, cdepen.endNode, cdepen.top, cdepen.bot);
								}
								cdepen.l1.toFront();
								cdepen.l2.toFront();
								cdepen.l3.toFront();
								cdepen = null;
								menu.isCDepend = false;
							} else if (menu.isRegionDraw) {
								// region.accessibleHelpProperty()

								VBox priceBox = new VBox();
								Button printB = new Button("P");
								Button closeB = new Button("C");
								priceBox.getChildren().addAll(printB, closeB);
								priceBox.layoutXProperty().bind(region.xProperty().add(region.getWidth()));
								priceBox.layoutYProperty()
										.bind(region.yProperty().add(region.getHeight()).subtract(50));
								draw.getArea().getChildren().addAll(priceBox);

								container.setDisable(false);
								menu.isRegionDraw = false;

								printB.setOnAction(e2 -> {
									// draw.getArea().getChildren().removeAll(priceBox);
									String picName = null;
									try {
										SnapshotParameters parameters = new SnapshotParameters();
										Rectangle2D toPaint = new Rectangle2D(region.getX(), region.getY(),
												region.getWidth(), region.getHeight());
										parameters.setViewport(toPaint);
										picName = "" + System.currentTimeMillis();
										WritableImage snapshot = draw.getArea().snapshot(parameters, null);
										File output = new File("ImgTemp/" + picName + ".png");
										ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output);
									} catch (IOException ex) {

									}

									// regionPrint();

									// Show Preview Print
									BPrintPreview preview = new BPrintPreview(stage, picName);
									preview.sizeToScene();
									container.setDisable(true);
									preview.setAlwaysOnTop(true);
									preview.showAndWait();
									draw.getArea().getChildren().removeAll(priceBox, region);
									container.setDisable(false);
									region = null;
								});
								closeB.setOnAction(e2 -> {
									draw.getArea().getChildren().removeAll(priceBox, region);
									region = null;
								});
							} else if (menu.isAEdge) {
								aedge.setEndX(e.getX());
								aedge.setEndY(e.getY());
								aedge.filterLine();
								draw.getArea().getChildren().addAll(aedge.l1, aedge.l2, aedge.l3);
								aedge = null;
								menu.isAEdge = false;
							} else if (menu.isCODepend) {
								codepend.setEndX(e.getX());
								codepend.setEndY(e.getY());
								codepend.recalculatePoint();
								draw.getArea().getChildren().addAll(codepend.top, codepend.bot);
								codepend = null;
								menu.isCODepend = false;
							} else if (menu.isDprotocal) {
								dprotocal.setEndX(e.getX());
								dprotocal.setEndY(e.getY());
								dprotocal.filterLine();
								draw.getArea().getChildren().addAll(dprotocal.l1, dprotocal.l2, dprotocal.l3);
								dprotocal = null;
								menu.isDprotocal = false;
							}
						}
					});
				}
			}
		});

		// Menus Function////////////
		menu.nFile.setOnAction(e -> {
			BNewDiagram box = new BNewDiagram(stage);
			box.sizeToScene();
			setBackgorund();
			box.setAlwaysOnTop(true);
			box.showAndWait();
			if (box.condition.equals("finish")) {
				container.setCenter(tabPane);
				try {
					addNewTab(box.getFileName(), box.folder, box.diagram);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			setForeground();
		});

		menu.oProject.setOnAction(e -> {
			BOpen box = new BOpen(stage);
			box.sizeToScene();
			container.setDisable(true);
			box.setAlwaysOnTop(true);
			box.showAndWait();
			if (box.condition.equals("open")) {
				container.setCenter(tabPane);
				addOldTab(box.files, box.projectName);
			}
			container.setDisable(false);
		});

		menu.nProject.setOnAction(e -> {
			BNewProject box = new BNewProject(stage);
			box.sizeToScene();
			container.setDisable(true);
			box.setAlwaysOnTop(true);
			box.showAndWait();

			if (box.getValue().equals("finish")) {
				File file = null;
				switch (osType) {
				case "Unix":
					file = new File("Diagrams/" + box.nameF.getText().toString().trim());
					if (!file.exists()) {
						file.mkdir();
						System.out.println("Folder @" + box.nameF.getText().toString() + " is created!");
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setHeaderText("Project @" + box.nameF.getText().toString() + " creation is success.");
						alert.showAndWait();
					} else {
						System.out.println("Folder already exists.");
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Alert!");
						alert.setHeaderText("Project Folder is Alreay exists.");
						alert.setContentText("Ooops, that is error");
						alert.showAndWait();
						openNewProjectBox();
					}
					break;
				case "Windows":
					// Not Support!
					break;
				}
			}
			container.setDisable(false);
		});

		menu.export.setOnAction(e -> {
			BExport box = new BExport(stage);
			box.sizeToScene();
			container.setDisable(true);
			box.setAlwaysOnTop(true);
			box.showAndWait();
			if (box.condition.equals("finish")) {

			}
			container.setDisable(false);
		});

		menu.importD.setOnAction(e -> {
			iFile = menu.fileChoose.showOpenDialog(stage);
			String format = iFile.getName().substring(iFile.getName().indexOf(".") + 1, iFile.getName().length());
			String name = iFile.getName().substring(0, iFile.getName().indexOf("."));
			System.out.println(" Name :" + name);
			if (format.equals("uml")) {
				if (!isPacked(name)) {
					Pack pack = new Pack();
					pack.doUnPack(iFile.getPath(), name);
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Project Package Already Exists");
					alert.setContentText("Rename project package .");
					alert.show();
				}

			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Fomart Error!");
				alert.setHeaderText("UML Diagram");
				alert.setContentText("Open supported UML Package");
				alert.show();
			}
		});

		menu.save.setOnAction(e -> {
			if (!tabPane.getSelectionModel().isEmpty()) {
				Draw draw = (Draw) tabPane.getSelectionModel().getSelectedItem().getContent();
				String file = tabPane.getSelectionModel().getSelectedItem().getText();
				switch (draw.diagram) {
				case 1: // UseCase
					try {
						UCXml xml = new UCXml("Diagrams/" + draw.projectName + "/" + file + ".xml");
						xml.add(draw.objects);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				}
			}

		});

		menu.saveB.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				menu.save.fire();
			}
		});

	}

	public void initState() throws IOException {
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
		OS os = new OS();
		if (os.isWindows()) {
			osType = "Windows";
		} else if (os.isUnix()) {
			osType = "Unix";
		}
		System.out.println("OS Type :" + osType);

		folder = new File("Diagrams");
		if (!folder.exists()) {
			folder.mkdir();
			System.out.println("Diagram Folder is created");
		} else {
			System.out.println("Diagram Folder is already exists");
		}

		tempPIC = new File("ImgTemp");
		if (!tempPIC.exists()) {
			tempPIC.mkdir();
			System.out.println("Temp Photo Folder is created");
		} else {
			System.out.println("Temp Photo Folder is already exists");
		}

		System.out.println("Copy.xml File Loading...");
		copyxml = new CopyXML();

		container.setCenter(menu.home);

		printer = Printer.getDefaultPrinter();
		pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM);
	
		tabSelect = tabPane.getSelectionModel();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	public void addNewTab(String name, String folder, int diagram) throws IOException {
		Tab tab = new Tab();
		// Select Folder
		File project = new File("Diagrams/" + folder);
		if (project.exists() && project.isDirectory()) {
			System.out.println("Folder @" + folder + " is exists and Directory");
			// Create File For Diagram
			File file = null;
			switch (osType) {
			case "Unix":
				file = new File(project.getPath() + "/" + name + ".xml");
				if (!file.exists()) {
					if (file.createNewFile()) {
						System.out.println("File @" + name + " is created!");
						draw = new Draw(scene, diagram, folder);
						tab.setContent(draw);
						tab.setText(name);
						tabPane.getTabs().add(tab);
						tabSelect.select(tab);
					}
				} else {
					System.out.println("File already exists.");
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Alert!");
					alert.setHeaderText("File Alreay exists.");
					alert.setContentText("Ooops, there was an error!");
					alert.showAndWait();
					menu.nFile.fire();
				}
				break;
			case "Windows":
				// Not Support!
				break;
			}
		} else {
			System.out.println("Folder @" + folder + " is missing.");
		}
	}

	public void addOldTab(ArrayList<String> files, String folder) {
		for (int i = 0; i < files.size(); i++) {
			String name = files.get(i).substring(0, files.get(i).indexOf("."));
			File file = new File("Diagrams/" + folder + "/" + name + ".xml");
			Tab tab = new Tab();
			switch (osType) {
			case "Unix":
				if (file.exists()) {
					menu.dbFactory = DocumentBuilderFactory.newInstance();
					try {
						menu.dBuilder = menu.dbFactory.newDocumentBuilder();
						menu.doc = menu.dBuilder.parse(file);
						String diagram = menu.doc.getElementsByTagName("Document").item(0).getAttributes().item(0)
								.getNodeValue();
						System.out.println("Diagram " + diagram);
						switch (diagram) {
						case "UseCase":
							draw = new Draw(scene, 1, folder);
							// load data here!
							openOldTabUseCase(draw, file);
							break;
						}
						tab.setContent(draw);
						tab.setText(name);
						tabPane.getTabs().add(tab);
					} catch (Exception e) {

					}
				}
				break;
			case "Windows":
				// Not Support!
				break;
			}
		}

	}

	public void clean() {
		for (File file : tempPIC.listFiles()) {
			file.delete();
		}
	}

	public void closeAllTab() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Save!");
		alert.setHeaderText("Save before close diagram");
		alert.showAndWait();
		List<Tab> tabs = tabPane.getTabs();
		tabPane.getTabs().removeAll(tabs);
	}

	public void openNewProjectBox() {
		menu.nProject.fire();
	}

	public boolean isPacked(String name) {
		boolean ispacked = false;
		File file = new File("Diagrams/" + name);
		if (file.exists()) {
			ispacked = true;
		}
		return ispacked;
	}

	public void setBackgorund() {
		container.getChildren().removeAll();
		container.setEffect(menu.bbox);
		container.setDisable(true);
	}

	public void setForeground() {
		container.setEffect(null);
		container.setDisable(false);
	}

	public void openOldTabUseCase(Draw draw, File file) {
		System.out.println("Draw : " + draw);
		System.out.println("File Path : " + file.getPath());
		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;
		Document document;
		dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			document = dBuilder.parse(file);
			NodeList nList = document.getDocumentElement().getChildNodes();
			System.out.println(" Size " + nList.getLength());
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node diagram = nList.item(temp);
				String att = diagram.getAttributes().item(0).getNodeValue();

				if (att.equals("UCProcess")) {
					NodeList nodes = diagram.getChildNodes();
					Node nNode = nodes.item(0); //
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						double x = Double.parseDouble(nodes.item(0).getTextContent());
						double y = Double.parseDouble(nodes.item(1).getTextContent());
						double xr = Double.parseDouble(nodes.item(2).getTextContent());
						double yr = Double.parseDouble(nodes.item(3).getTextContent());
						String label = nodes.item(4).getTextContent();
						String color = nodes.item(5).getTextContent();
						UCProcess process = new UCProcess(stage, x, y, Color.web(color));
						process.setRadiusX(xr);
						process.setRadiusY(yr);
						process.data.set(label);
						process.label.layoutXProperty().bind(process.centerXProperty()
								.subtract(process.label.layoutBoundsProperty().getValue().getWidth() / 2));
						draw.getArea().getChildren().addAll(process, process.getLabel(), process.getText(false));
						draw.objects.add(process);
					}
				}
				if (att.equals("UCActor")) {
					NodeList nodes = diagram.getChildNodes();
					Node nNode = nodes.item(0);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						double x = Double.parseDouble(nodes.item(0).getTextContent());
						double y = Double.parseDouble(nodes.item(1).getTextContent());
						String label = nodes.item(2).getTextContent();
						String color = nodes.item(3).getTextContent();
						UCActor actor = new UCActor(stage, x, y, Color.web(color));
						actor.data.set(label);
						draw.getArea().getChildren().addAll(actor, actor.getBody(), actor.getLeg(), actor.getLeg2(),
								actor.getLeg3(), actor.getLeg4(), actor.getLabel(), actor.getText(false));
						draw.objects.add(actor);
					}
				}

				if (att.equals("UCBoundary")) {
					NodeList nodes = diagram.getChildNodes();
					Node nNode = nodes.item(0);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						double x = Double.parseDouble(nodes.item(0).getTextContent());
						double y = Double.parseDouble(nodes.item(1).getTextContent());
						double w = Double.parseDouble(nodes.item(2).getTextContent());
						double h = Double.parseDouble(nodes.item(3).getTextContent());
						String label = nodes.item(4).getTextContent();
						String color = nodes.item(5).getTextContent();

						UCBoundary boundary = new UCBoundary(stage, x, y, Color.web(color));
						boundary.setWidth(w);
						boundary.setHeight(h);
						boundary.data.set(label);
						draw.getArea().getChildren().addAll(boundary, boundary.resizeVB, boundary.resizeHB);
						boundary.toBack();
						draw.objects.add(boundary);
					}
				}
				if (att.equals("UCExtend")) {
					NodeList nodes = diagram.getChildNodes();
					Node nNode = nodes.item(0);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						double sx = Double.parseDouble(nodes.item(0).getTextContent());
						double sy = Double.parseDouble(nodes.item(1).getTextContent());
						double ex = Double.parseDouble(nodes.item(2).getTextContent());
						double ey = Double.parseDouble(nodes.item(3).getTextContent());
						double lx = Double.parseDouble(nodes.item(4).getTextContent());
						double ly = Double.parseDouble(nodes.item(5).getTextContent());
						UCExtend extend = new UCExtend(sx, sy, ex, ey, Color.BLACK);
						extend.update();
						extend.label.setX(lx);
						extend.label.setY(ly);
						draw.getArea().getChildren().addAll(extend, extend.getSnode(), extend.getEnode(), extend.top,
								extend.label);
						draw.objects.add(extend);
					}
				}

				if (att.equals("UCInclude")) {
					NodeList nodes = diagram.getChildNodes();
					Node nNode = nodes.item(0);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						double sx = Double.parseDouble(nodes.item(0).getTextContent());
						double sy = Double.parseDouble(nodes.item(1).getTextContent());
						double ex = Double.parseDouble(nodes.item(2).getTextContent());
						double ey = Double.parseDouble(nodes.item(3).getTextContent());
						double lx = Double.parseDouble(nodes.item(4).getTextContent());
						double ly = Double.parseDouble(nodes.item(5).getTextContent());
						UCInclude extend = new UCInclude(sx, sy, ex, ey, Color.BLACK);
						extend.update();
						extend.label.setX(lx);
						extend.label.setY(ly);
						draw.getArea().getChildren().addAll(extend, extend.getSnode(), extend.getEnode(), extend.top,
								extend.label);
						draw.objects.add(extend);
					}
				}

				if (att.equals("UCGeneral")) {
					NodeList nodes = diagram.getChildNodes();
					Node nNode = nodes.item(0);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						double sx = Double.parseDouble(nodes.item(0).getTextContent());
						double sy = Double.parseDouble(nodes.item(1).getTextContent());
						double ex = Double.parseDouble(nodes.item(2).getTextContent());
						double ey = Double.parseDouble(nodes.item(3).getTextContent());
						String color = nodes.item(4).getTextContent();

						UCGeneral general = new UCGeneral(sx, sy, ex, ey, Color.web(color));
						general.calculateTri();
						draw.getArea().getChildren().addAll(general.getSnode(), general.getEnode(), general,
								general.tri);
						draw.objects.add(general);
					}
				}

				if (att.equals("UCRelation")) {
					NodeList nodes = diagram.getChildNodes();
					Node nNode = nodes.item(0);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						double sx = Double.parseDouble(nodes.item(0).getTextContent());
						double sy = Double.parseDouble(nodes.item(1).getTextContent());
						double ex = Double.parseDouble(nodes.item(2).getTextContent());
						double ey = Double.parseDouble(nodes.item(3).getTextContent());
						UCRelation relation = new UCRelation(stage, sx, sy, ex, ey);
						draw.getArea().getChildren().addAll(relation);
						draw.objects.add(relation);
					}
				}
			}

		} catch (Exception parserException) {
			parserException.printStackTrace();
		}
	}

	public void checkLineForDelete(Point2D point) {
		switch (draw.diagram) {
		case 1: // Use Case
			for (int i = 0; i < draw.objects.size(); i++) {

				if (draw.objects.get(i) instanceof UCRelation) {
					if (isClicked(draw.objects.get(i), point)) {
						UCRelation r = (UCRelation) draw.objects.get(i);
						draw.getArea().getChildren().removeAll(r, r.snode, r.enode, r.mnode);
						draw.objects.remove(r);
					}
				} else if (draw.objects.get(i) instanceof UCInclude) {
					if (isClicked(draw.objects.get(i), point)) {
						UCInclude r = (UCInclude) draw.objects.get(i);
						draw.getArea().getChildren().removeAll(r, r.getSnode(), r.getEnode(), r.top, r.label);
						draw.objects.remove(r);
					}
				} else if (draw.objects.get(i) instanceof UCGeneral) {
					if (isClicked(draw.objects.get(i), point)) {
						UCGeneral r = (UCGeneral) draw.objects.get(i);
						draw.getArea().getChildren().removeAll(r, r.getSnode(), r.getEnode(), r.getTri());
						draw.objects.remove(r);
					}
				} else if (draw.objects.get(i) instanceof UCExtend) {
					if (isClicked(draw.objects.get(i), point)) {
						UCExtend r = (UCExtend) draw.objects.get(i);
						draw.getArea().getChildren().removeAll(r, r.getSnode(), r.getEnode(), r.top, r.label);
						draw.objects.remove(r);
					}
				}
			}
			break;
		}
	}

	public boolean isClicked(Object l, Point2D point) {
		Line line = (Line) l;
		boolean isclick = line.contains(point.getX(), point.getY()) || line.contains(point.getX() - 1, point.getY())
				|| line.contains(point.getX() - 2, point.getY()) || line.contains(point.getX() - 3, point.getY())
				|| line.contains(point.getX() - 4, point.getY()) || line.contains(point.getX() - 5, point.getY())
				|| line.contains(point.getX() + 1, point.getY()) || line.contains(point.getX() + 2, point.getY())
				|| line.contains(point.getX() + 3, point.getY()) || line.contains(point.getX() + 4, point.getY())
				|| line.contains(point.getX() + 5, point.getY()) || line.contains(point.getX(), point.getY() - 1)
				|| line.contains(point.getX(), point.getY() - 2) || line.contains(point.getX(), point.getY() - 3)
				|| line.contains(point.getX(), point.getY() - 4) || line.contains(point.getX(), point.getY() - 5)
				|| line.contains(point.getX(), point.getY() + 1) || line.contains(point.getX(), point.getY() + 2)
				|| line.contains(point.getX(), point.getY() + 3) || line.contains(point.getX(), point.getY() + 4)
				|| line.contains(point.getX(), point.getY() + 5);

		return isclick;
	}
}
