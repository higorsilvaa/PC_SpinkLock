import java.util.concurrent.Semaphore;

public class SemaphoreB implements Lock {
	Semaphore s = new Semaphore(1);
	
	public void lock() {
		s.acquireUninterruptibly();
	}
	
	public void unlock() {
		s.release();
	}
}
