package ikube.index.handler;

import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public abstract class Handler implements IHandler<Indexable<?>> {

	protected Logger logger;

	private IHandler<Indexable<?>> prev;
	private IHandler<Indexable<?>> next;
	private int threads;
	private IDataBase dataBase;

	public Handler(IHandler<Indexable<?>> previous) {
		this.prev = previous;
		if (this.prev != null) {
			((Handler) previous).setNext(this);
		}
		this.logger = Logger.getLogger(this.getClass());
	}

	@Override
	public List<Thread> handle(IndexContext indexContext, Indexable<?> indexable) throws Exception {
		if (this.next != null) {
			return this.next.handle(indexContext, indexable);
		}
		return null;
	}

	private void setNext(IHandler<Indexable<?>> next) {
		this.next = next;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public IDataBase getDataBase() {
		return dataBase;
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}
