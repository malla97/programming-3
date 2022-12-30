package fi.tuni.prog3.sisu;

import java.util.ArrayList;

/**
 * Opintokokonaisuutta kuvaava luokka
 */
public class Program {
    private String id;
    private String name;
    private Program parent;
    private ArrayList<Program> children;
    private ArrayList<Course> courses; // vaiha <Course>
    
    /**
     * Rakennin, joka ottaa parametrikseen opintokokonaisuuden id:n ja nimen.
     * @param id Opintokokonaiseedn id
     * @param name Opintokokonaisuuden nimi
     */
    public Program(String id, String name) {
        this.id = id;
        this.name = name;
        children = new ArrayList<>();
        courses = new ArrayList<>();
    }
    
    /**
     * Ylemmän opintokokonaisuuden lisäys.
     * @param program opintokokonaisuus
     */
    public void addParent(Program program) {
        this.parent = program;
    }
    
    /**
     * Alemman opintokokonaisuuden lisäys.
     * @param childProgram opintokokonaisuus
     */
    public void addChildren(Program childProgram) {
        children.add(childProgram);
    }
    
    /**
     * Kurssin lisääminen opintokokonaisuuteen.
     * @param course kurssi
     */
    public void addCourses(Course course) {
        courses.add(course);
    }
    
    /**
     * Palauttaa opintokokonaisuuden nimen.
     * @return opintokokonaisuuden nimi
     */
    public String getName() {
        return name;
    }
    
    /**
     * Palauttaa opintokokonaisuuden id:n.
     * @return opintokokonaisuuden id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Palauttaa ylemmän opintokokonaisuuden.
     * @return ylempi opintokokonaisuus
     */
    public Program getParent() {
        return parent;
    }
    
    /**
     * Palauttaa alemmat opintokokonaisuudet.
     * @return alemmat opintokokonaisuudet
     */
    public ArrayList<Program> getChildren() {
        return children;
    }
    
    /**
     * Palauttaa opintokokonaisuuden kurssit.
     * @return opintokokonaisuuden kurssit
     */
    public ArrayList<Course> getCourses() {
        return courses;
    }
}
