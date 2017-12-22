
package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import com.xdev.security.authentication.Authenticator;
import com.xdev.security.authentication.AuthenticatorProvider;
import com.xdev.security.authentication.CredentialsUsernamePassword;
import com.xdev.security.authentication.jpa.HashStrategy;
import com.xdev.security.authentication.jpa.JPAAuthenticator;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.entities.User;

public class AuthenticationProvider
		implements AuthenticatorProvider<CredentialsUsernamePassword, CredentialsUsernamePassword> {
	private static AuthenticationProvider INSTANCE;

	public static AuthenticationProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AuthenticationProvider();
		}

		return INSTANCE;
	}

	private final HashStrategy hashStrategy = new HashStrategy.SHA2();
	private JPAAuthenticator authenticator;

	private AuthenticationProvider() {
	}

	@Override
	public Authenticator<CredentialsUsernamePassword, CredentialsUsernamePassword> provideAuthenticator() {
		if (this.authenticator == null) {
			this.authenticator = new JPAAuthenticator(User.class);
			this.authenticator.setHashStrategy(getHashStrategy());
		}

		return this.authenticator;
	}

	public HashStrategy getHashStrategy() {
		return this.hashStrategy;
	}
}
