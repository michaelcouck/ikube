package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.log4j.Logger;

public abstract class Handler implements IHandler<Indexable<?>> {

	protected Logger logger;
	private IHandler<Indexable<?>> prev;
	private IHandler<Indexable<?>> next;

	public Handler(IHandler<Indexable<?>> previous) {
		this.prev = previous;
		if (this.prev != null) {
			((Handler) previous).setNext(this);
		}
		this.logger = Logger.getLogger(this.getClass());
	}

	@Override
	public void handle(IndexContext indexContext, Indexable<?> indexable) {
		if (this.next != null) {
			this.next.handle(indexContext, indexable);
		}
	}

	private void setNext(IHandler<Indexable<?>> next) {
		this.next = next;
	}

}
