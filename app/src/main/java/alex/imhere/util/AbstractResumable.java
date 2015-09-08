package alex.imhere.util;

public abstract class AbstractResumable implements Resumable {
	private boolean isResumed = false;

	@Override
	public boolean isResumed() {
		return isResumed;
	}

	@Override
	public void pause() {
		isResumed = false;
	}

	@Override
	public void resume() {
		isResumed = true;
	}
}
