package fi.tuni.prog3.sisu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Käyttöliittymän toiminnallisuuden toteuttava luokka.
 */
public class Sisu extends Application {
    private Student student;
    private SisuAPI api = new SisuAPI();
    private TreeMap<String, String> degrees = new TreeMap<>();
    private TreeMap<String, String> students = new TreeMap<>();
    private ArrayList<TreeItem> items = new ArrayList<>(); // parempi nimi
    // Valintalaatikot, joiden avulla näytetään tutkintoon 
    // kuuluvat kurssit, sekä onko kurssista suoritusta.
    ArrayList<CheckBox> boxes = new ArrayList<>();
    
    // Lukee JSON tiedostosta opiskelijoiden tietoja.
    private void getStudentsJSON() {
        // Luetaan kaikki opiskelijat tiedostosta
        JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader("students.json")) {
            Object obj = jsonParser.parse(reader);
            JSONArray studentList = (JSONArray) obj;
            
            studentList.forEach( stdnt -> parseStudentObject( 
                    (JSONObject) stdnt ));
            
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (ParseException e) {
        }
    }
    
    // Lisää opiskelijan TreeMap:iin students.
    private void parseStudentObject(JSONObject stdnt) {
        String name = (String) stdnt.get("nimi");
        String stNumber = (String) stdnt.get("opiskelijanumero");
        this.students.put(stNumber, name);
    }
    
    // Jos alkunäkymässä syötettiin opiskelijanumero, joka löytyy students
    // TreeMapista luetaan opsikelijan tiedot JSON tiedostosta
    private void getStudentInfoJSON(String stnum) {
        // Luetaan kaikki opiskelijat tiedostosta
        JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader("students.json")) {
            Object obj = jsonParser.parse(reader);
            JSONArray studentList = (JSONArray) obj;
            
            studentList.forEach( stdnt -> findParseStudentObject( 
                    (JSONObject) stdnt, stnum ));
            
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (ParseException e) {
        }
    }
    
    // Tutkitaan onko opiskelija sama, jonka opiskelijanumero syötettiin 
    // alkunäkymässä. Jos kyseessä on sama opiskelija lisätään tämän tiedot 
    // ohjelmaan
    private void findParseStudentObject(JSONObject stdnt, String stnum) {
        String stNumber = (String) stdnt.get("opiskelijanumero");
        // Etsitään opiskelija, jonka opiskelijanumero vastaa 
        // ohjelman opiskelijaa ja haetaan nimi sekä tutkinto-ohjelma 
        // student:iin 
        if (stNumber.equals(stnum)) {
            String name = (String) stdnt.get("nimi");
            String studyProgram = (String) stdnt.get("tutkinto-ohjelma");
            this.student = new Student(name, stnum);
            this.student.setStudyProgram(studyProgram);
            
            // Haetaan suoritetut kurssit, jos niitä on
            try {
                ArrayList<String> courses = 
                        (ArrayList) stdnt.get("suoritetutKurssit");
                for (String course : courses) {
                    this.student.addCourse(course); 
                }
            } catch (NullPointerException e) {
                // JSON tiedostossa ei suoritettuja kursseja
            }
            
            // Haetaan vanhat tutkinto-ohjelmat, jos niitä on
            try {
                ArrayList<String> studyPrograms = 
                        (ArrayList) stdnt.get("vanhatTutkinto-ohjelmat");
                for (String prgrm : studyPrograms) {
                    this.student.addOldStudyProgram(prgrm);
                }
            } catch (NullPointerException e) {
                // JSON tiedostossa ei aiempia tutkinto-ohjelmia
            }
        }
    }
    
    // Lisätään uusi opiskelija JSON tiedostoon students.json
    private void createStudentJSON() {
        JSONParser jsonParser = new JSONParser();
        
        try (FileReader fileReader = new FileReader("students.json")) {

            Object obj = jsonParser.parse(fileReader);
            JSONArray studentList = (JSONArray) obj;
            
            // Tarkistetaan onko opiskelija olemassa jo ennestään
            // Tätä tarvitaan tilanteessa, jossa vaihdetaan tutkinto-ohjelmaa
            boolean oldStudent = false;
            for (var studentObj : studentList) {
                JSONObject stdntJSON = (JSONObject) studentObj;
                if (stdntJSON.get("opiskelijanumero").equals(
                        student.getStudentNumber())) {
                    // Jos opiskelija on jo listassa lisätään tälle vain 
                    // tutkinto-ohjelma
                    stdntJSON.put("tutkinto-ohjelma", student.getStudyProgram());
                    stdntJSON.put("vanhatTutkinto-ohjelmat", 
                            student.getOldStudyPrograms());
                    oldStudent = true;
                }
            }
            
            // Opiskelija ei ollut listassa
            if (!oldStudent) {
                JSONObject studentObject = new JSONObject();
                studentObject.put("nimi", student.getName());
                studentObject.put("opiskelijanumero", 
                        student.getStudentNumber());
                studentObject.put("tutkinto-ohjelma", 
                        student.getStudyProgram());
            
                studentList.add(studentObject);
            }
            
            // Kirjoitetaan tiedostoon
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(studentList);
            FileWriter file = new FileWriter("students.json");
            file.write(jsonOutput);
            file.flush();
            
        } catch (ParseException e) {    
        } catch(IOException e) {
        }
    }
    
    // Päivittää opiskelijan opintojen tiedot JSON tiedostoon
    private void updateStudentJSON() {
        JSONParser jsonParser = new JSONParser();
        
        // Luetaan opiskelijat tiedostosta
        try (FileReader fileReader = new FileReader("students.json")) {

            Object obj = jsonParser.parse(fileReader);
            JSONArray studentList = (JSONArray) obj;
            String stnum = student.getStudentNumber();
            
            // Lisätään oikealle opiskelijalle kurssit
            studentList.forEach( stdnt -> {
                JSONObject stdntJSON = (JSONObject) stdnt;
                if (stnum.equals(stdntJSON.get("opiskelijanumero"))) {
                    stdntJSON.put("tutkinto-ohjelma", 
                            student.getStudyProgram());
                    stdntJSON.put("suoritetutKurssit", student.getCourses());
                }
            });
            
            // Kirjoitetaan tiedostoon
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(studentList);
            FileWriter file = new FileWriter("students.json");
            file.write(jsonOutput);
            file.flush();
            
        } catch (ParseException e) {
        } catch(IOException e) {
        }
    }

    // Rekursiivisesti käy läpi koko tutkinnon
    private void requestWholeDegree(Program program) {
        api.programStructure(program.getId(), program, "parent");
        if(program.getChildren().isEmpty() == false) {
            for(var child : program.getChildren()) {
                requestWholeDegree(child);
            }
        }
    }
    
    // Rekursiivisesti lisää TreeItemiin koko tutkinnon rakenteen ja siihen
    // kuuluvat kurssit
    private void addToTree(ArrayList<Program> children, TreeItem root) {
        if(children.isEmpty() == false) {
            for(var child : children) {
                TreeItem<String> childNode = new TreeItem(child.getName());
                root.getChildren().add(childNode);
                items.add(childNode);
                if(child.getChildren().isEmpty() == false && 
                        child.getCourses().isEmpty() == false) {
                    for(var course : child.getCourses()) {
                        TreeItem<String> courseNode = 
                                new TreeItem(course.getCourseName());
                        childNode.getChildren().add(courseNode);
                        items.add(courseNode);
                    }
                    addToTree(child.getChildren(), childNode);
                } else if(child.getChildren().isEmpty() == false && 
                        child.getCourses().isEmpty() == true) {
                    addToTree(child.getChildren(), childNode);
                } else if(child.getChildren().isEmpty() == true && 
                        child.getCourses().isEmpty() == false) {
                    for(var course : child.getCourses()) {
                        TreeItem<String> courseNode = 
                                new TreeItem(course.getCourseName());
                        childNode.getChildren().add(courseNode);
                        items.add(courseNode);
                    }
                }
            }
        }
    }
    
    // Palauttaa kaikki TreeItemit, joiden arvo on sama, kuin kurssi, joka
    // merkittiin suoritetuksi
    private ArrayList<TreeItem<String>> getItems(String name) {
        ArrayList<TreeItem<String>> itemsToReturn = new ArrayList<>();
        for(var item : items) {
            if(item.getValue().equals(name)) {
                itemsToReturn.add(item);
            }
        }
        return itemsToReturn;
    }
    
    private void getStructure(ArrayList<Program> programs, String degree) {
        for(var program : programs) {
            if(program.getName().equals(degree)) {
                api.programStructure(program.getId(), program, "parent");
                break;
            }
        }
    }
    
    private Program getRootProgram(ArrayList<Program> programs, String degree) {
        Program programToReturn = null;
        for(Program program : programs) {
            if(program.getName().equals(degree)) {
                programToReturn = program;
                break;
            }
        }
        return programToReturn;
    }
    
    private Program getSelectedProgram(TreeItem<String> selectedItem) {
        ArrayList<Program> programs = api.getPrograms();
        Program program = null;
        for(var pr : programs) {
            if(selectedItem.getValue().equals(pr.getName())) {
                String moduleParent = selectedItem.getParent().getValue();
                if(pr.getParent().getName().equals(moduleParent)) {
                    program = pr;
                    break;
                }
            }
        }
        return program;
    }
    
    // Lisää kurssit checkboxeina
    private void addCoursesToView(ArrayList<Course> courses, VBox boxForCourses) {
        // Lisätään kurssien vanhempi ja isovanhempi, että nähdään mihin
        // opintokokonaisuuksiin kurssit kuuluvat
        Label parentModuleLabel = new Label(courses.get(0).getModule()
                .getParent().getName());
        parentModuleLabel.setFont(new Font("Arial", 15));
        Label moduleLabel = new Label(courses.get(0).getModule().getName());
        moduleLabel.setFont(new Font("Arial", 13));
        boxForCourses.getChildren().add(parentModuleLabel);
        boxForCourses.getChildren().add(moduleLabel);
        // Lisätään kurssit
        for(var course : courses) {
            CheckBox checkBox = new CheckBox(course.getCourseName());
            // Tarkistus onko kurssi jo merkitty aikaisemmin suoritetuksi
            if(course.getIsPassed() == true) {
                checkBox.setSelected(true);
                checkBox.setDisable(true);
            }
            boxes.add(checkBox);
            // Lisää kurssille infoboksin
            Tooltip tooltip = new Tooltip(course.getCourseInformation());
            tooltip.setPrefSize(290, 300);
            tooltip.setOpacity(.8);
            tooltip.setShowDuration(Duration.millis(15000));
            checkBox.setTooltip(tooltip);
            boxForCourses.getChildren().add(checkBox);
        }
    }
    
    // Tarkistaa onko opiskelija suorittanut checboxn kurssin
    // Ruksittaa boxn ja treeviewn rivin
    private void boxCheckIfCompleted(CheckBox box, Program program, 
            TreeView<String> treeView) {
        // Tarkistetaan onko kurssi suoritettu
        if (student.getCourses().contains(box.getText())) {
            box.setSelected(true);
            // TreeViewistä polku, jota on klikattu, että saa oikean treeitemin
            ArrayList<TreeItem<String>> list = new ArrayList<>();
            for (TreeItem<String> item = (TreeItem) 
                    treeView.getSelectionModel().getSelectedItem();
                    item != null ; item = item.getParent()) {
                list.add(item);
            }
            TreeItem<String> module = list.get(0);
            
            ArrayList<TreeItem<String>> results = getItems(box.getText());
            ArrayList<Course> courses = program.getCourses();
            
            for(var res : results) {
                if(res.getParent().equals(module)) {
                    // Merkitään käyty kurssi suoritetuksi
                    for(var course : courses) {
                        if(course.getCourseName().equals(res.getValue())) {
                            course.setIsPassed(true);
                        }
                    }
                    Label label = new Label("✓");
                    res.setGraphic(label);
                    box.setDisable(true);    
                }
            }
        }
    }
    
    // Kun checkbox klikataan eli kurssi merkitään suoritetuksi
    private void boxDoInAction(CheckBox box, Program program, 
            TreeView<String> treeView) {
        box.setOnAction((eventhand) -> {
            if(box.isSelected()) {
                box.setDisable(true);
                // Lisätään kurssi student:lle ja päivitetään 
                // JSON tiedosto
                this.student.addCourse(box.getText());
                updateStudentJSON();

                // TreeViewistä polku, jota on klikattu, että saa oikean
                // treeitemin
                ArrayList<TreeItem<String>> list = new ArrayList<>();
                for (TreeItem<String> item = 
                        treeView.getSelectionModel().getSelectedItem();
                    item != null ; item = item.getParent()) {
                    list.add(item);
                }
                TreeItem<String> module = list.get(0);
                
                ArrayList<TreeItem<String>> results = getItems(box.getText());
                ArrayList<Course> courses = program.getCourses();

                for(var res : results) {
                    if(res.getParent().equals(module)) {
                        // Merkitään käyty kurssi suoritetuksi
                        for(var course : courses) {
                            if(course.getCourseName().equals(res.getValue())) {
                                course.setIsPassed(true);
                            }
                        }
                        Label label = new Label("✓");
                        res.setGraphic(label);
                        box.setDisable(true);
                    }
                }
            }
        });
    }
    
    /**
     * Käyttöliittymän komponentit ja niiden toiminnallisuudet toteuttava
     * funktio.
     * @param stage Stage esittämään käyttöliittymän ikkunaa.
     */
    @Override
    public void start(Stage stage) {
        getStudentsJSON();
        stage.setTitle("SISU");
        TabPane tabPane = new TabPane();
        degrees = api.getDegreePrograms();
        
        // Opiskelijatietojen syöttämisen scene
        GridPane startgrid = new GridPane();
        startgrid.setAlignment(Pos.CENTER);
        Scene startScene = new Scene(startgrid, 450, 180);
        Button nextButtonStart = new Button("Seuraava");
        Button closeButtonStart = new Button("Sulje ohjelma");
        Label nameTextStart = new Label("Nimi: ");
        Label studentNumberTextStart = new Label("Opiskelijanumero: ");
        Label nameErrorText = new Label("");
        nameErrorText.setTextFill(Color.color(1, 0, 0));
        Label stnumberErrorText = new Label("");
        stnumberErrorText.setTextFill(Color.color(1, 0, 0));
        TextField nameField = new TextField("");
        TextField stnumberField = new TextField("");
        startgrid.add(nameTextStart, 0, 0);
        startgrid.add(studentNumberTextStart, 0, 1);
        startgrid.add(nameField, 1, 0);
        startgrid.add(stnumberField, 1, 1);
        startgrid.add(nameErrorText, 2, 0);
        startgrid.add(stnumberErrorText, 2, 1);
        startgrid.add(nextButtonStart, 1, 3);
        startgrid.add(closeButtonStart, 1, 4);
        
        // Tutkinto-ohjelman valitsemisen scene
        GridPane gridChooseProgram = new GridPane();
        Scene sceneChooseProgram = new Scene(gridChooseProgram, 620, 300);
        Label degreeLabel = new Label("Valitse tutkinto-ohjelma: ");
        ComboBox comboBox = new ComboBox();
        
        ArrayList<Program> programs = api.getPrograms();
        for (var degree : programs) {
            comboBox.getItems().add(degree.getName());
        }
        
        Button nextButtonChooseProgram = new Button("Seuraava");
        Button closeButtonChooseProgram = new Button("Sulje ohjelma");
        gridChooseProgram.add(nextButtonChooseProgram, 0, 2);
        gridChooseProgram.add(comboBox, 0, 1);
        gridChooseProgram.add(degreeLabel, 0, 0);
        gridChooseProgram.add(closeButtonChooseProgram, 0, 3);
              
        // Pääikkunan scene
        Tab tabStudent = new Tab("Opiskelijan tiedot");
        Tab tabCourses = new Tab("Opintojen rakenne");
        tabPane.getTabs().add(tabStudent);
        tabPane.getTabs().add(tabCourses);
        VBox vBox = new VBox(tabPane);
        var sceneMain = new Scene(vBox, 1000, 800);
        
        // Opiskelijan tiedot-näkymä
        Label nameLabelMain = new Label("Nimi: " );
        Label stnumberLabelMain = new Label("Opiskelijanumero: ");
        Label studyProgramLbl = new Label("Tutkinto-ohjelma: ");
        Button changeProgramButton = new Button("Vaihda tutkinto-ohjelmaa");
        Button logOutButton = new Button("Kirjaudu ulos");
        Button closeButtonMain = new Button("Sulje ohjelma");
        VBox studentTabVBox = new VBox();
        studentTabVBox.getChildren().addAll(
                nameLabelMain, stnumberLabelMain, studyProgramLbl, 
                changeProgramButton, logOutButton, closeButtonMain);
        tabStudent.setContent(studentTabVBox);
        
        // Scene varmistukseksi uloskirjautumiselle
        GridPane gridLogOut = new GridPane();
        gridLogOut.setAlignment(Pos.CENTER);
        Scene sceneLogOut = new Scene(gridLogOut, 400, 300);
        Button eikuButtonLogOut = new Button("EIKU");
        Button logOutButtonLogOut = new Button("Kirjaudu ulos");
        Button closeButtonLogOut = new Button("Sulje ohjelma");
        Label logOutLabel = new Label("Haluatko varmasti kirjautua ulos?");
        gridLogOut.add(logOutLabel, 0, 0, 4, 1);
        gridLogOut.add(eikuButtonLogOut, 0, 1);
        gridLogOut.add(closeButtonLogOut, 2, 1);
        gridLogOut.add(logOutButtonLogOut, 1, 1);
        
        // Scene varmistukseksi tutkinnon vaihdolle
        GridPane gridChangeProgram = new GridPane();
        gridChangeProgram.setAlignment(Pos.CENTER);
        Scene sceneChangeProgram = new Scene(gridChangeProgram, 400, 300);
        Button eikuButtonChangeProgram = new Button("EIKU");
        Button yesButtonChangeProgram = new Button("Kyllä");
        Button closeButtonChangeProgram = new Button("Sulje ohjelma");
        Label labelChangeProgram = new Label(
                "Haluatko varmasti vaihtaa tutkintoa?");
        gridChangeProgram.add(labelChangeProgram, 0, 0, 4, 1);
        gridChangeProgram.add(eikuButtonChangeProgram, 0, 1);
        gridChangeProgram.add(yesButtonChangeProgram, 1, 1);
        gridChangeProgram.add(closeButtonChangeProgram, 2, 1);
        
        // Ohjelman sulkemis-nappien toiminnallisuudet
        closeButtonStart.setOnAction(e -> Platform.exit());
        closeButtonChooseProgram.setOnAction(e -> Platform.exit());
        closeButtonMain.setOnAction(e -> Platform.exit());
        closeButtonLogOut.setOnAction(e -> Platform.exit());
        closeButtonChangeProgram.setOnAction(e -> Platform.exit());

        // Tutkinto-ohjelman vaihto-napin toiminnallisuus
        changeProgramButton.setOnAction((event) -> {
            stage.setScene(sceneChangeProgram);
        });
        
        // Kirjaudu ulos-napin toiminnallisuus opiskelijan tiedot välilehdessä
        logOutButton.setOnAction((event) -> {
            stage.setScene(sceneLogOut);
        });
        
        // Varmistus välilehdellä ulos kirjautumisen yhteydessä eiku-napin
        // toiminnallisuus
        eikuButtonLogOut.setOnAction((event) -> {
            stage.setScene(sceneMain);
        });
        
        // Varmitus välilehdellä ulos kirjautumisen yhteydessä kirjaudu 
        // ulos-napin
        // toiminnallisuus
        logOutButtonLogOut.setOnAction((event) -> {
            stage.setScene(startScene);
        });
        
        // Varmistus välilehdellä tutkinnon vaihdon yhteydessä eiku-napin
        // toiminnallisuus
        eikuButtonChangeProgram.setOnAction((event) -> {
            stage.setScene(sceneMain);
        });
        
        // Varmistus välilehdellä tutkinnon vaihdon yhteydessä kyllä-napin
        // toiminnallisuus
        yesButtonChangeProgram.setOnAction((event) -> {
            stage.setScene(sceneChooseProgram);
        });
        
        // Aloitus näkymästä pääikkunaan siirtyminen
        nextButtonStart.setOnAction((event) -> {
            
            // Opiskelijan olemassa olemisen tarkistus
            String studentName = nameField.getText();
            String stnumber = stnumberField.getText();
            nameErrorText.setText("");
            stnumberErrorText.setText("");
            
            // Testaa kenttien sisällön sopivuus
            
            if (studentName.equals("") && stnumber.equals("")) {
                nameErrorText.setText("Syötä nimi");
                stnumberErrorText.setText("Syötä opiskelijanumero");
            } else if (studentName.matches(".*[0-9].*") &&
                    !stnumber.matches(".*[0-9].*")) {
                stnumberErrorText.setText("Opiskelijanumerossa tulee"
                        + " olla numeroita");
                nameErrorText.setText("Poista numerot nimestä");
            } else if (studentName.matches(".*[0-9].*") && stnumber.equals("")) {
                stnumberErrorText.setText("Syötä opiskelijanumero");
                nameErrorText.setText("Poista numerot nimestä");
            } else if (studentName.equals("") && !stnumber.matches(".*[0-9].*")) {
                nameErrorText.setText("Syötä nimi");
                stnumberErrorText.setText("Opiskelijanumerossa tulee"
                        + " olla numeroita");
            } else if(studentName.matches(".*[0-9].*")) {
                    nameErrorText.setText("Poista numerot nimestä");
            } else if (!stnumber.matches(".*[0-9].*")) {
                stnumberErrorText.setText("Opiskelijanumerossa tulee"
                        + " olla numeroita");
            } else if (studentName.equals("")) {
                    nameErrorText.setText("Syötä nimi");
            } else if (stnumber.equals("")) {
                    stnumberErrorText.setText("Syötä opiskelijanumero");
            } else {
                // Tekstikenttien tyhjennys ulos kirjautumista varten
                nameField.clear();
                stnumberField.clear();
                
                if (students.containsKey(stnumber)) {
                    getStudentInfoJSON(stnumber);
                    nameLabelMain.setText("Nimi: " + student.getName());
                    stnumberLabelMain.setText("Opiskelijanumero: " 
                            + student.getStudentNumber());
                    studyProgramLbl.setText("Tutkinto-ohjelma: " 
                            + student.getStudyProgram());
                    
                    // Halutun tutkinto-ohjelman rakenteen haku
                    String degree = student.getStudyProgram();
                    getStructure(programs, degree);
            
                    Program rootProgram = getRootProgram(programs, degree);
                    ArrayList<Program> children = rootProgram.getChildren();
                    for(var child : children) {
                        requestWholeDegree(child);
                    }
                    
                    stage.setScene(sceneMain);
                    HBox hBoxProgram = new HBox();
                    VBox vBoxCourses = new VBox();
                    ScrollPane scroll = new ScrollPane();
                    
                    // TreeView tutkinto-ohjelman esittämiseen
                    TreeView treeView = new TreeView();
                    treeView.setMinSize(500, 700);
                    scroll.setMinSize(500, 700);
                    
                    TreeItem<String> rootItem = new TreeItem(
                            rootProgram.getName());
                    items.add(rootItem);
                    addToTree(children, rootItem);
                    treeView.setRoot(rootItem);
                    
                    tabCourses.setContent(hBoxProgram);
                    hBoxProgram.getChildren().add(treeView);
                    hBoxProgram.getChildren().add(scroll);
                    scroll.setContent(vBoxCourses);
                    
                    Label instruction = new Label("Valitse opintokokonaisuus"
                            + " johon kuuluu kursseja");
                    instruction.setFont(new Font("Arial", 15));
                    vBoxCourses.getChildren().add(instruction);
            
                    // Tutkintoon kuuluvien kurssien läpikäynti ja checkboxin 
                    // luominen kurssille
                    treeView.getSelectionModel().selectedItemProperty()
                            .addListener(new ChangeListener() {
                        @Override
                        public void changed(ObservableValue observable,
                                Object oldValue, Object newValue) {
                            TreeItem<String> selectedItem = 
                                    (TreeItem<String>) newValue;
                            // Tästä eteenpäin funtkioon?
                            vBoxCourses.getChildren().clear();
                            Program progToHandle = getSelectedProgram(
                                    selectedItem);
                            if(progToHandle != null && 
                                    progToHandle.getCourses().isEmpty() == false) {
                                addCoursesToView(progToHandle.getCourses(), 
                                            vBoxCourses);
                            }
                            for(var box : boxes) {
                                boxDoInAction(box, progToHandle, treeView);
                                boxCheckIfCompleted(box, progToHandle, treeView);
                              }
                          }
                      });

                } else {
                    this.student = new Student(studentName, stnumber);
                    students.put(stnumber, studentName);
                    nameLabelMain.setText("Nimi: " + student.getName());
                    stnumberLabelMain.setText("Opiskelijanumero: " 
                            + student.getStudentNumber());
                    stage.setScene(sceneChooseProgram);
                }
            }
        });
        
        nextButtonChooseProgram.setOnAction((event) -> {
            String degree = (String) comboBox.getValue();
            
            // Halutun tutkinto-ohjelman rakenteen haku
            getStructure(programs, degree);
            
            Program rootProgram = getRootProgram(programs, degree);
            ArrayList<Program> children = rootProgram.getChildren();
            for(var child : children) {
                requestWholeDegree(child);
            }
            
            stage.setScene(sceneMain);
            HBox hBoxProgram = new HBox();
            VBox vBoxCourses = new VBox();
            ScrollPane scroll = new ScrollPane();
            
            // TreeView tutkinto-ohjelman esittämiseen
            TreeView treeView = new TreeView();
            treeView.setMinSize(500, 700);
            scroll.setMinSize(500, 700);
            
            TreeItem<String> rootItem = new TreeItem(rootProgram.getName());
            items.add(rootItem);          
            addToTree(children, rootItem);

            treeView.setRoot(rootItem);
            
            tabCourses.setContent(hBoxProgram);
            hBoxProgram.getChildren().add(treeView);
            hBoxProgram.getChildren().add(scroll);
            scroll.setContent(vBoxCourses);
            
            Label instruction = new Label("Valitse opintokokonaisuus"
                    + " johon kuuluu kursseja");
            instruction.setFont(new Font("Arial", 15));
            vBoxCourses.getChildren().add(instruction);
            
            // Tutkintoon kuuluvien kurssien läpikäynti ja checkboxin luominen
            // kurssille
            treeView.getSelectionModel().selectedItemProperty()
                            .addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable,
                    Object oldValue, Object newValue) {
                    TreeItem<String> selectedItem = (TreeItem<String>) newValue;
                    vBoxCourses.getChildren().clear();
                    Program progToHandle = getSelectedProgram(selectedItem);
                    if(progToHandle != null && 
                            progToHandle.getCourses().isEmpty() == false) {
                        addCoursesToView(progToHandle.getCourses(), vBoxCourses);
                    }
                    for(var box : boxes) {
                        boxDoInAction(box, progToHandle, treeView);
                        boxCheckIfCompleted(box, progToHandle, treeView);
                    }
                }
                        
            });
            
            // Lisätään tutkinto-ohjelma Student:lle ja lisätään opiskelija
            // JSON tiedostoon
            this.student.setStudyProgram(degree);
            createStudentJSON();
            studyProgramLbl.setText("Tutkinto-ohjelma: " + degree);
        });
        
        stage.setScene(startScene);
        stage.show();
    }

    /**
     * Käyttöliittymän käynnistys.
     * @param args 
     */
    public static void main(String[] args) {
        launch();
    }
}