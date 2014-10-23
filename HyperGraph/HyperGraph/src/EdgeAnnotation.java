
public class EdgeAnnotation {
	public boolean active;
	private String justification;
	public String get_justification(){
		return justification;
	}
	
	public EdgeAnnotation(){
		justification = "";
		active = false;
	}
	
	public EdgeAnnotation(String just, boolean activ){
		justification = just;
		active= activ;
	}
	public boolean IsActive(){return active;}
	
	@Override
	public String toString(){
		return justification;
	}
}
