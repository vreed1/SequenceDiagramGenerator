
public class Foo {

  public boolean as_long_as_it_takes = true;

  public int test() {
    int blue = 100;
    while(as_long_as_it_takes) {
        if(blue < 200) {
            blue = 100;
	}
        else {
            blue = 200;
	}
    }

    return blue;
  }

}
