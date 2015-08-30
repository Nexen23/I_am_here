package alex.imhere.service;

import alex.imhere.service.api.DateApi;
import alex.imhere.service.api.UserApi;
import alex.imhere.service.channel.PubnubBroadcastChannel;
import alex.imhere.service.domain.ApiService;
import alex.imhere.service.domain.ChannelService;
import alex.imhere.service.domain.ParserService;
import alex.imhere.service.parser.UserParser;

public class ImhereService extends Service {
	public ImhereService() {
		constructParserService();
		constructChannelService();
		constructApiService(getParserService());
	}

	protected void constructParserService() {
		ParserService parserService = new ParserService();
		parserService.setUserParser(new UserParser());
		setParserService(parserService);
	}

	protected void constructChannelService() {
		ChannelService channelService = new ChannelService();
		channelService.setBroadcastChannel(new PubnubBroadcastChannel());
		setChannelService(channelService);
	}

	protected void constructApiService(ParserService parserService) {
		ApiService apiService = new ApiService();
		apiService.setDateApi(new DateApi(parserService));
		apiService.setUserApi(new UserApi(parserService));
		setApiService(apiService);
	}
}
