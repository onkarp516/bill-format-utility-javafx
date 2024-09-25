package com.example.demo1;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.*;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class HelloApplication extends Application {
    private double dragStartX;
    private double dragStartY;
    private boolean dragging = false;
    private Rectangle highlightArea = new Rectangle(); // For visual feedback
    private Map<String, Shape> shapeMap = new HashMap<>();
    private Stack<Shape> undoHistory = new Stack<>();
    // Define a color for the background
    private final Color BACKGROUND_COLOR = Color.WHITE;
    private static final int GRID_SIZE = 20;
    private boolean linesVisible = false;  // To track the visibility of lines
    private static final double DPI = 96; // or your chosen DPI

    private static final double[][] PAGE_SIZES = {
            {841 * DPI / 25.4, 1189 * DPI / 25.4}, // A0
            {594 * DPI / 25.4, 841 * DPI / 25.4},  // A1
            {420 * DPI / 25.4, 594 * DPI / 25.4},  // A2
            {297 * DPI / 25.4, 420 * DPI / 25.4},  // A3
            {210 * DPI / 25.4, 297 * DPI / 25.4},  // A4
            {148 * DPI / 25.4, 210 * DPI / 25.4},  // A5
            {105 * DPI / 25.4, 148 * DPI / 25.4},  // A6
            {74 * DPI / 25.4, 105 * DPI / 25.4},   // A7
            {52 * DPI / 25.4, 74 * DPI / 25.4},    // A8
            {37 * DPI / 25.4, 52 * DPI / 25.4},    // A9
            {26 * DPI / 25.4, 37 * DPI / 25.4}     // A10
    };
    // Variable to store the last selected border style
    String lastSelectedBorderStyle = "None"; // Default value
    @Override
    public void start(Stage primaryStage) {

        Stack<Shape> undoHistory = new Stack<>();
        Stack<Shape> redoHistory = new Stack<>();
        primaryStage.setMaximized(true);

        // Header Panel
        HBox header = new HBox();
        header.setPadding(new Insets(10));
//        header.setStyle("-fx-background-color: #336699;");
//        Label headerLabel = new Label("Drawing Application");
//        headerLabel.setTextFill(Color.WHITE);
//        headerLabel.setFont(new Font("Arial", 24));

        // Page Setup Menu
        MenuBar menuBar = new MenuBar();
        Menu pageSetupMenu = new Menu("Insert");
        MenuItem setupMenuItem = new MenuItem("Page Setup");
        MenuItem setupTable = new MenuItem("Table");
        pageSetupMenu.getItems().add(setupMenuItem);
        pageSetupMenu.getItems().add(setupTable);
        menuBar.getMenus().add(pageSetupMenu);
        header.getChildren().addAll(menuBar);

        // Toggle buttons for drawing tools
        ToggleButton drawbtn = new ToggleButton("Draw");
        ToggleButton rubberbtn = new ToggleButton("Rubber");
        ToggleButton linebtn = new ToggleButton("Line");
        ToggleButton rectbtn = new ToggleButton("Rectangle");
        ToggleButton circlebtn = new ToggleButton("Circle");
        ToggleButton elpslebtn = new ToggleButton("Ellipse");
        ToggleButton textbtn = new ToggleButton("Text");


        ToggleButton[] toolsArr = {drawbtn, rubberbtn, linebtn, rectbtn, circlebtn, elpslebtn, textbtn};

        ToggleGroup tools = new ToggleGroup();

        for (ToggleButton tool : toolsArr) {
            tool.setMinWidth(90);
            tool.setToggleGroup(tools);
            tool.setCursor(Cursor.HAND);
        }

        ColorPicker cpLine = new ColorPicker(Color.BLACK);
        ColorPicker cpFill = new ColorPicker(Color.TRANSPARENT);

        TextArea text = new TextArea();
        text.setPrefRowCount(1);

        Slider slider = new Slider(1, 50, 3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);

        TextField ip = new TextField("Enter Text");
        Label line_color = new Label("Line Color");
        Label fill_color = new Label("Fill Color");
        Label line_width = new Label("3.0");

        Button undo = new Button("Undo");
        Button redo = new Button("Redo");
        Button save = new Button("Save");
        Button open = new Button("Open");
        Button export = new Button("Export To Java");
        Button exportToHtml = new Button("Export To HTML");
        // Create a shape, e.g., a Circle
        // Create a VBox to hold the Save button and the shape
        VBox saveContainer = new VBox(10); // 10 is the spacing between elements

        Circle shapeBelowSave = new Circle(50, Color.LIGHTBLUE); // Circle with radius 50 and light blue color
        // Create a movable square shape
        Rectangle square = new Rectangle(100, 100, Color.LIGHTBLUE); // 100x100 square with light blue color
        square.setOnMousePressed(e -> {
            // Start dragging
            square.setOnMouseDragged(dragEvent -> {
                // Move the square with the mouse
                square.setX(dragEvent.getX() - square.getWidth() / 2);
                square.setY(dragEvent.getY() - square.getHeight() / 2);
            });
        });

        // Create a line shape
        Line hline = new Line(0, 0, 100, 0); // Line with length of 100 pixels
        hline.setStroke(Color.BLACK); // Set the color of the line
        // Add Save button and shape to the VBox
        saveContainer.getChildren().addAll( shapeBelowSave,hline,square);

// Add saveContainer to the existing layout
//        btns.getChildren().add(saveContainer);

        Button[] basicArr = {undo, redo, save, open,export,exportToHtml};

        for (Button btn : basicArr) {
            btn.setMinWidth(90);
            btn.setCursor(Cursor.HAND);
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: #666;");
        }
        save.setStyle("-fx-background-color: #80334d;");
        open.setStyle("-fx-background-color: #80334d;");
        export.setStyle("-fx-background-color: #80334d;");
        exportToHtml.setStyle("-fx-background-color: #80334d;");

        ComboBox<String> pageSizeComboBox = new ComboBox<>();
        for (int i = 0; i <= 10; i++) {
            pageSizeComboBox.getItems().add("A" + i);
        }
        pageSizeComboBox.setValue("A4"); // Default value

        ComboBox<String> pageLayoutComboBox = new ComboBox<>();
        pageLayoutComboBox.getItems().addAll("Portrait", "Landscape");
        pageLayoutComboBox.setValue("Portrait"); // Default value

        //border
        ComboBox<String> borderStyleComboBox = new ComboBox<>();
        borderStyleComboBox.getItems().addAll("None", "Solid", "Dashed", "Dotted");
        borderStyleComboBox.setValue("None"); // Default value

        VBox btns = new VBox(10);
        btns.getChildren().addAll(drawbtn, rubberbtn, linebtn, rectbtn, circlebtn, elpslebtn,
                textbtn, text, ip,line_color, cpLine, fill_color, cpFill, line_width, slider,
                pageSizeComboBox, pageLayoutComboBox,borderStyleComboBox, undo, redo, open, save,export,exportToHtml,saveContainer);
        btns.setPadding(new Insets(5));
        btns.setStyle("-fx-background-color: #999");
        btns.setPrefWidth(100);

        highlightArea.setFill(Color.LIGHTGRAY);
        highlightArea.setStroke(Color.BLACK);
        highlightArea.setStrokeWidth(1);
        highlightArea.setVisible(false); // Initially hidden


        // Drawing Canvas
        Canvas canvas = new Canvas(PAGE_SIZES[4][0], PAGE_SIZES[4][1]); // Default to A4 size
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineWidth(1);
        // Create and add ruler
        HBox ruler = createRuler(canvas.getWidth());
        // Create a horizontal line
        Line horizontalLine = new Line();
        horizontalLine.setStartX(0);
        horizontalLine.setEndX(800); // Adjust this to match the width of your ruler/canvas
        horizontalLine.setStroke(Color.BLACK); // Set the color of the line

        //  ***********************************************************HEADER and FOOTER START ***********************************************************
        // Draw the initial page background (white background)
        drawPageBackground(gc, canvas.getWidth(), canvas.getHeight());

        // Set up the mouse click event handler
        canvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                // Draw header and footer lines on double-click
                drawFooterLine(gc, canvas.getWidth(), canvas.getHeight());
                drawHeaderLine(gc, canvas.getWidth());
                linesVisible = true;  // Mark the lines as visible
            }
//            else if (event.getClickCount() == 1 && linesVisible) {
//                // Clear header and footer lines on single-click
//                clearLines(gc, canvas.getWidth(), canvas.getHeight());
//                linesVisible = false;  // Mark the lines as invisible
//            }
        });
        // Drag and Drop for TextField
        ip.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();
                dragging = true;
                ip.setCursor(Cursor.CLOSED_HAND);
            }
        });

        ip.setOnMouseDragged(event -> {
            if (dragging) {
                double deltaX = event.getSceneX() - dragStartX;
                double deltaY = event.getSceneY() - dragStartY;
                ip.setLayoutX(ip.getLayoutX() + deltaX);
                ip.setLayoutY(ip.getLayoutY() + deltaY);
                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();
            }
        });

        ip.setOnMouseReleased(event -> {
            dragging = false;
            ip.setCursor(Cursor.HAND);
            highlightArea.setVisible(false);
        });

        // Handle dragging over the Canvas
        canvas.setOnDragOver(event -> {
            if (event.getGestureSource() != canvas && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                double x = event.getX();
                double y = event.getY();
                highlightArea.setX(x - 50); // Adjust size as needed
                highlightArea.setY(y - 15); // Adjust size as needed
                highlightArea.setWidth(100); // Size of the TextField
                highlightArea.setHeight(30); // Size of the TextField
                highlightArea.setVisible(true);
            }
            event.consume();
        });

        // Handle drop on the Canvas
        canvas.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                // Place the TextField on the canvas at the drop location
                double x = event.getX() - 50; // Adjust to center the TextField
                double y = event.getY() - 15; // Adjust to center the TextField
                ip.setLayoutX(x);
                ip.setLayoutY(y);

                success = true;
            }
            event.setDropCompleted(success);
            highlightArea.setVisible(false); // Hide the highlight area
            event.consume();
        });

//  ***********************************************************HEADER and FOOTER END ***********************************************************
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-background-color: blue;");
        canvasContainer.setPadding(new Insets(20));
        // StackPane to contain both ruler and canvas
        VBox canvasAndRuler = new VBox();
        canvasAndRuler.setAlignment(Pos.CENTER); // Center the ruler and canvas
        canvasAndRuler.getChildren().addAll(ruler,horizontalLine, canvas);
//        canvasAndRuler.setStyle("-fx-background-color: grey;");
        canvasAndRuler.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(canvasAndRuler);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Line line = new Line();
        Rectangle rect = new Rectangle();
        Circle circ = new Circle();
        Ellipse elps = new Ellipse();

        pageSizeComboBox.setOnAction(e -> {
            updateCanvasSize(canvas, gc, pageSizeComboBox, pageLayoutComboBox,borderStyleComboBox);
        });

        pageLayoutComboBox.setOnAction(e -> {
            updateCanvasSize(canvas, gc, pageSizeComboBox, pageLayoutComboBox,borderStyleComboBox);
        });

        borderStyleComboBox.setOnAction(e->{
            updateCanvasSize(canvas,gc,pageSizeComboBox,pageLayoutComboBox,borderStyleComboBox);
        });

        //  ***********************************************************Id Generation for Shapes START ***********************************************************
//        canvas.setOnMousePressed(e -> {
//            if (drawbtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                gc.beginPath();
//                gc.lineTo(e.getX(), e.getY());
//            } else if (rubberbtn.isSelected()) {
//                double lineWidth = gc.getLineWidth();
//                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
//            } else if (linebtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                line.setStartX(e.getX());
//                line.setStartY(e.getY());
//            } else if (rectbtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                gc.setFill(cpFill.getValue());
//                rect.setX(e.getX());
//                rect.setY(e.getY());
//            } else if (circlebtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                gc.setFill(cpFill.getValue());
//                circ.setCenterX(e.getX());
//                circ.setCenterY(e.getY());
//            } else if (elpslebtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                gc.setFill(cpFill.getValue());
//                elps.setCenterX(e.getX());
//                elps.setCenterY(e.getY());
//            } else if (textbtn.isSelected()) {
//                gc.setLineWidth(1);
//                gc.setFont(Font.font(slider.getValue()));
//                gc.setStroke(cpLine.getValue());
//                gc.setFill(cpFill.getValue());
//                gc.fillText(text.getText(), e.getX(), e.getY());
//                gc.strokeText(text.getText(), e.getX(), e.getY());
//            }
//        });
//
//        canvas.setOnMouseDragged(e -> {
//            if (drawbtn.isSelected()) {
//                gc.lineTo(e.getX(), e.getY());
//                gc.stroke();
//            } else if (rubberbtn.isSelected()) {
//                double lineWidth = gc.getLineWidth();
//                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
//            }
//        });
//
//        canvas.setOnMouseReleased(e -> {
//            if (drawbtn.isSelected()) {
//                gc.lineTo(e.getX(), e.getY());
//                gc.stroke();
//                gc.closePath();
//            } else if (rubberbtn.isSelected()) {
//                double lineWidth = gc.getLineWidth();
//                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
//            } else if (linebtn.isSelected()) {
//                line.setEndX(e.getX());
//                line.setEndY(e.getY());
//                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
//                undoHistory.push(new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()));
//
//                String uniqueID = UUID.randomUUID().toString();
//                shapeMap.put(uniqueID, new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()));
//                System.out.println("Line ID: " + uniqueID);
//            } else if (rectbtn.isSelected()) {
//                rect.setWidth(Math.abs((e.getX() - rect.getX())));
//                rect.setHeight(Math.abs((e.getY() - rect.getY())));
//                if (rect.getX() > e.getX()) {
//                    rect.setX(e.getX());
//                }
//                if (rect.getY() > e.getY()) {
//                    rect.setY(e.getY());
//                }
//                gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
//                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
//                undoHistory.push(new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
//
//                String uniqueID = UUID.randomUUID().toString();
//                shapeMap.put(uniqueID, new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
//                System.out.println("Rectangle ID: " + uniqueID);
//            } else if (circlebtn.isSelected()) {
//                circ.setRadius(Math.abs((e.getX() - circ.getCenterX())));
//                if (circ.getCenterX() > e.getX()) {
//                    circ.setCenterX(e.getX());
//                }
//                if (circ.getCenterY() > e.getY()) {
//                    circ.setCenterY(e.getY());
//                }
//                gc.fillOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
//                gc.strokeOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
//                undoHistory.push(new Circle(circ.getCenterX(), circ.getCenterY(), circ.getRadius()));
//
//                String uniqueID = UUID.randomUUID().toString();
//                shapeMap.put(uniqueID, new Circle(circ.getCenterX(), circ.getCenterY(), circ.getRadius()));
//                System.out.println("Circle ID: " + uniqueID);
//            } else if (elpslebtn.isSelected()) {
//                elps.setRadiusX(Math.abs((e.getX() - elps.getCenterX())));
//                elps.setRadiusY(Math.abs((e.getY() - elps.getCenterY())));
//                if (elps.getCenterX() > e.getX()) {
//                    elps.setCenterX(e.getX());
//                }
//                if (elps.getCenterY() > e.getY()) {
//                    elps.setCenterY(e.getY());
//                }
//                gc.fillOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
//                gc.strokeOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
//                undoHistory.push(new Ellipse(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY()));
//
//                String uniqueID = UUID.randomUUID().toString();
//                shapeMap.put(uniqueID, new Ellipse(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY()));
//                System.out.println("Ellipse ID: " + uniqueID);
//            }
//        });
        //  ***********************************************************Id Generation for Shapes END ***********************************************************


        //  ***********************************************************Dynamic ID Generating and Proper Draw Line(Dotted) START ***********************************************************
//        canvas.setOnMousePressed(e -> {
//            Point snappedPoint = snapToGrid(new Point((int) e.getX(), (int) e.getY()));
//            if (drawbtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                gc.beginPath();
//                gc.lineTo(snappedPoint.x, snappedPoint.y);  // Snapped to grid
//            } else if (linebtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                line.setStartX(snappedPoint.x);  // Snapped to grid
//                line.setStartY(snappedPoint.y);  // Snapped to grid
//            } else if (rectbtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                gc.setFill(cpFill.getValue());
//                rect.setX(snappedPoint.x);  // Snapped to grid
//                rect.setY(snappedPoint.y);  // Snapped to grid
//            } else if (circlebtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                gc.setFill(cpFill.getValue());
//                circ.setCenterX(snappedPoint.x);  // Snapped to grid
//                circ.setCenterY(snappedPoint.y);  // Snapped to grid
//            } else if (elpslebtn.isSelected()) {
//                gc.setStroke(cpLine.getValue());
//                gc.setFill(cpFill.getValue());
//                elps.setCenterX(snappedPoint.x);  // Snapped to grid
//                elps.setCenterY(snappedPoint.y);  // Snapped to grid
//            }else if (textbtn.isSelected()) {
//                gc.setLineWidth(1);
//                gc.setFont(Font.font(slider.getValue()));
//                gc.setStroke(cpLine.getValue());
//                gc.setFill(cpFill.getValue());
//                gc.fillText(text.getText(), e.getX(), e.getY());
//                gc.strokeText(text.getText(), e.getX(), e.getY());
//            }
//        });
//
//        canvas.setOnMouseDragged(e -> {
//            Point snappedPoint = snapToGrid(new Point((int) e.getX(), (int) e.getY()));
//            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear canvas to redraw
//            drawShapes(canvas,gc); // Redraw previous shapes from history
//
//            gc.setLineDashes(10); // Set line dash pattern for dotted effect
//
//            if (linebtn.isSelected()) {
//                gc.strokeLine(line.getStartX(), line.getStartY(), snappedPoint.x, snappedPoint.y);
//            } else if (rectbtn.isSelected()) {
//                double width = Math.abs(snappedPoint.x - rect.getX());
//                double height = Math.abs(snappedPoint.y - rect.getY());
//                gc.strokeRect(rect.getX(), rect.getY(), width, height);
//            } else if (circlebtn.isSelected()) {
//                double radius = Math.abs(snappedPoint.x - circ.getCenterX());
//                gc.strokeOval(circ.getCenterX(), circ.getCenterY(), radius, radius);
//            } else if (elpslebtn.isSelected()) {
//                double radiusX = Math.abs(snappedPoint.x - elps.getCenterX());
//                double radiusY = Math.abs(snappedPoint.y - elps.getCenterY());
//                gc.strokeOval(elps.getCenterX(), elps.getCenterY(), radiusX, radiusY);
//            }
//
//            gc.setLineDashes(0); // Reset to solid line
//        });
//
//        canvas.setOnMouseReleased(e -> {
//            Point snappedPoint = snapToGrid(new Point((int) e.getX(), (int) e.getY()));
//            if (drawbtn.isSelected()) {
//                gc.lineTo(snappedPoint.x, snappedPoint.y);  // Snapped to grid
//                gc.stroke();
//                gc.closePath();
//            } else if (linebtn.isSelected()) {
//                line.setEndX(snappedPoint.x);  // Snapped to grid
//                line.setEndY(snappedPoint.y);  // Snapped to grid
//                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
//                undoHistory.push(new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()));
//
//                String uniqueID = UUID.randomUUID().toString();
//                shapeMap.put(uniqueID, new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()));
//                System.out.println("Line ID: " + uniqueID);
//            } else if (rectbtn.isSelected()) {
//                rect.setWidth(Math.abs((snappedPoint.x - rect.getX())));
//                rect.setHeight(Math.abs((snappedPoint.y - rect.getY())));
//                if (rect.getX() > snappedPoint.x) {
//                    rect.setX(snappedPoint.x);
//                }
//                if (rect.getY() > snappedPoint.y) {
//                    rect.setY(snappedPoint.y);
//                }
//                gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
//                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
//                undoHistory.push(new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
//
//                String uniqueID = UUID.randomUUID().toString();
//                shapeMap.put(uniqueID, new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
//                System.out.println("Rectangle ID: " + uniqueID);
//            } else if (circlebtn.isSelected()) {
//                circ.setRadius(Math.abs((snappedPoint.x - circ.getCenterX())));
//                if (circ.getCenterX() > snappedPoint.x) {
//                    circ.setCenterX(snappedPoint.x);
//                }
//                if (circ.getCenterY() > snappedPoint.y) {
//                    circ.setCenterY(snappedPoint.y);
//                }
//                gc.fillOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
//                gc.strokeOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
//                undoHistory.push(new Circle(circ.getCenterX(), circ.getCenterY(), circ.getRadius()));
//
//                String uniqueID = UUID.randomUUID().toString();
//                shapeMap.put(uniqueID, new Circle(circ.getCenterX(), circ.getCenterY(), circ.getRadius()));
//                System.out.println("Circle ID: " + uniqueID);
//            } else if (elpslebtn.isSelected()) {
//                elps.setRadiusX(Math.abs((snappedPoint.x - elps.getCenterX())));
//                elps.setRadiusY(Math.abs((snappedPoint.y - elps.getCenterY())));
//                if (elps.getCenterX() > snappedPoint.x) {
//                    elps.setCenterX(snappedPoint.x);
//                }
//                if (elps.getCenterY() > snappedPoint.y) {
//                    elps.setCenterY(snappedPoint.y);
//                }
//                gc.fillOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
//                gc.strokeOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
//                undoHistory.push(new Ellipse(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY()));
//
//                String uniqueID = UUID.randomUUID().toString();
//                shapeMap.put(uniqueID, new Ellipse(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY()));
//                System.out.println("Ellipse ID: " + uniqueID);
//            }
//        });


        canvas.setOnMousePressed(e -> {
            Point snappedPoint = snapToGrid(new Point((int) e.getX(), (int) e.getY()));
            if (drawbtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.beginPath();
                gc.lineTo(snappedPoint.x, snappedPoint.y);  // Snapped to grid
            } else if (linebtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                line.setStartX(snappedPoint.x);  // Snapped to grid
                line.setStartY(snappedPoint.y);  // Snapped to grid
            } else if (rectbtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                rect.setX(snappedPoint.x);  // Snapped to grid
                rect.setY(snappedPoint.y);  // Snapped to grid
            } else if (circlebtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                circ.setCenterX(snappedPoint.x);  // Snapped to grid
                circ.setCenterY(snappedPoint.y);  // Snapped to grid
            } else if (elpslebtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                elps.setCenterX(snappedPoint.x);  // Snapped to grid
                elps.setCenterY(snappedPoint.y);  // Snapped to grid
            }
            else if (elpslebtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                elps.setCenterX(e.getX());
                elps.setCenterY(e.getY());
            } else if (textbtn.isSelected()) {
                gc.setLineWidth(1);
                gc.setFont(Font.font(slider.getValue()));
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                gc.fillText(text.getText(), e.getX(), e.getY());
                gc.strokeText(text.getText(), e.getX(), e.getY());
            }
        });

        canvas.setOnMouseDragged(e -> {
            Point snappedPoint = snapToGrid(new Point((int) e.getX(), (int) e.getY()));
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear canvas to redraw

            // Fill the canvas with a white background
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Redraw all shapes from history
            for (Shape shape : undoHistory) {
                drawShape(gc, shape); // Helper function to draw shapes from the history
            }

            gc.setLineDashes(10); // Set line dash pattern for dotted effect

            if (linebtn.isSelected()) {
                gc.strokeLine(line.getStartX(), line.getStartY(), snappedPoint.x, snappedPoint.y);
            }else  if (drawbtn.isSelected()) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            } else if (rubberbtn.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
            } else if (rectbtn.isSelected()) {
                double width = Math.abs(snappedPoint.x - rect.getX());
                double height = Math.abs(snappedPoint.y - rect.getY());
                gc.strokeRect(rect.getX(), rect.getY(), width, height);
            } else if (circlebtn.isSelected()) {
                double radius = Math.abs(snappedPoint.x - circ.getCenterX());
                gc.strokeOval(circ.getCenterX(), circ.getCenterY(), radius, radius);
            } else if (elpslebtn.isSelected()) {
                double radiusX = Math.abs(snappedPoint.x - elps.getCenterX());
                double radiusY = Math.abs(snappedPoint.y - elps.getCenterY());
                gc.strokeOval(elps.getCenterX(), elps.getCenterY(), radiusX, radiusY);
            }

            gc.setLineDashes(0); // Reset to solid line

        });

        canvas.setOnMouseReleased(e -> {
            Point snappedPoint = snapToGrid(new Point((int) e.getX(), (int) e.getY()));
            if (drawbtn.isSelected()) {
                gc.lineTo(snappedPoint.x, snappedPoint.y);  // Snapped to grid
                gc.stroke();
                gc.closePath();
            } else if (linebtn.isSelected()) {
                line.setEndX(snappedPoint.x);  // Snapped to grid
                line.setEndY(snappedPoint.y);  // Snapped to grid
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
                Line newLine = new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
                undoHistory.push(newLine);
                addShapeToMap(newLine); // Helper function to add shape to shapeMap with UUID
            } else if (rectbtn.isSelected()) {
                rect.setWidth(Math.abs((snappedPoint.x - rect.getX())));
                rect.setHeight(Math.abs((snappedPoint.y - rect.getY())));
                if (rect.getX() > snappedPoint.x) {
                    rect.setX(snappedPoint.x);
                }
                if (rect.getY() > snappedPoint.y) {
                    rect.setY(snappedPoint.y);
                }
                gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                Rectangle newRect = new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                undoHistory.push(newRect);
                addShapeToMap(newRect);
            } else if (circlebtn.isSelected()) {
                circ.setRadius(Math.abs((snappedPoint.x - circ.getCenterX())));
                if (circ.getCenterX() > snappedPoint.x) {
                    circ.setCenterX(snappedPoint.x);
                }
                if (circ.getCenterY() > snappedPoint.y) {
                    circ.setCenterY(snappedPoint.y);
                }
                gc.fillOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
                gc.strokeOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
                Circle newCircle = new Circle(circ.getCenterX(), circ.getCenterY(), circ.getRadius());
                undoHistory.push(newCircle);
                addShapeToMap(newCircle);
            } else if (elpslebtn.isSelected()) {
                elps.setRadiusX(Math.abs((snappedPoint.x - elps.getCenterX())));
                elps.setRadiusY(Math.abs((snappedPoint.y - elps.getCenterY())));
                if (elps.getCenterX() > snappedPoint.x) {
                    elps.setCenterX(snappedPoint.x);
                }
                if (elps.getCenterY() > snappedPoint.y) {
                    elps.setCenterY(snappedPoint.y);
                }
                gc.fillOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
                gc.strokeOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
                Ellipse newEllipse = new Ellipse(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
                undoHistory.push(newEllipse);
                addShapeToMap(newEllipse);
            }

        });
        //  ***********************************************************Dynamic ID Generating and Proper Draw Line(Dotted) END ***********************************************************

        slider.valueProperty().addListener(e -> {
            double width = slider.getValue();
            line_width.setText(String.format("%.1f", width));
            gc.setLineWidth(width);
        });

        undo.setOnAction(e -> {
            if (!undoHistory.isEmpty()) {
                Shape shape = undoHistory.pop();
                redoHistory.push(shape);
                redrawCanvas(gc, canvas, undoHistory);
            }
        });

        redo.setOnAction(e -> {
            if (!redoHistory.isEmpty()) {
                Shape shape = redoHistory.pop();
                undoHistory.push(shape);
                redrawCanvas(gc, canvas, undoHistory);
            }
        });

        save.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                try {
                    WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                    canvas.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", file);
                } catch (IOException ex) {
                    System.out.println("Error saving image: " + ex.getMessage());
                }
            }
        });

        open.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    InputStream is = new FileInputStream(file);
                    Image image = new Image(is);
                    gc.drawImage(image, 0, 0);
                } catch (IOException ex) {
                    System.out.println("Error opening image: " + ex.getMessage());
                }
            }
        });
        export.setOnAction(e -> exportCanvasToJavaFile(canvas));
        exportToHtml.setOnAction(e-> exportCanvasToHTMLFile(canvas));

        // Add action to the Page Setup menu item
        setupMenuItem.setOnAction(e -> {
            Stage setupStage = new Stage();
            setupStage.setTitle("Page Setup");

            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10));
            gridPane.setVgap(10);
            gridPane.setHgap(10);

            Label widthLabel = new Label("Width:");
            TextField widthField = new TextField(Double.toString(canvas.getWidth()));
            Label heightLabel = new Label("Height:");
            TextField heightField = new TextField(Double.toString(canvas.getHeight()));
            Label borderStyleLabel = new Label("Border Style:");
            ComboBox<String> borderStyleComboBox1 = new ComboBox<>();
            borderStyleComboBox1.getItems().addAll("None", "Solid", "Dashed", "Dotted");
            borderStyleComboBox1.setValue(lastSelectedBorderStyle); // Default value

            Button submitButton = new Button("Submit");

            gridPane.add(widthLabel, 0, 0);
            gridPane.add(widthField, 1, 0);
            gridPane.add(heightLabel, 0, 1);
            gridPane.add(heightField, 1, 1);
            gridPane.add(borderStyleLabel, 0, 2);
            gridPane.add(borderStyleComboBox1, 1, 2);
            gridPane.add(submitButton, 1, 3);

            submitButton.setOnAction(event -> {
                double newWidth = Double.parseDouble(widthField.getText());
                double newHeight = Double.parseDouble(heightField.getText());

                canvas.setWidth(newWidth);
                canvas.setHeight(newHeight);

                // Clear the canvas and redraw the white page
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // Draw the border if a border style is selected
                String borderStyle = borderStyleComboBox1.getValue();
                double borderMargin = 10; // Margin around the border

                if (!borderStyle.equals("None")) {
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2); // Border width
                    switch (borderStyle) {
                        case "Solid":
                            gc.setLineDashes(); // Solid border
                            break;
                        case "Dashed":
                            gc.setLineDashes(10, 10); // Dashed border
                            break;
                        case "Dotted":
                            gc.setLineDashes(2, 5); // Dotted border
                            break;
                    }
                    // Draw the border with a margin of 10px
                    gc.strokeRect(borderMargin, borderMargin, canvas.getWidth() - 2 * borderMargin, canvas.getHeight() - 2 * borderMargin);
                } else {
                    gc.setLineDashes(); // Ensure no dashed line for "None"
                }

// Store the selected border style for the next time the dialog is opened
                lastSelectedBorderStyle = borderStyle;
                setupStage.close();
            });

            Scene scene = new Scene(gridPane, 300, 200);
            setupStage.setScene(scene);
            setupStage.show();
        });
        setupTable.setOnAction(e -> {
            Stage setupStage = new Stage();
            setupStage.setTitle("Table");
            // Form for user input
            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10));
            gridPane.setHgap(10);
            gridPane.setVgap(10);

            TextField rowsField = new TextField("4");
            TextField colsField = new TextField("3");
            TextField cellWidthField = new TextField("100");
            TextField cellHeightField = new TextField("50");
            TextField rowHeightsField = new TextField("50,50,50,50");
            TextField colWidthsField = new TextField("100,100,100");
            TextField colWidthsFromRowField = new TextField("100,100,100");
            TextField startRowForColWidthsField = new TextField("0");
            ColorPicker cellColorPicker = new ColorPicker(Color.WHITE);
            ColorPicker borderColorPicker = new ColorPicker(Color.BLACK);
            Button drawButton = new Button("Draw Table");

            gridPane.add(new Label("Rows:"), 0, 0);
            gridPane.add(rowsField, 1, 0);
            gridPane.add(new Label("Columns:"), 0, 1);
            gridPane.add(colsField, 1, 1);
            gridPane.add(new Label("Cell Width:"), 0, 2);
            gridPane.add(cellWidthField, 1, 2);
            gridPane.add(new Label("Cell Height:"), 0, 3);
            gridPane.add(cellHeightField, 1, 3);
            gridPane.add(new Label("Row Heights (comma-separated):"), 0, 4);
            gridPane.add(rowHeightsField, 1, 4);
            gridPane.add(new Label("Column Widths (comma-separated):"), 0, 5);
            gridPane.add(colWidthsField, 1, 5);
            gridPane.add(new Label("Column Widths from Row (comma-separated):"), 0, 6);
            gridPane.add(colWidthsFromRowField, 1, 6);
            gridPane.add(new Label("Start Row for Column Widths:"), 0, 7);
            gridPane.add(startRowForColWidthsField, 1, 7);
            gridPane.add(new Label("Cell Color:"), 0, 8);
            gridPane.add(cellColorPicker, 1, 8);
            gridPane.add(new Label("Border Color:"), 0, 9);
            gridPane.add(borderColorPicker, 1, 9);
            gridPane.add(drawButton, 1, 10);

            drawButton.setOnAction(event -> {


                // Clear the canvas and redraw the white page
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // Additional logic from drawButton.setOnAction
                int rows = Integer.parseInt(rowsField.getText());
                int cols = Integer.parseInt(colsField.getText());
                double cellWidth = Double.parseDouble(cellWidthField.getText());
                double cellHeight = Double.parseDouble(cellHeightField.getText());
                Color cellColor = cellColorPicker.getValue();
                Color borderColor = borderColorPicker.getValue();

                double[] rowHeights = parseCsvToDoubleArray(rowHeightsField.getText(), rows);
                double[] colWidths = parseCsvToDoubleArray(colWidthsField.getText(), cols);
                double[] colWidthsFromRow = parseCsvToDoubleArray(colWidthsFromRowField.getText(), cols);
                int startRowForColWidths = Integer.parseInt(startRowForColWidthsField.getText());

                drawTable(canvas, gc,50, 50, rows, cols, colWidths, colWidthsFromRow, startRowForColWidths, rowHeights, cellColor, borderColor);

                setupStage.close();
            });

            Scene scene = new Scene(gridPane, 500, 500);
            setupStage.setScene(scene);
            setupStage.show();
        });
        BorderPane layout = new BorderPane();
        layout.setTop(header);
        layout.setLeft(btns);
        layout.setCenter(scrollPane);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setTitle("Bill Format");
        primaryStage.setScene(scene);
        primaryStage.show();
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newWidth = newVal.doubleValue();
            ruler.setPrefWidth(newWidth);
            ruler.setPrefHeight(500.0);
            HBox newRuler = createRuler(newWidth); // Create a new ruler with updated width
            canvasAndRuler.getChildren().set(0, newRuler); // Replace old ruler with new one
        });
        //ruller done
        addUndoRedoShortcuts(scene, undoHistory, redoHistory, canvas, gc);

    }
    // ****************************************************************************************Shortcut Keys CTRL+Z START***************************************************************************************
// Add this method to handle both undo and redo shortcuts
    private void addUndoRedoShortcuts(Scene scene, Stack<Shape> undoHistory, Stack<Shape> redoHistory, Canvas canvas, GraphicsContext gc) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                if (event.isShiftDown()) {
                    // Redo: CTRL+SHIFT+Z
                    if (!redoHistory.isEmpty()) {
                        Shape shape = redoHistory.pop();
                        undoHistory.push(shape);
                        redrawCanvas(gc, canvas, undoHistory);
                    }
                } else {
                    // Undo: CTRL+Z
                    if (!undoHistory.isEmpty()) {
                        Shape shape = undoHistory.pop();
                        redoHistory.push(shape);
                        redrawCanvas(gc, canvas, undoHistory);
                    }
                }
            }
        });
    }

    // ****************************************************************************************Shortcut Keys CTRL+Z END***************************************************************************************

    // ****************************************************************************************Export to Java Code START***************************************************************************************
    private void exportCanvasToJavaFile(Canvas canvas) {
        // Get the canvas size
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        // Prepare the content of the .java file
        StringBuilder javaCode = new StringBuilder();
        javaCode.append("import javafx.application.Application;\n");
        javaCode.append("import javafx.scene.Scene;\n");
        javaCode.append("import javafx.scene.canvas.Canvas;\n");
        javaCode.append("import javafx.scene.canvas.GraphicsContext;\n");
        javaCode.append("import javafx.scene.layout.StackPane;\n");
        javaCode.append("import javafx.stage.Stage;\n");
        javaCode.append("\n");
        javaCode.append("public class ExportedCanvas extends Application {\n");
        javaCode.append("    @Override\n");
        javaCode.append("    public void start(Stage primaryStage) {\n");
        javaCode.append("        Canvas canvas = new Canvas(").append(canvasWidth).append(", ").append(canvasHeight).append(");\n");
        javaCode.append("        GraphicsContext gc = canvas.getGraphicsContext2D();\n");
        javaCode.append("\n");

        // Iterate through your shapes map to generate Java code for each shape
        for (Shape shape : shapeMap.values()) {
            if (shape instanceof Line) {
                Line line = (Line) shape;
                javaCode.append("        gc.strokeLine(")
                        .append(line.getStartX()).append(", ")
                        .append(line.getStartY()).append(", ")
                        .append(line.getEndX()).append(", ")
                        .append(line.getEndY()).append(");\n");
            } else if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                javaCode.append("        gc.fillRect(")
                        .append(rect.getX()).append(", ")
                        .append(rect.getY()).append(", ")
                        .append(rect.getWidth()).append(", ")
                        .append(rect.getHeight()).append(");\n");
            } else if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                javaCode.append("        gc.fillOval(")
                        .append(circle.getCenterX() - circle.getRadius()).append(", ")
                        .append(circle.getCenterY() - circle.getRadius()).append(", ")
                        .append(circle.getRadius() * 2).append(", ")
                        .append(circle.getRadius() * 2).append(");\n");
            } else if (shape instanceof Ellipse) {
                Ellipse ellipse = (Ellipse) shape;
                javaCode.append("        gc.fillOval(")
                        .append(ellipse.getCenterX() - ellipse.getRadiusX()).append(", ")
                        .append(ellipse.getCenterY() - ellipse.getRadiusY()).append(", ")
                        .append(ellipse.getRadiusX() * 2).append(", ")
                        .append(ellipse.getRadiusY() * 2).append(");\n");
            }
            // Add more cases for other shapes if needed
        }

        javaCode.append("\n");
        javaCode.append("        StackPane root = new StackPane();\n");
        javaCode.append("        root.getChildren().add(canvas);\n");
        javaCode.append("        primaryStage.setScene(new Scene(root, ").append(canvasWidth).append(", ").append(canvasHeight).append("));\n");
        javaCode.append("        primaryStage.show();\n");
        javaCode.append("    }\n");
        javaCode.append("    public static void main(String[] args) {\n");
        javaCode.append("        launch(args);\n");
        javaCode.append("    }\n");
        javaCode.append("}\n");

        // Prompt the user to save the file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));
        fileChooser.setInitialFileName("ExportedCanvas.java");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(javaCode.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ****************************************************************************************Export to Java Code END***************************************************************************************
    //
    // ****************************************************************************************Export to HTML Code START***************************************************************************************

    //FOR CANVAS IMAGE
//    private void exportCanvasToHTMLFile(Canvas canvas) {
//        // Get the canvas size
//        double canvasWidth = canvas.getWidth();
//        double canvasHeight = canvas.getHeight();
//
//        // Prepare the content of the HTML file
//        StringBuilder htmlCode = new StringBuilder();
//        htmlCode.append("<!DOCTYPE html>\n");
//        htmlCode.append("<html>\n");
//        htmlCode.append("<head>\n");
//        htmlCode.append("<title>Exported Canvas</title>\n");
//        htmlCode.append("<style>\n");
//        htmlCode.append("  body { display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }\n");
//        htmlCode.append("</style>\n");
//        htmlCode.append("</head>\n");
//        htmlCode.append("<body>\n");
//        htmlCode.append("<canvas id=\"myCanvas\" width=\"").append(canvasWidth).append("\" height=\"").append(canvasHeight).append("\" style=\"border:1px solid #000000;\"></canvas>\n");
//        htmlCode.append("<script>\n");
//        htmlCode.append("var canvas = document.getElementById('myCanvas');\n");
//        htmlCode.append("var ctx = canvas.getContext('2d');\n");
//
//        // Iterate through your shapes map to generate JavaScript code for each shape
//        for (Shape shape : shapeMap.values()) {
//            if (shape instanceof Line) {
//                Line line = (Line) shape;
//                htmlCode.append("ctx.beginPath();\n");
//                htmlCode.append("ctx.moveTo(").append(line.getStartX()).append(", ").append(line.getStartY()).append(");\n");
//                htmlCode.append("ctx.lineTo(").append(line.getEndX()).append(", ").append(line.getEndY()).append(");\n");
//                htmlCode.append("ctx.stroke();\n");
//            } else if (shape instanceof Rectangle) {
//                Rectangle rect = (Rectangle) shape;
//                htmlCode.append("ctx.fillRect(")
//                        .append(rect.getX()).append(", ")
//                        .append(rect.getY()).append(", ")
//                        .append(rect.getWidth()).append(", ")
//                        .append(rect.getHeight()).append(");\n");
//            } else if (shape instanceof Circle) {
//                Circle circle = (Circle) shape;
//                htmlCode.append("ctx.beginPath();\n");
//                htmlCode.append("ctx.arc(")
//                        .append(circle.getCenterX()).append(", ")
//                        .append(circle.getCenterY()).append(", ")
//                        .append(circle.getRadius()).append(", 0, 2 * Math.PI);\n");
//                htmlCode.append("ctx.fill();\n");
//            } else if (shape instanceof Ellipse) {
//                Ellipse ellipse = (Ellipse) shape;
//                htmlCode.append("ctx.beginPath();\n");
//                htmlCode.append("ctx.ellipse(")
//                        .append(ellipse.getCenterX()).append(", ")
//                        .append(ellipse.getCenterY()).append(", ")
//                        .append(ellipse.getRadiusX()).append(", ")
//                        .append(ellipse.getRadiusY()).append(", 0, 0, 2 * Math.PI);\n");
//                htmlCode.append("ctx.fill();\n");
//            }
//            // Add more cases for other shapes if needed
//        }
//
//        htmlCode.append("</script>\n");
//        htmlCode.append("</body>\n");
//        htmlCode.append("</html>\n");
//
//        // Prompt the user to save the file
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
//        fileChooser.setInitialFileName("ExportedCanvas.html");
//        File file = fileChooser.showSaveDialog(null);
//
//        if (file != null) {
//            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//                writer.write(htmlCode.toString());
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//    }

    private void exportCanvasToHTMLFile(Canvas canvas) {
        // Prepare the content of the HTML file
        StringBuilder htmlCode = new StringBuilder();
        htmlCode.append("<!DOCTYPE html>\n");
        htmlCode.append("<html lang=\"en\">\n");
        htmlCode.append("<head>\n");
        htmlCode.append("    <meta charset=\"UTF-8\">\n");
        htmlCode.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        htmlCode.append("    <title>Exported Canvas</title>\n");
        htmlCode.append("    <style>\n");
        htmlCode.append("        .shape {\n");
        htmlCode.append("            position: absolute;\n");
        htmlCode.append("            border: 1px solid black;\n");  // Adding white border to all shapes
        htmlCode.append("        }\n");
        htmlCode.append("    </style>\n");
        htmlCode.append("</head>\n");
        htmlCode.append("<body>\n");

        // Iterate through your shapes map to generate HTML code for each shape
        for (Shape shape : shapeMap.values()) {
            if (shape instanceof Line) {
                Line line = (Line) shape;
                // Convert the line into a div with a border
                htmlCode.append("<div class=\"shape\" style=\"")
                        .append("left:").append(line.getStartX()).append("px; ")
                        .append("top:").append(line.getStartY()).append("px; ")
                        .append("width:").append(line.getEndX() - line.getStartX()).append("px; ")
                        .append("height:1px; ")
                        .append("background-color:white;\"></div>\n");
            } else if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                // Convert the rectangle into a div
                htmlCode.append("<div class=\"shape\" style=\"")
                        .append("left:").append(rect.getX()).append("px; ")
                        .append("top:").append(rect.getY()).append("px; ")
                        .append("width:").append(rect.getWidth()).append("px; ")
                        .append("height:").append(rect.getHeight()).append("px; ")
                        .append("background-color:white;\"></div>\n");
            } else if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                // Convert the circle into a div with border-radius
                htmlCode.append("<div class=\"shape\" style=\"")
                        .append("left:").append(circle.getCenterX() - circle.getRadius()).append("px; ")
                        .append("top:").append(circle.getCenterY() - circle.getRadius()).append("px; ")
                        .append("width:").append(circle.getRadius() * 2).append("px; ")
                        .append("height:").append(circle.getRadius() * 2).append("px; ")
                        .append("border-radius:50%; ")
                        .append("background-color:white;\"></div>\n");
            } else if (shape instanceof Ellipse) {
                Ellipse ellipse = (Ellipse) shape;
                // Convert the ellipse into a div with border-radius and different width/height
                htmlCode.append("<div class=\"shape\" style=\"")
                        .append("left:").append(ellipse.getCenterX() - ellipse.getRadiusX()).append("px; ")
                        .append("top:").append(ellipse.getCenterY() - ellipse.getRadiusY()).append("px; ")
                        .append("width:").append(ellipse.getRadiusX() * 2).append("px; ")
                        .append("height:").append(ellipse.getRadiusY() * 2).append("px; ")
                        .append("border-radius:50%; ")
                        .append("background-color:white;\"></div>\n");
            }
            // Add more cases for other shapes if needed
        }

        htmlCode.append("</body>\n");
        htmlCode.append("</html>\n");

        // Prompt the user to save the file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
        fileChooser.setInitialFileName("ExportedCanvas.html");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(htmlCode.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ****************************************************************************************Export to HTML Code END***************************************************************************************

    // Helper function to draw shapes from history
    private void drawShape(GraphicsContext gc, Shape shape) {
        if (shape instanceof Line) {
            Line line = (Line) shape;
            gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
        } else if (shape instanceof Rectangle) {
            Rectangle rect = (Rectangle) shape;
            gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        } else if (shape instanceof Circle) {
            Circle circ = (Circle) shape;
            gc.fillOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
            gc.strokeOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
        } else if (shape instanceof Ellipse) {
            Ellipse elps = (Ellipse) shape;
            gc.fillOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
            gc.strokeOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
        }
    }

    // Helper function to add shapes to shapeMap with a UUID
    private void addShapeToMap(Shape shape) {
        String uniqueID = UUID.randomUUID().toString();
        shapeMap.put(uniqueID, shape);
        System.out.println(shape.getClass().getSimpleName() + " ID: " + uniqueID);
    }
    private int snapToGrid(int value) {
        return ((value + GRID_SIZE / 2) / GRID_SIZE) * GRID_SIZE;
    }

    private Point snapToGrid(Point p) {
        int x = snapToGrid(p.x);
        int y = snapToGrid(p.y);
        return new Point(x, y);
    }
    private void drawShapes(Canvas canvas,GraphicsContext gc) {
        // Clear the canvas first
//        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Redraw the background color
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Iterate over shapes in shapeMap and draw them
        for (Map.Entry<String, Shape> entry : shapeMap.entrySet()) {
            Shape shape = entry.getValue();

            if (shape instanceof Line) {
                Line line = (Line) shape;
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            } else if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            } else if (shape instanceof Circle) {
                Circle circ = (Circle) shape;
                gc.fillOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
                gc.strokeOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
            } else if (shape instanceof Ellipse) {
                Ellipse elps = (Ellipse) shape;
                gc.fillOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
                gc.strokeOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
            }
        }
    }


    private double[] parseCsvToDoubleArray(String csv, int length) {
        String[] parts = csv.split(",");
        double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = i < parts.length ? Double.parseDouble(parts[i]) : 50; // Default if not provided
        }
        return array;
    }

    private void drawTable(Canvas canvas, GraphicsContext gc,double startX, double startY, int rows, int cols, double[] colWidths, double[] colWidthsFromRow, int startRowForColWidths, double[] rowHeights, Color cellColor, Color borderColor) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw cells with color
        gc.setFill(cellColor);
        double currentY = startY;
        for (int i = 0; i < rows; i++) {
            double currentX = startX;
            for (int j = 0; j < cols; j++) {
                double width = (i >= startRowForColWidths) ? colWidthsFromRow[j] : colWidths[j];
                gc.fillRect(currentX, currentY, width, rowHeights[i]);
                currentX += width;
            }
            currentY += rowHeights[i];
        }

        // Draw border lines
        gc.setStroke(borderColor);
        gc.setLineWidth(1);

        // Draw horizontal lines
        currentY = startY;
        for (int i = 0; i <= rows; i++) {
            gc.strokeLine(startX, currentY, startX + Arrays.stream(colWidths).sum(), currentY);
            currentY += i < rowHeights.length ? rowHeights[i] : 50;
        }

        // Draw vertical lines
        double currentX = startX;
        for (int i = 0; i <= cols; i++) {
            gc.strokeLine(currentX, startY, currentX, startY + Arrays.stream(rowHeights).sum());
            currentX += i < colWidths.length ? colWidths[i] : 100;
        }
    }
    private void updateCanvasSize(Canvas canvas, GraphicsContext gc, ComboBox<String> pageSizeComboBox, ComboBox<String> pageLayoutComboBox, ComboBox<String> borderStyleComboBox) {
        String pageSize = pageSizeComboBox.getValue();
        String pageLayout = pageLayoutComboBox.getValue();
        String borderStyle = borderStyleComboBox.getValue();
        int index = Integer.parseInt(pageSize.substring(1));

        double width = PAGE_SIZES[index][0];
        double height = PAGE_SIZES[index][1];

        if (pageLayout.equals("Landscape")) {
            double temp = width;
            width = height;
            height = temp;
        }

        canvas.setWidth(width);
        canvas.setHeight(height);

        // Clear the canvas and redraw the white page
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the border if a border style is selected
        double borderMargin = 10; // Margin around the border

        if (!borderStyle.equals("None")) {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2); // Border width
            switch (borderStyle) {
                case "Solid":
                    gc.setLineDashes(); // Solid border
                    break;
                case "Dashed":
                    gc.setLineDashes(10, 10); // Dashed border
                    break;
                case "Dotted":
                    gc.setLineDashes(2, 5); // Dotted border
                    break;
            }
            // Draw the border with a margin of 2px
            gc.strokeRect(borderMargin, borderMargin, canvas.getWidth() - 2 * borderMargin, canvas.getHeight() - 2 * borderMargin);
        } else {
            gc.setLineDashes(); // Ensure no dashed line for "None"
        }

        // Update the ruler with the new width
        HBox ruler = createRuler(canvas.getWidth());
        // Assuming you have a reference to the StackPane containing the ruler and canvas
        StackPane canvasContainer = (StackPane) ((VBox) ((ScrollPane) canvas.getParent()).getContent()).getChildren().get(1);
        canvasContainer.getChildren().set(0, ruler); // Replace the old ruler with the new one
    }


    private void redrawCanvas(GraphicsContext gc, Canvas canvas, Stack<Shape> undoHistory) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Shape shape : undoHistory) {
            if (shape instanceof Line) {
                Line line = (Line) shape;
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            } else if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            } else if (shape instanceof Circle) {
                Circle circ = (Circle) shape;
                gc.fillOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
                gc.strokeOval(circ.getCenterX(), circ.getCenterY(), circ.getRadius(), circ.getRadius());
            } else if (shape instanceof Ellipse) {
                Ellipse elps = (Ellipse) shape;
                gc.fillOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
                gc.strokeOval(elps.getCenterX(), elps.getCenterY(), elps.getRadiusX(), elps.getRadiusY());
            }
        }
    }

    //  ***********************************************************HEADER and FOOTER START ***********************************************************
    private void drawPageBackground(GraphicsContext gc, double pageWidth, double pageHeight) {
        gc.setFill(Color.WHITE);  // Set the background color to white
        gc.fillRect(0, 0, pageWidth, pageHeight);  // Fill the entire canvas with the background color
    }

    private void drawFooterLine(GraphicsContext gc, double pageWidth, double pageHeight) {
        gc.setStroke(Color.BLACK);  // Set the line color to black
        gc.setLineWidth(2);  // Set the line width

        double footerYPosition = pageHeight - 200;  // 10px above the bottom
        gc.strokeLine(0, footerYPosition, pageWidth, footerYPosition);  // Draw the footer line

        gc.setFill(Color.BLACK);  // Set the text color to black
        gc.setFont(new Font(12));  // Set the font size
        gc.fillText("Footer", 5, footerYPosition - 5);  // Draw the footer label above the line
    }

    private void drawHeaderLine(GraphicsContext gc, double pageWidth) {
        gc.setStroke(Color.BLACK);  // Set the line color to black
        gc.setLineWidth(2);  // Set the line width

        double headerYPosition = 200;  // 10px below the top
        gc.strokeLine(0, headerYPosition, pageWidth, headerYPosition);  // Draw the header line

        gc.setFill(Color.BLACK);  // Set the text color to black
        gc.setFont(new Font(12));  // Set the font size
        gc.fillText("Header", 5, headerYPosition - 5);  // Draw the header label above the line
    }

    private void clearLines(GraphicsContext gc, double pageWidth, double pageHeight) {
        // Redraw the background of the areas where the lines and labels were drawn
        gc.clearRect(0, 0, pageWidth, 30);  // Clear the area where the header line and label were drawn
        gc.clearRect(0, pageHeight - 30, pageWidth, 30);  // Clear the area where the footer line and label were drawn

        // Redraw the page background over the cleared areas
        drawPageBackground(gc, pageWidth, pageHeight);
    }


    //  ***********************************************************HEADER and FOOTER END ***********************************************************

    private HBox createRuler(double width) {
        HBox rulerBox = new HBox();
        rulerBox.setSpacing(1); // Adjust spacing between ruler marks as needed
        rulerBox.setAlignment(Pos.CENTER); // Center the ruler

        // Create a canvas for drawing the ruler
        Canvas rulerCanvas = new Canvas(width, 30); // Height of the ruler
        GraphicsContext gc = rulerCanvas.getGraphicsContext2D();

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        double rulerHeight = 20;
        double markLength = 10; // Length of ruler marks
        double scaleInterval = 10; // Interval between marks

        for (double x = 0; x < width; x += scaleInterval) {
            gc.strokeLine(x, 0, x, markLength);

            // Add labels for major intervals
            if (x % (scaleInterval * 50) == 0) { // Adjust interval for labels
                gc.fillText(String.format("%.0f", x), x, markLength + 15);
            }
        }

        rulerBox.getChildren().add(rulerCanvas);
        return rulerBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

