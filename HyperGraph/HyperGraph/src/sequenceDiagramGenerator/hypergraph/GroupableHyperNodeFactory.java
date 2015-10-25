package sequenceDiagramGenerator.hypergraph;

public class GroupableHyperNodeFactory<T> implements HyperNodeFactory<T> {

	@Override
	public HyperNode<T> Generate(T data) {
		return new GroupableHyperNode<T>(data, uniqueint());
	}
	
	private static int anInt = 0;
	private static int uniqueint(){
		return anInt++;
	}
}
