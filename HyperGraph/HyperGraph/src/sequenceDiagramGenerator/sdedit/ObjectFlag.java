package sequenceDiagramGenerator.sdedit;

public enum ObjectFlag {
    ANONYMOUS("a"), ROLE("r"), PROCESS("p"), X_AUTO_DESTRUCT("x");
    
    private final String tag;
    ObjectFlag(String t) { tag = t; }
    
    public String tag() { return tag; }
}
