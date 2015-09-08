package alex.imhere.service.component;

import alex.imhere.activity.ImhereActivity;
import alex.imhere.fragment.LoginFragment;
import alex.imhere.fragment.UsersFragment;
import alex.imhere.service.module.ApiModule;
import alex.imhere.service.module.ChannelModule;
import alex.imhere.service.module.ParserModule;
import alex.imhere.service.module.TickerModule;
import dagger.Component;

@Component(modules = {
		ApiModule.class,
		ChannelModule.class,
		ParserModule.class,
		TickerModule.class
})
public interface ServicesComponent {
	void inject(UsersFragment fragment);
	void inject(LoginFragment fragment);

	void inject(ImhereActivity activity);
}
