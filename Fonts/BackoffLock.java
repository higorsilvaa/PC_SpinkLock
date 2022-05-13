import java.util.concurrent.atomic.AtomicBoolean;

public class BackoffLock implements Lock {
	private AtomicBoolean state = new AtomicBoolean(false);
	private static final int MIN_DELAY = 1;
	private static final int MAX_DELAY = 4096;
	
	public void lock() {
		Backoff backoff = new Backoff(MIN_DELAY, MAX_DELAY);
		while (true) {
			while (state.get()) {};
			if (!state.getAndSet(true))
				return;
			else {
				try{
					backoff.backoff();
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public void unlock() {
		state.set(false);
	}
}
