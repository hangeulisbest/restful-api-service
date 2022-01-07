package com.memo.backend.filter.login;

import com.memo.backend.commoncode.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class LoginCheckFilter implements Filter {

    private static final String[] whiteList = {"/api/member/v1",
            "/api/login/*",
            "/api/logout/*",
            "/swagger-ui/*",
            "/swagger-resources*",
            "/v3/api-docs"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        String requestURI = servletRequest.getRequestURI();
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        try{
            log.info("login 인증 체크 필터 start = {}",requestURI);

            if(isLoginCheckPath(requestURI)){
                // false 로 해야 세션생성을 안함
                HttpSession session = servletRequest.getSession(false);
                if(session == null  // 세션이 없거나
                || session.getAttribute(SessionConst.LOGIN_MEMBER)==null // 세션이 만료된 경우
                ){
                    log.info("미인증 사용자 요청 발생 = {}",requestURI);
                    servletResponse.sendError(401);
                    return;
                }
            }
            chain.doFilter(request,response);
        }catch (Exception e){
            log.error("check filter error = {}",e.getMessage());
            throw e; // was에 에러를 보내주어야함
        }finally {
            log.info("인증필터 종료 {}",requestURI);
        }
    }

    /**
     * 화이트리스트인 경우 세션체크없이 승낙
     */
    private boolean isLoginCheckPath(String requestURI){
        return !PatternMatchUtils.simpleMatch(whiteList,requestURI);
    }
}
