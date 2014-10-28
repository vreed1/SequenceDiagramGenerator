package concreteAST;

public class CodeObject {
    private int objectId;
    public void setId(int id) { objectId = id; }
    public int getId() { return objectId; }



    public CodeObject() {
        objectId = -1;
    }
}
