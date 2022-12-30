package fi.tuni.prog3.sisu;

/**
 * Kurssia kuvaava luokka
 */
public class Course {
    private String courseName;
    private String courseDescription;
    private String credits;
    private Program studyModule;
    private boolean isPassed;
    
    /**
     * Rakennin, joka luo kurssin.
     * @param courseName kurssin nimi
     * @param courseDescription kurssin esittely
     * @param module opintokokonaisuus, johon kurssi kuuluu
     * @param credits kurssin laajuus opintopisteinä
     */
    public Course(String courseName, String courseDescription, Program module,
            String credits) {
        this.courseName = courseName;
        this.studyModule = module;
        this.credits = credits;
        this.courseDescription = courseDescription;
        this.isPassed = false; 
    }
    
    /**
     * Palauttaa kurssin nimen.
     * @return kurssin nimi
     */
    public String getCourseName() {
        return courseName;
    }
    
    /**
     * Palauttaa opintokokonaisuuden, johon kurssi kuuluu.
     * @return opintokokonaisuus
     */
    public Program getModule() {
        return studyModule;
    }
    
    /**
     * Palauttaa totuusarvon siitä, onko kurssi suoritettu.
     * @return onko kurssi suoritettu
     */
    public boolean getIsPassed() {
        return isPassed;
    }
    
    /**
     * Asettaa totuusarvon siitä, onko kurssi suoritettu.
     * @param value onko kurssi suoritettu
     */
    public void setIsPassed(boolean value) {
        isPassed = value;
    }
    
    /**
     * Palauttaa kurssin kuvauksen.
     * @return kurssin kuvaus
     */
    public String getCourseInformation() {
        String info = String.format("Opintopisteet: %s%nTavoitteet: "
                + "%s", credits, courseDescription);
        return info;
    }
}
