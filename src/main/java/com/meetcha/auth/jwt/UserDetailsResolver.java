package com.meetcha.auth.jwt;

import com.meetcha.global.annotation.AuthUser;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class);
    }


    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mav,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        AuthUser ann = parameter.getParameterAnnotation(AuthUser.class);
        boolean required = (ann == null) || ann.required();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            if (required) throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
            return null;
        }

        Object principal = auth.getPrincipal();

        // 커스텀 Principal인 경우
        if (principal instanceof JwtUserPrincipal p) {
            return p.userId();
        }

        // Spring Security 기본 JwtAuthenticationToken 사용하는 경우
        // sub는 authentication.getName()에 들어있음
        try {
            return java.util.UUID.fromString(auth.getName());
        } catch (Exception e) {
            if (required) throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
            return null;
        }
    }
}
