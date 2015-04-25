package sequenceDiagramGenerator.hypergraph;

public class GroupableHyperNodeFactory<T, A> implements HyperNodeFactory<T, A> {

	@Override
	public HyperNode<T, A> Generate(T data) {
		return new GroupableHyperNode<T, A>(data, uniqueint());
	}
	
	private static int anInt = 0;
	private static int uniqueint(){
		return anInt++;
	}
}
