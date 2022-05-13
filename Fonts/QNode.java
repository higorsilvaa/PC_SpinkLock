public class QNode {
	volatile boolean locked = false;
	volatile QNode next = null;
}
