package fi.tuni.prog3.sisu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Luokka tutkinto-ohjelmien tietojen hakemiseen JSON-tiedostoista.
 */
public class SisuAPI {
    private static HttpURLConnection conn;
    private TreeMap<String, String> degreePrograms = new TreeMap<>();
    private ArrayList<Program> programs = new ArrayList<>();
    
    /**
     * Rakennin, joka aloittaa kaikkien tutkinto-ohjelmien hakemisen.
     */
    public SisuAPI() {
        requestPrograms();
    }
    
    // Hakee kaikki tutkinto-ohjelmat
    private void requestPrograms() {
        BufferedReader reader;
	String line;
        StringBuilder responseContent = new StringBuilder();
            try{
                URL url = new URL("https://sis-tuni.funidata.fi/kori/api"
                        + "/module-search?curriculumPeriodId=uta-lvv-2021"
                        + "&universityId=tuni-university-root-id&moduleType"
                        + "=DegreeProgramme&limit=1000");
                conn = (HttpURLConnection) url.openConnection();
			
                // Request
                conn.setRequestMethod("GET");

                int status = conn.getResponseCode();
			
                if (status >= 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                }
                parseProgramRequest(responseContent.toString());
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }finally {
                conn.disconnect();
            }
    }
    
    // Ottaa halutut tiedot haetuista tutkinto-ohjelmista ja luo uuden
    // program-olion
    private void parseProgramRequest(String response) {
        JSONObject studyPrograms = new JSONObject(response);
        JSONArray programInfo = studyPrograms.getJSONArray("searchResults");
        for(int i = 0; i < programInfo.length(); i++) {
            JSONObject programObject = programInfo.getJSONObject(i);
            String id = programObject.getString("id");
            String name = programObject.getString("name");
            degreePrograms.put(name, id);
            
            Program programRoot = new Program(id, name);
            programs.add(programRoot);       
        }
        
    }
    
    // Tutkii rakenteen ja sen perusteella onko moduleGroupid vai 
    // courseUnitGroupId kutsuu sen perusteella oikeaa funktiota 
    private void getCorrectId(String response, Program programRoot) {
        JSONObject studyPrograms = new JSONObject(response);
        JSONObject rule = studyPrograms.getJSONObject("rule");
            Set<String> inside = rule.keySet();
            if(inside.contains("rule")) {
                JSONObject rule2 = rule.getJSONObject("rule");
                JSONArray rules = rule2.getJSONArray("rules");
                for(int j = 0; j < rules.length(); j++) {                
                    JSONObject obj = rules.getJSONObject(j);
                    Set<String> insideObj = obj.keySet();
                    if(insideObj.contains("rules")) {
                        JSONArray rules2 = obj.getJSONArray("rules");
                        for(int k = 0; k < rules2.length(); k++) {
                            JSONObject obj2 = rules2.getJSONObject(k);
                            Set<String> insideObj2 = obj2.keySet();
                            if(insideObj2.contains("moduleGroupId")) {
                                String moduleId = obj2.getString("moduleGroupId");
                                programStructure(moduleId, programRoot, "child");
                            } else if(insideObj2.contains("courseUnitGroupId")) {
                                String courseUnitId = obj2.getString("courseUnitGroupId");
                                coursesInStructure(courseUnitId, programRoot);
                            }
                        }
                    }else if(insideObj.contains("moduleGroupId")){
                        String moduleId = obj.getString("moduleGroupId");
                        programStructure(moduleId, programRoot, "child");
                    }else if(insideObj.contains("courseUnitGroupId")) {
                        String courseUnitId = obj.getString("courseUnitGroupId");
                        coursesInStructure(courseUnitId, programRoot);
                    }
                }
            } else if(inside.contains("rules")) {
                JSONArray rules = rule.getJSONArray("rules");
                for(int j = 0; j < rules.length(); j++) {
                    JSONObject obj = rules.getJSONObject(j);
                    Set<String> insideObj = obj.keySet();
                    if(insideObj.contains("rules")) {
                        JSONArray rules2 = obj.getJSONArray("rules");
                        for(int k = 0; k < rules2.length(); k++) {
                            JSONObject obj2 = rules2.getJSONObject(k);
                            Set<String> insideObj2 = obj2.keySet();
                            if(insideObj2.contains("moduleGroupId")) {
                                String moduleId = obj2.getString("moduleGroupId");
                                programStructure(moduleId, programRoot, "child");
                            } else if(insideObj2.contains("courseUnitGroupId")) {
                                String courseUnitId = obj2.getString("courseUnitGroupId");
                                coursesInStructure(courseUnitId, programRoot);
                            }
                        }
                    } else if(insideObj.contains("moduleGroupId")) {
                        String moduleId = obj.getString("moduleGroupId");
                        programStructure(moduleId, programRoot, "child");
                    } else if(insideObj.contains("courseUnitGroupId")) {
                        String courseUnitId = obj.getString("courseUnitGroupId");
                        coursesInStructure(courseUnitId, programRoot);
                    }
                }
            }
    }
    
    // Ottaa nimen ja id:n talkeen, sekä luo uuden Program-olion ja lisää olion
    // lapseksi sitä edeltävälle ohjelmalle
    private void parseOTM(String response, Program parent) {
        JSONObject studyPrograms = new JSONObject(response);
        JSONObject nameObj = studyPrograms.getJSONObject("name");
        String id = studyPrograms.getString("id");
        String name = "";
        if(nameObj.has("fi")) {
            name = (String) nameObj.get("fi");
            Program child = new Program(id, name);
            programs.add(child);
            parent.addChildren(child);
            child.addParent(parent);
        } else if(nameObj.has("en")) {
            name = (String) nameObj.get("en");
            Program child = new Program(id, name);
            programs.add(child);
            parent.addChildren(child);
            child.addParent(parent);
        }
    }
    
    // Ottaa nimen ja id:n talkeen, sekä luo uuden Program-olion ja lisää olion
    // lapseksi sitä edeltävälle ohjelmalle
    private void parseOther(String response, Program parent) {
        JSONArray studyPrograms = new JSONArray(response);
        JSONObject object = studyPrograms.getJSONObject(0);
        String id = object.getString("id");
        JSONObject nameObj = object.getJSONObject("name");
        String name = "";
        if(nameObj.has("fi")) {
            name = (String) nameObj.get("fi");
            Program child = new Program(id, name);
            programs.add(child);
            parent.addChildren(child);
            child.addParent(parent);
        } else if(nameObj.has("en")) {
            name = (String) nameObj.get("en");
            Program child = new Program(id, name);
            programs.add(child);
            parent.addChildren(child);
            child.addParent(parent);
        }
    }
    
    /**
     * Hakee Kori API:sta tietyn opintokokonaisuuden annetun id:n perusteella
     * @param idToGet 
     * @param parent
     * @param type 
     */
    public void programStructure(String idToGet, Program parent, String type) {
        String urlStr = "";
        // Katsotaan mitä muotoa id on ja tehdään haku sen perusteella
        if(isOTMFormat(idToGet)) {
            urlStr = "https://sis-tuni.funidata.fi/kori/api/modules/"
                        + idToGet;
        } else {
            urlStr = "https://sis-tuni.funidata.fi/kori/api/modules/by-group-id?groupId="
                    + idToGet
                    + "&universityId=tuni-university-root-id";
        }
        BufferedReader reader;
	String line;
        StringBuilder responseContent = new StringBuilder();
            try{
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
			
                // Request
                conn.setRequestMethod("GET");

                int status = conn.getResponseCode();
			
                if (status >= 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                }
                if(type.equals("parent")) {
                    getCorrectId(responseContent.toString(), parent);
                } else {
                    if(isOTMFormat(idToGet)) {
                        parseOTM(responseContent.toString(), parent);
                    } else {
                        parseOther(responseContent.toString(), parent);
                    }
                }
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }finally {
                conn.disconnect();
            }
    }

    // Hakee pyydetyn kurssin tiedot
    private void coursesInStructure(String courseId, Program program) {
        String urlStr = "https://sis-tuni.funidata.fi/kori/api/course-units"
                        + "/by-group-id?groupId="
                        + courseId
                        + "&universityId=tuni-university-root-id";
        BufferedReader reader;
	String line;
        StringBuilder responseContent = new StringBuilder();
            try{
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();

                // Request
                conn.setRequestMethod("GET");

                int status = conn.getResponseCode();
			
                if (status >= 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                }
                parseCourses(responseContent.toString(), program);
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }finally {
                conn.disconnect();
            }
    }
    
    // Ottaa halutut kurssin tiedot talteen ja lisää ne oikeaan kohtaan rakennetta
    private void parseCourses(String response, Program program) {
        JSONArray studyPrograms = new JSONArray(response);
        JSONObject obj = studyPrograms.getJSONObject(0);
        JSONObject nameObj = obj.getJSONObject("name");
        String name = "";
        if(nameObj.has("fi")) {
            name = (String) nameObj.get("fi");
        } else if(nameObj.has("en")) {
            name = (String) nameObj.get("en");
        }
        // Otetaan kurssin infoksi "outcomes"
        JSONObject outcomeObj = obj.optJSONObject("outcomes");
        String outcomeHTML = "";
        String outcome = "";
        if(outcomeObj != null) {
            if(outcomeObj.has("fi")) {
                outcomeHTML = (String) outcomeObj.get("fi");
            } else if(outcomeObj.has("en")) {
                outcomeHTML = (String) outcomeObj.get("en");
            }
            outcome = HTMLToString(outcomeHTML);
            outcome = formatString(outcome);
        // Jos outcomes on null, otetaan kurssin infoksi osio "additional"
        // jos sellainen löytyy. Muuten kurssilla ei lisätietoa
        } else {
            JSONObject additionalObj = obj.optJSONObject("additional");
            if(additionalObj != null) {
                if(additionalObj.has("fi")) {
                    outcomeHTML = (String) additionalObj.get("fi");
                } else if(additionalObj.has("en")) {
                    outcomeHTML = (String) additionalObj.get("en");
                }
                outcome = HTMLToString(outcomeHTML);
                outcome = formatString(outcome);
            }
            outcome = "Lisätietoja ei saatavilla";
        }
        JSONObject creditsObj = obj.getJSONObject("credits");
        Integer credits = 0;
        if(creditsObj.has("min")) {
            credits = (Integer) creditsObj.get("min");
        } else if(outcomeObj.has("max")) {
            credits = (Integer) creditsObj.get("max");
        }
        String creditsStr = credits.toString();
        Course course = new Course(name, outcome, program, creditsStr); 

        program.addCourses(course);
    }
    
    // Tarkistaa onko url:in ensimmäiset kolme merkkiä otm
    private boolean isOTMFormat(String idToGet) {
        String firstThreeChars = idToGet.substring(0, 3);
        if(firstThreeChars.equals("otm")) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Palauttaa TreeMapin jossa on tutkinto-ohjelmat ja niiden id:t.
     * @return Tutkinto-ohjelmat ja niiden id:t
     */
    public TreeMap<String, String> getDegreePrograms() {
        return degreePrograms;
    }
    
    /**
     * Palauttaa koko tutkinto-ohjelman rakenteeseen kuuluvat, paitsi kurssit.
     * @return tutkinto-ohjelman rakenne
     */
    public ArrayList<Program> getPrograms() {
        return programs;
    }
    
    // Muuttaa HTML-muotoisen tekstin normaaliin String-muotoon
    private String HTMLToString(String html) {
        return Jsoup.parse(html).wholeText();
    }
    
    // Tekee merkkijonoista korkeintaan 40 merkkiä pitkiä
    private String formatString(String word) {
        StringBuilder sb = new StringBuilder(word);
        int i = 0;
        while ((i = sb.indexOf(" ", i + 40)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        return sb.toString();
    }
}
