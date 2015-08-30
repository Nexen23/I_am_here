package alex.imhere.service;

import alex.imhere.service.domain.ApiService;
import alex.imhere.service.domain.ChannelService;
import alex.imhere.service.domain.ParserService;

public class Service {
	ApiService apiService;
	ChannelService channelService;
	ParserService parserService;

	public ApiService getApiService() {
		return apiService;
	}

	public void setApiService(ApiService apiService) {
		this.apiService = apiService;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	public ParserService getParserService() {
		return parserService;
	}

	public void setParserService(ParserService parserService) {
		this.parserService = parserService;
	}
}
