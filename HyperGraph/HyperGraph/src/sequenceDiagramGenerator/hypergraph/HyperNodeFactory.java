package sequenceDiagramGenerator.hypergraph;

public interface HyperNodeFactory<T> {
	public HyperNode<T> Generate(T data);
}
