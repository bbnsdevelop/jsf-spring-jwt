package com.example.demo.config.filter;

import static com.example.demo.config.Constants.HEADER_STRING;
import static com.example.demo.config.Constants.SECRET;
import static com.example.demo.config.Constants.TOKEN_PREFIX;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.demo.config.security.service.CustomUserDetailsService;
import com.example.demo.entities.ApplicationUser;

import io.jsonwebtoken.Jwts;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
	
	private final CustomUserDetailsService customUserDetailsService;
		
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, CustomUserDetailsService customUserDetailsService) {
		super(authenticationManager);
		this.customUserDetailsService = customUserDetailsService;
		
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,	HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		String header = request.getHeader(HEADER_STRING);
		if(header == null || !header.startsWith(TOKEN_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}
		UsernamePasswordAuthenticationToken authenticationToken = getAuthenticationToken(request);
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		chain.doFilter(request, response);
	}
	
	private UsernamePasswordAuthenticationToken getAuthenticationToken(HttpServletRequest request) {
		String token = request.getHeader(HEADER_STRING);
		if(token == null) {
			return null;
		}
		String username = Jwts.parser().setSigningKey(SECRET)
				.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
				.getBody()
				.getSubject();
		UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
		ApplicationUser applicationUser = this.customUserDetailsService.loadApplicationUserByUsername(username);
		return username != null ? new UsernamePasswordAuthenticationToken(applicationUser, null, userDetails.getAuthorities()) : null;
	} 
	
	

}
