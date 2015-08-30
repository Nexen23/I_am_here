package alex.imhere.service.domain;

import alex.imhere.service.api.DateApi;
import alex.imhere.service.api.UserApi;

public class ApiService {
	DateApi dateApi;
	UserApi userApi;

	public DateApi getDateApi() {
		return dateApi;
	}

	public void setDateApi(DateApi dateApi) {
		this.dateApi = dateApi;
	}

	public UserApi getUserApi() {
		return userApi;
	}

	public void setUserApi(UserApi userApi) {
		this.userApi = userApi;
	}
}
