package app.armot.utils;

import annotations.Singleton;
import app.armot.api.bean.User;

import java.util.HashMap;
import java.util.Map;
@Singleton
public class Session {

	private Map<String, User> users = new HashMap<>();

	public User getUser(String sid) {
		return users.get(sid);
	}

	public void setUser(String sid, User user) {
		this.users.put(sid, user);
	}

}
