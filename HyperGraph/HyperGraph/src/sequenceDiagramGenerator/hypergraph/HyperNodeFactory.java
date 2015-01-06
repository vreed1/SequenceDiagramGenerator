package sequenceDiagramGenerator.hypergraph;

public interface HyperNodeFactory<T,A> {
	public HyperNode<T,A> Generate(T data);
}
