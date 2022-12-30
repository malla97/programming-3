package fi.tuni.prog3.sisu;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Yksikkötestejä Student-, Program- ja Course-luokalle.
 */
public class SisuTest {

    @Test
    public void testStudentClass() {
        // Testataan opiskelijan nimen ja opiskelijanumeron toimivuus
        Student testStudent = new Student("Teemu Teekkari", "H123456");
        String expResultName = "Teemu Teekkari";
        String expResultStnumber = "H123456";
        String nameResult = testStudent.getName();
        String stnumberResult = testStudent.getStudentNumber();
        assertEquals(expResultName, nameResult);
        assertEquals(expResultStnumber, stnumberResult);
        
        // Tutkinto-ohjelman testaus
        testStudent.setStudyProgram("Tietotekniikan DI-ohjelma");
        String expResultStudyProgram = "Tietotekniikan DI-ohjelma";
        String studyProgramResult = testStudent.getStudyProgram();
        assertEquals(expResultStudyProgram, studyProgramResult);
        
        // Aiempien tutkinto-ohjelmien testaus uuden lisäämisen jälkeen
        testStudent.setStudyProgram("Rakennustekniikan kandidaatintutkinto");
        assertEquals("Tietotekniikan DI-ohjelma",
                testStudent.getOldStudyPrograms().get(0));
        
        // Aiempien tutkinto-ohjelmien testaus uuden aiemman tutkinto-ohjelman
        // lisäyksen yhteydessä
        testStudent.addOldStudyProgram("Tietotekniikan DI-ohjelma");
        // Testataan, lisääkö jo löytyvää ohjelmaa uudestaan
        assertEquals(1, testStudent.getOldStudyPrograms().size());
        testStudent.addOldStudyProgram("Kauppatieteiden kandidaatintutkinto");
        ArrayList<String> expResultOldPrograms = new ArrayList<>();
        expResultOldPrograms.add("Tietotekniikan DI-ohjelma");
        expResultOldPrograms.add("Kauppatieteiden kandidaatintutkinto");
        ArrayList<String> oldProgramsResult = testStudent.getOldStudyPrograms();
        
        // Testataan, lisätäänkö uusi ohjelma listaan
        for (int i = 0; i < oldProgramsResult.size(); i++) {
            assertEquals(expResultOldPrograms.get(i), oldProgramsResult.get(i));
        }
        
        // Kurssien testaus
        testStudent.addCourse("Matriisilaskenta");
        testStudent.addCourse("Vektorit ja matriisit");
        ArrayList<String> expResultCourses = new ArrayList<>();
        expResultCourses.add("Matriisilaskenta");
        expResultCourses.add("Vektorit ja matriisit");
        ArrayList<String> coursesResult = testStudent.getCourses();
        
        for (int i = 0; i < coursesResult.size(); i++) {
            assertEquals(expResultCourses.get(i), coursesResult.get(i));
        }
    }
    
    @Test
    public void testProgramClass() {
        // Opintokokonaisuuden nimen ja id:n testaus
        Program testProgram = new Program("COMP.SE-AO",
                "Ohjelmistotekniikan aineopinnot");
        String expResultId = "COMP.SE-AO";
        String expResultName = "Ohjelmistotekniikan aineopinnot";
        String idResult = testProgram.getId();
        String nameResult = testProgram.getName();
        assertEquals(expResultId, idResult);
        assertEquals(expResultName, nameResult);
        
        // Ylemmän opintokokonaisuuden lisäys
        Program parentTest = new Program("", "Aineopinnot");
        testProgram.addParent(parentTest);
        assertEquals("", testProgram.getParent().getId());
        
        // Alempien kokonaisuuksien lisäys
        Program children1Test = new Program("TTY-SU_120100",
                "Tietotekniikan suuntaavat opinnot");
        Program children2Test = new Program("TTY-AO_TIE-0001",
                "Ohjelmistotekniikka");
        testProgram.addChildren(children1Test);
        testProgram.addChildren(children2Test);
        ArrayList<Program> expResultChildren = new ArrayList<>();
        expResultChildren.add(children1Test);
        expResultChildren.add(children2Test);
        ArrayList<Program> childrenResult = testProgram.getChildren();
        
        for (int i = 0; i < childrenResult.size(); i++) {
            String expIdChild = expResultChildren.get(i).getId();
            String idResultChild = childrenResult.get(i).getId();
            assertEquals(expIdChild, idResultChild);
        }
        
        // Kurssien lisäys
        Course testCourse1 = new Course("Tietokantojen perusteet",
                "Käsiteltäviä aiheita ovat tietokannan perustaminen, päivittäminen"
                + " ja kyselyt SQL-kielellä sekä tietokannan mallintaminen ja suunnittelu.",
                testProgram, "5");
        Course testCourse2 = new Course("Rinnakkaisuus", "Opiskelija tunnistaa "
                + "rinnakkaisuuden aiheuttamat perusongelmat ja osaa joko välttää"
                + " tai ratkaista nämä ongelmat yksinkertaisissa tapauksissa.",
                testProgram, "5");
        
        testProgram.addCourses(testCourse1);
        testProgram.addCourses(testCourse2);
        ArrayList<Course> expResultCourses = new ArrayList<>();
        expResultCourses.add(testCourse1);
        expResultCourses.add(testCourse2);
        ArrayList<Course> coursesResult = testProgram.getCourses();
        
        for (int i = 0; i < coursesResult.size(); i++) {
            String expCourseName = expResultCourses.get(i).getCourseName();
            String courseNameResult = coursesResult.get(i).getCourseName();
            assertEquals(expCourseName, courseNameResult);
        }
    }
    
    @Test
    public void testCourseClass() {
        // Kurssin rakentajan testaus
        Program testProgram = new Program("COMP.SE-AO", "Ohjelmistotekniikan"
                + " aineopinnot");
        Course testCourse = new Course("Rinnakkaisuus", "Opiskelija tunnistaa "
                + "rinnakkaisuuden aiheuttamat perusongelmat ja osaa joko välttää"
                + " tai ratkaista nämä ongelmat yksinkertaisissa tapauksissa.",
                testProgram, "5");
        
        String expResultName = "Rinnakkaisuus";
        String nameResult = testCourse.getCourseName();
        String expResultDescription = String.format("Opintopisteet: 5%nTavoitteet:"
                + " Opiskelija tunnistaa rinnakkaisuuden aiheuttamat perusongelmat"
                + " ja osaa joko välttää tai ratkaista nämä ongelmat yksinkertaisissa"
                + " tapauksissa.");
        String descriptionResult = testCourse.getCourseInformation();
        boolean expResultIsPassed = false;
        boolean isPassedResult = testCourse.getIsPassed();
        String expModuleId = "COMP.SE-AO";
        String moduleIdResult = testCourse.getModule().getId();
        
        assertEquals(expResultName, nameResult);
        assertEquals(expResultDescription, descriptionResult);
        assertEquals(expResultIsPassed, isPassedResult);
        assertEquals(expModuleId, moduleIdResult);
        
        // isPassed muuttaminen
        expResultIsPassed = true;
        testCourse.setIsPassed(true);
        isPassedResult = testCourse.getIsPassed();
        assertEquals(expResultIsPassed, isPassedResult);
    }
}
