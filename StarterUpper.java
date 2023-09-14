package gtjavafx;

import javafx.geometry.HPos; // Main Program Imports
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.collections.ObservableList; // Extra Credit: Display Problems
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;

import java.util.Scanner; // Extra Credit: Upload ideas.txt to program
import java.util.NoSuchElementException;

import javafx.scene.paint.Color; // Extra Credit: Add color

/**
 * CS1331: Intro to Object-Oriented Programming Homework #9: (Due 26 April 2022)
 * I worked on the homework assignment alone, using only course materials.
 *
 * This document details a StarterUpper.
 *
 * @author Ethan Nguyen-Tu
 * @version 1.0.5
 */
public class StarterUpper extends Application {

    private ArrayList<StartUpIdea> ideas = new ArrayList<>(1);

    private File startUpIdeas = new File("ideas.txt");

    private int headerRows = 4; // number of rows set aside for the header (Note: row index starts at 0)
    private int totColumns = 3;
    private int midColumn = (int) Math.ceil((double) totColumns / 2);
    private int totQuestions = 0; // total number of questions asked

    private int editIndex = -1; // Edit Index's index

    private GridPane gridPane = new GridPane();
    private final Label author = new Label("Created by Ethan Nguyen-Tu"); // NAME REQUIREMENT

    private TextField problemInput = new TextField(); // User Inputs
    private TextField targetCustomerInput = new TextField();
    private TextField customerNeedInput = new TextField();
    private TextField knwnPepleWithProbInput = new TextField();
    private TextField targetMarketSizeInput = new TextField();
    private TextField competitorsInput = new TextField();
    private TextField requiredSkillsInput = new TextField(); // Extra Credit: Additional Startup Field

    private boolean updateCheck;

    private String problem; // Expected Values
    private String targetCustomer;
    private int customerNeed = -1;
    private int knownPeopleWithProblem = -1;
    private int targetMarketSize = -1;
    private String competitors;
    private String requiredSkills; // Extra Credit: Additional Startup Field

    private ObservableList<String> problems = FXCollections.observableArrayList();
    private ListView<String> listView = new ListView<>(problems);

    @Override
    public void start(Stage mainStage) {
        //gridPane.setGridLinesVisible(true); // Shows Gridlines (Debugging tool)
        formHeader();
        formDesign();
        uploadIdeas(); // Extra Credit: Upload ideas.txt to program
        addQuestion("What is the problem?", problemInput); // Questions
        addQuestion("Who is the target customer?", targetCustomerInput);
        addQuestion("How badly does the customer NEED this problem fixed (1-10)?", customerNeedInput);
        addQuestion("How many people do you know who might experience this problem?", knwnPepleWithProbInput);
        addQuestion("How big is the target market?", targetMarketSizeInput);
        addQuestion("Who are the competitors/existing solutions?", competitorsInput);
        addQuestion("What are the main skills required to solve the problem?", requiredSkillsInput);
        // Display Problems
        updateProblemListView();
        // Submit Button
        Button submit = new Button("Add Idea");
        submit.setOnAction(event -> addIdea());
        // Sort Button
        Button sort = new Button("Sort Ideas");
        gridPane.add(sort, totColumns, totQuestions + headerRows + 2);
        GridPane.setHalignment(sort, HPos.CENTER);
        sort.setOnAction(event -> {
                try {
                    Collections.sort(ideas);
                } catch (ClassCastException | UnsupportedOperationException | IllegalArgumentException e) {
                    errorPopUp("Exception", "Sorting Exception", "Ideas cannot be sorted.");
                }
                updateProblemListView();
            });
        // Reset Button
        Button reset = new Button("Full Reset");
        reset.setOnAction(event -> {
                Alert resetConfirm = new Alert(Alert.AlertType.CONFIRMATION); // Reset Confirmation Check
                resetConfirm.setTitle("Confirm Full Reset?");
                resetConfirm.setHeaderText("Pressing OK deletes the file ideas.txt (if it exists) and resets all form "
                        + "fields.");
                resetConfirm.showAndWait();
                if (resetConfirm.getResult() == ButtonType.OK) {
                    ideas = new ArrayList<>(); // clear current list of StartUpIdea's
                    clearFields();
                    updateProblemListView();
                    try { //Delete File if exists
                        if (startUpIdeas.exists()) {
                            System.out.println(startUpIdeas.delete() ? "Deleted file: ideas.txt"
                                    : "Failed to delete file: ideas.txt");
                        } else {
                            System.out.println("File does not exist: ideas.txt");
                        }
                    } catch (SecurityException e) {
                        System.out.println("Denied Access to file ideas.txt by security manager.");
                    }
                }
            });
        // Save To File Button
        Button saveToFile = new Button("Save to File");
        gridPane.add(saveToFile, totColumns, totQuestions + headerRows + 3);
        GridPane.setHalignment(saveToFile, HPos.CENTER);
        saveToFile.setOnAction(new EventHandler<ActionEvent>() { //ANONYMOUS CLASS REQUIREMENT
                @Override
                public void handle(ActionEvent actionEvent) {
                    FileUtil.saveIdeasToFile(ideas, startUpIdeas);
                }
            });
        // Remove Idea Button
        Button remove = new Button("Remove Idea");
        remove.setOnAction(event -> {
                Alert removeConfirm = new Alert(Alert.AlertType.CONFIRMATION); // Reset Confirmation Check
                removeConfirm.setTitle("Confirm Idea Removal?");
                removeConfirm.setHeaderText("If an idea was selected, pressing OK removes the selected idea.");
                removeConfirm.showAndWait();
                if (removeConfirm.getResult() == ButtonType.OK) {
                    if (editIndex != -1) {
                        ideas.remove(editIndex);
                        editIndex = -1;
                    }
                    updateProblemListView();
                    clearFields();
                }
            });
        // Update Button
        Button update = new Button("Update");
        update.setOnAction(event -> {
                Alert updateConfirm = new Alert(Alert.AlertType.CONFIRMATION); // Reset Confirmation Check
                updateConfirm.setTitle("Confirm Idea Update?");
                updateConfirm.setHeaderText("If an idea was selected, pressing OK removes the selected idea and adds "
                        + "the updated\n idea to the bottom of the list. If no idea was selected, the idea\nis "
                        + "regularly added.");
                updateConfirm.showAndWait();
                if (updateConfirm.getResult() == ButtonType.OK) {
                    updateCheck = false;
                    addIdea();
                    if (editIndex != -1 && updateCheck) {
                        ideas.remove(editIndex);
                        editIndex = -1;
                        updateProblemListView();
                    }
                }
            });
        // Clear Button
        Button clear = new Button("Clear");
        clear.setOnAction(event -> clearFields());
        //Add Buttons
        HBox addRemoveHBox = new HBox(10);
        addRemoveHBox.getChildren().addAll(submit, remove);
        addRemoveHBox.setAlignment(Pos.CENTER);
        gridPane.add(addRemoveHBox, midColumn, totQuestions + headerRows + 2);
        HBox updateClearHBox = new HBox(10);
        updateClearHBox.getChildren().addAll(update, clear, reset);
        updateClearHBox.setAlignment(Pos.CENTER_LEFT);
        gridPane.add(updateClearHBox, midColumn, totQuestions + headerRows + 3);
        //SCENE CREATION
        Scene scene = new Scene(gridPane, Color.GOLD);
        mainStage.setTitle("Problem Ideation Form"); //WINDOW TITLE REQUIREMENT
        mainStage.setScene(scene);
        mainStage.show();
    }

    //FORM METHODS
    /**
     * Helper method that clears the form fields.
     */
    private void clearFields() {
        problemInput.clear(); //Clear Form
        targetCustomerInput.clear();
        customerNeedInput.clear();
        knwnPepleWithProbInput.clear();
        targetMarketSizeInput.clear();
        competitorsInput.clear();
        requiredSkillsInput.clear();
    }

    /**
     * Method that adds the idea contained in the TextFields to ideas.
     */
    private void addIdea() {

        boolean notInteger = true;
        String error = "Not all values are filled out correctly:";

        try {
            // String Response Questions
            problem = problemInput.getCharacters().toString(); // Problem Question
            if (problem.equals("")) {
                error += "\nThe problem is not specified.";
            }
            targetCustomer = targetCustomerInput.getCharacters().toString(); // Customer Question
            if (targetCustomer.equals("")) {
                error += "\nThe target customer is not specified.";
            }
            competitors = competitorsInput.getCharacters().toString(); // Competition Question
            if (competitors.equals("")) {
                error += "\nThe competitors/existing solutions are not specified.";
            }
            requiredSkills = requiredSkillsInput.getCharacters().toString(); // Competition Question
            if (requiredSkills.equals("")) {
                error += "\nThe required skills are not specified.";
            }
            // Integer Response Questions
            if (customerNeedInput.getCharacters().toString().equals("")) { //Need Question
                error += "\nA rating for customer need is not specified.";
            } else {
                customerNeed = Integer.parseInt(customerNeedInput.getCharacters().toString());
                if (customerNeed < 1 || customerNeed > 10) {
                    error += "\nSpecify an integer from 1 to 10 for customer need.";
                }
            }
            if (knwnPepleWithProbInput.getCharacters().toString().equals("")) { // Affected Question
                error += "\nThe number people who you know might be affected by this problem is not specified.";
            } else {
                knownPeopleWithProblem = Integer.parseInt(knwnPepleWithProbInput.getCharacters().toString());
                if (knownPeopleWithProblem < 0) {
                    error += "\nSpecify the number of people who might be affected as a positive integer.";
                }
            }
            if (targetMarketSizeInput.getCharacters().toString().equals("")) { // Target Market Size
                error += "\nThe market size is not specified.";
            } else {
                targetMarketSize = Integer.parseInt(targetMarketSizeInput.getCharacters().toString());
                notInteger = false;
                if (targetMarketSize < 0) {
                    error += "\nSpecify the market size as a positive integer.";
                }
            }
            if (!error.equals("Not all values are filled out correctly:")) { //Error Check
                throw new NumberFormatException();
            }
            // Creation of new StartUpIdea instance to List
            updateCheck = true;
            ideas.add(new StartUpIdea(problem, targetCustomer, customerNeed, knownPeopleWithProblem,
                    targetMarketSize, competitors, requiredSkills));
            clearFields();
            updateProblemListView();
        } catch (NumberFormatException e) { // Error Message
            if (notInteger) {
                error += "\nA positive integer value was not specified for customer need, number of people who "
                        + "might experience this problem, and/or how big is the market.";
            }
            errorPopUp("Error", "Invalid User Input", error);
        }
    }

    /**
     * Helper method that adds a question to the GridPane. Questions in the GridPlane are located in column 2 (index 1).
     * TextField for the question is located in column 3.
     * @param question String of question to be added
     */
    private void addQuestion(String question, TextField textField) {
        int rowLocation = headerRows + totQuestions + 1;
        Label theQuestion = new Label(question);
        gridPane.add(theQuestion, 1, rowLocation);
        gridPane.add(textField, 2, rowLocation);
        GridPane.setHalignment(theQuestion, HPos.RIGHT);
        totQuestions++;
    }

    /**
     * Method that sets up am Error Alert and takes in the error's title, header, and contents in that order and sets
     * each appropriately.
     * @param title String title of the error window
     * @param header String header of the error window
     * @param contents String contents of the error window
     */
    private void errorPopUp(String title, String header, String contents) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(title);
        error.setHeaderText(header);
        error.setContentText(contents);
        error.showAndWait();
    }

    /**
     * Method that updates the displayed problem list view.
     */
    private void updateProblemListView() {
        problems = FXCollections.observableArrayList();
        for (StartUpIdea idea : ideas) {
            problems.add(idea.toString());
        }
        gridPane.getChildren().remove(listView);
        listView = new ListView<>(problems);
        listView.setMinSize(150, 150);
        listView.setMaxHeight(totQuestions * 30);
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() { // Second Anonymous Class just in case
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (ideas.size() > 0 ) {
                        editIndex = listView.getSelectionModel().getSelectedIndex();
                        problemInput.setText(ideas.get(editIndex).getProblem());
                        targetCustomerInput.setText(ideas.get(editIndex).getTargetCustomer());
                        customerNeedInput.setText("" + ideas.get(editIndex).getCustomerNeed());
                        knwnPepleWithProbInput.setText("" + ideas.get(editIndex).getKnownPeopleWithProblem());
                        targetMarketSizeInput.setText("" + ideas.get(editIndex).getTargetMarketSize());
                        competitorsInput.setText(ideas.get(editIndex).getCompetitors());
                        requiredSkillsInput.setText(ideas.get(editIndex).getRequiredSkills());
                    }
                }
            });
        gridPane.add(listView, totColumns, headerRows + 1, 1, totQuestions + 1);
    }

    /**
     * Method for uploading ideas from ideas.txt. Blank numbers result in -1.
     */
    private void uploadIdeas() {
        try (Scanner scanFile = new Scanner(new File("ideas.txt"))) {
            String line;
            while ((line = scanFile.nextLine()) != null) {
                if (line.startsWith("Problem:")) {
                    problem = line.substring(8).trim();
                    line = scanFile.nextLine().trim();
                    targetCustomer = line.startsWith("Target Customer:") ? line.substring(17).trim() : "";
                    line = scanFile.nextLine().trim();
                    customerNeed = line.startsWith("Customer Need:") ? Integer.parseInt(line.substring(14).trim()) : -1;
                    line = scanFile.nextLine().trim();
                    knownPeopleWithProblem = line.startsWith("Known People With Problem:")
                            ? Integer.parseInt(line.substring(26).trim()) : -1;
                    line = scanFile.nextLine().trim();
                    targetMarketSize = line.startsWith("Target Market Size:")
                            ? Integer.parseInt(line.substring(19).trim()) : -1;
                    line = scanFile.nextLine().trim();
                    competitors = line.startsWith("Competitors:") ? line.substring(12).trim() : "";
                    line = scanFile.nextLine().trim();
                    requiredSkills = line.startsWith("Required Skills:") ? line.substring(16).trim() : "";
                    ideas.add(new StartUpIdea(problem, targetCustomer, customerNeed, knownPeopleWithProblem,
                            targetMarketSize, competitors, requiredSkills));
                    updateProblemListView();
                }
            }
        } catch (NumberFormatException | NoSuchElementException | FileNotFoundException e) {
            System.out.println("Ideas have been added if ideas.txt exists.");
        }
    }
    //DESIGN METHODS
    /**
     * Helper method that sets up the header of the form.
     */
    private void formHeader() {
        Label title = new Label("Problem Ideation Form"); // Header Title
        title.setFont(new Font(20.0));
        gridPane.add(title, midColumn, 1);
        GridPane.setHalignment(title, HPos.CENTER);
        GridPane.setConstraints(author, midColumn, 2); // NAME REQUIREMENT
        gridPane.getChildren().add(author);
        GridPane.setHalignment(author, HPos.CENTER);
        Label ideasView = new Label("Current Ideas:"); // Current Ideas Title
        gridPane.add(ideasView, totColumns, headerRows);
        GridPane.setHalignment(ideasView, HPos.CENTER);
    }

    /**
     * Helper method that adds aesthetics to the form.
     */
    private void formDesign() {
        gridPane.setHgap(20);
        gridPane.add(new Label(" "), midColumn, headerRows); //header2form
        gridPane.add(new Label(" "), midColumn, totQuestions + headerRows + 1); //form2button
        gridPane.add(new Label(" "), midColumn, headerRows + totQuestions + 10); // Add space at bottom
    }
    //LAUNCH METHOD
    /**
     * Launches StarterUpper from Terminal.
     * @param args String array that does nothing
     */
    public static void main(String[] args) {
        launch();
    }

} //END OF STARTERUPPER CLASS

/* Mac Terminal Commands to Run in Folder
javac --module-path javafx-sdk-18.0.1/lib/ --add-modules javafx.controls StarterUpper.java
java --module-path javafx-sdk-18.0.1/lib/ --add-modules javafx.controls StarterUpper.java
 */