package alex.imhere.service.domain;

import alex.imhere.service.parser.UserParser;

public class ParserService {
	UserParser userParser;

	public UserParser getUserParser() {
		return userParser;
	}

	public void setUserParser(UserParser userParser) {
		this.userParser = userParser;
	}
}
