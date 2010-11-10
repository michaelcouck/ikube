package ikube.action;

public interface IAction<E, F> {

	public F execute(E e);

}
