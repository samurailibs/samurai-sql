package jp.dodododo.sql.function;

import java.util.List;
import java.util.function.Consumer;

import jp.dodododo.sql.IterationCallback;

public class  ConsumerWrapper<T> implements IterationCallback<T> {

	protected Consumer<T> consumer;

	public ConsumerWrapper(Consumer<T> consumer) {
		if(consumer == null) {
			throw new IllegalArgumentException();
		}
		this.consumer = consumer;
	}

    @Override
	public void iterate(T obj) {
		consumer.accept(obj);
	}

    @Override
	public List<T> getResult() {
		return null;
	}
}