package alex.imhere.util;

public interface Resumable {
	void resume();
	void pause();
	boolean isResumed();
	//boolean isListening();
}
