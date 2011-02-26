package ikube.action.rule;

public interface IRule<T> {
	
	public boolean evaluate(T t);
	
}
