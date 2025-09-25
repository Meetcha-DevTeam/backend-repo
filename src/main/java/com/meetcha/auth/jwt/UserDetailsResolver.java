package com.meetcha.auth.jwt;

import com.meetcha.global.annotation.AuthUser;
import com.meetcha.global.util.AuthHeaderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;
//    private final AuthHeaderUtils authHeaderUtils;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class);
    }

//    @Override
//    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//
//        if (request == null) {
//            throw new BadRequestException();
//        }
//        String bearerToken= (String) request.getAttribute("accessToken");
//
//        String accessToken = AuthHeaderUtils.extractBearerToken(bearerToken);
//        log.info("accessToken : "+ accessToken);
//
//        return jwtProvider.getUserId(accessToken);
//    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        AuthUser ann = parameter.getParameterAnnotation(AuthUser.class);
        boolean required = (ann == null) || ann.required();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            if (required) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof JwtUserPrincipal p) {
            return p.userId();
        }

        // 예상과 다른 Principal 타입이면 인증 실패 처리
        if (required) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보를 확인할 수 없습니다.");
        return null;
    }



}
