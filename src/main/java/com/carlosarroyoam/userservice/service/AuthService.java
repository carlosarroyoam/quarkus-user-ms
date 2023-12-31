package com.carlosarroyoam.userservice.service;

import org.jboss.logging.Logger;

import com.carlosarroyoam.userservice.constant.AppMessages;
import com.carlosarroyoam.userservice.dto.LoginRequest;
import com.carlosarroyoam.userservice.dto.LoginResponse;
import com.carlosarroyoam.userservice.model.User;
import com.carlosarroyoam.userservice.repository.UserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.AuthenticationFailedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthService {

	private static final Logger LOG = Logger.getLogger(AuthService.class);
	private final UserRepository userRepository;
	private final TokenService tokenService;

	@Inject
	public AuthService(UserRepository userRepository, TokenService tokenService) {
		this.userRepository = userRepository;
		this.tokenService = tokenService;
	}

	public LoginResponse auth(LoginRequest loginRequest) {
		User userByUsername = userRepository.findByUsername(loginRequest.getUsername());

		if (userByUsername == null) {
			LOG.errorf(AppMessages.USER_ACCOUNT_NOT_FOUND_EXCEPTION_DETAILED_MESSAGE, loginRequest.getUsername());
			throw new AuthenticationFailedException(AppMessages.USER_ACCOUNT_NOT_FOUND_EXCEPTION_MESSAGE);
		}

		if (userByUsername.getIsActive().equals(Boolean.FALSE)) {
			LOG.errorf(AppMessages.USER_ACCOUNT_NOT_ACTIVE_EXCEPTION_DETAILED_MESSAGE, loginRequest.getUsername());
			throw new AuthenticationFailedException(AppMessages.USER_ACCOUNT_NOT_ACTIVE_EXCEPTION_MESSAGE);
		}

		if (!BcryptUtil.matches(loginRequest.getPassword(), userByUsername.getPassword())) {
			LOG.errorf(AppMessages.UNAUTHORIZED_CREDENTIALS_EXCEPTION_DETAILED_MESSAGE, loginRequest.getUsername());
			throw new AuthenticationFailedException(AppMessages.UNAUTHORIZED_CREDENTIALS_EXCEPTION_MESSAGE);
		}

		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setAccessToken(tokenService.generateToken(userByUsername));
		loginResponse.setUsername(userByUsername.getUsername());
		return loginResponse;
	}

}
