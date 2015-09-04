package alex.imhere.service;

import alex.imhere.activity.ImhereActivity;
import alex.imhere.fragment.LoginStatusFragment;
import alex.imhere.fragment.UsersFragment;
import alex.imhere.service.api.DateApi;
import alex.imhere.service.api.UserApi;
import alex.imhere.service.module.ApiModule;
import alex.imhere.service.module.ChannelModule;
import alex.imhere.service.module.ParserModule;
import dagger.Component;

@Component(modules = {
		ApiModule.class,
		ChannelModule.class,
		ParserModule.class
})
public interface ServicesComponent {
	void inject(UsersFragment fragment);
	void inject(LoginStatusFragment fragment);
}
