package fi.tuni.prog3.sisu;

import java.util.ArrayList;

/**
 * Opiskelijaa kuvaava luokka.
 */
public class Student {
    private String name;
    private String studentNumber;
    private String studyProgram;
    private ArrayList<String> courses = new ArrayList<>();
    private ArrayList<String> oldStudyPrograms = new ArrayList<>();
    
    /**
     * Rakennin, joka ottaa parametreinaan opiskelijan nimen ja
     * opiskelijanumeron.
     * @param name opiskelijan nimi
     * @param studentNumber opiskelijan opiskelijanumero
     */
    public Student(String name, String studentNumber) {
        this.name = name;
        this.studentNumber = studentNumber;
    }
    
    /**
     * Palauttaa opiskelijan nimen.
     * @return opiskelijan nimi
     */
    public String getName() {
        return name;
    }
    
    /**
     * Palauttaa opiskelijan opiskelijanumeron.
     * @return opiskelijan opiskelijanumero
     */
    public String getStudentNumber() {
        return studentNumber;
    }
    
    /**
     * Palauttaa opiskelijan tarkasteltavan tutkinto-ohjelman.
     * @return tutkinto-ohjelma
     */
    public String getStudyProgram() {
        return studyProgram;
    }
    
    /**
     * Palauttaa opiskelijan suorittamat kurssit.
     * @return opiskelijan suorittamat kurssit
     */
    public ArrayList<String> getCourses() {
        return courses;
    }
    
    /**
     * Palauttaa opiskelijan vanhat tutkinto-ohjelmat.
     * @return opiskelijan vanhat tutkinto-ohjelmat
     */
    public ArrayList<String> getOldStudyPrograms() {
        return this.oldStudyPrograms;
    }
    
    /**
     * Asettaa tarkasteltavan tutkinto-ohjelman opiskelijalle.
     * @param program tutkinto-ohjelma
     */
    public void setStudyProgram(String program) {
        
        if ( studyProgram != null && 
                !oldStudyPrograms.contains(studyProgram)){
            oldStudyPrograms.add(studyProgram);
        }
        this.studyProgram = program;
    }
    
    /**
     * Lisää opiskelijan aiemman tutkinto-ohjelman opiskelijalle.
     * @param program vanha tutkinto-ohjelma
     */
    public void addOldStudyProgram(String program) {
        if (!oldStudyPrograms.contains(program)) {
            oldStudyPrograms.add(program);
        }
    }
    
    /**
     * Kurssin lisääminen opiskelijan suorittamiin kursseihin.
     * @param course kurssi
     */
    public void addCourse(String course) {
        if (!courses.contains(course)) {
            this.courses.add(course);
        }
    }
}
