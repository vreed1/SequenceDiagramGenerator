package utilities;

import java.util.List;

public final class Utilities {
	private Utilities() {}
	
	//In GeoTutor, the pebbler accounted for restrictions on theorems and axioms to be used
	//Not sure if there would be any restrictions for sequence diagram generation, so using a placeholder for now
	public static boolean PLACEHOLDER_RESTRICTION = false;
	
    public static <T> boolean AddUnique(List<T> list, T obj) {
        if (list.contains(obj)) return false;

        list.add(obj);
        return true;
    }
    
    // Makes a list containing a single element
    public static <T> void AddUniqueList(List<T> list, List<T> objList) {
        for (T o : objList) {
            AddUnique(list, o);
        }
    }
}
