package alex.imhere.activity.model;

import com.parse.ParseException;

import org.joda.time.DateTime;

import java.util.ArrayList;

import alex.imhere.fragment.view.AbstractView;
import alex.imhere.layer.server.ServerAPI;
import alex.imhere.layer.server.Session;

public class ImhereModel implements AbstractModel {
	//TODO: exerpt methods to Service! This is too complex for Model in MVC
	ServerAPI api = new ServerAPI();

	String udid;
	Session currentSession = null;

	ArrayList<Session> onlineUsers = new ArrayList<>();
	DateTime now = new DateTime();

	public ImhereModel(String udid) {
		this.udid = udid;
	}

	public boolean isCurrentSessionAlive() {
		// TODO: do tests about aliveTo field
		return currentSession != null;
	}

	public ArrayList<Session> getOnlineUsers() throws ParseException {
		//TODO: log exception
		onlineUsers = api.getOnlineUsers(currentSession);
		return onlineUsers;
	}

	public DateTime getNow() throws ParseException {
		//TODO: log exception
		now = api.getNow(currentSession);
		return now;
	}

	public Session openNewSession() throws ParseException {
		//TODO: log exception
		currentSession = api.login(udid);
		return currentSession;
	}

	public void cancelCurrentSession() {
		if (currentSession != null) {
			api.logout(currentSession);
		}
		currentSession = null;
	}
}
