package sequenceDiagramGenerator.UI;

public class CmbBoxItem {
	public Object theObject;
	public String theString;
	public CmbBoxItem(){}
	public CmbBoxItem(Object aObject, String aString){
		theObject = aObject;
		theString = aString;
	}
	@Override
	public String toString(){
		return theString;
	}
}
