package com.john_deligiannis.laugh_9.bodies;

import org.springframework.web.multipart.MultipartFile;

public class AddUserRequest {

	private MultipartFile multipartFile;
	private String username;
	private String password;
	
	public MultipartFile getMultipartFile() {
		return multipartFile;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setMultipartFile(MultipartFile multipartFile) {
		this.multipartFile = multipartFile;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
}
