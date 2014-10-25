package utilities;

import java.util.List;

//For now, only moving over utility functions as needed
//Ultimately, this may not need to be in its own package

public final class Utilities {
	private Utilities() {}
	
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
