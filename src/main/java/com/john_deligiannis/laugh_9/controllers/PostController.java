package com.john_deligiannis.laugh_9.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.john_deligiannis.laugh_9.bodies.PostCategoryRequest;
import com.john_deligiannis.laugh_9.bodies.PostIdRequest;
import com.john_deligiannis.laugh_9.bodies.UsernameRequest;
import com.john_deligiannis.laugh_9.entities.Post;
import com.john_deligiannis.laugh_9.entities.User;
import com.john_deligiannis.laugh_9.entities.UserVote;
import com.john_deligiannis.laugh_9.entities.enums.*;
import com.john_deligiannis.laugh_9.repositories.PostRepository;
import com.john_deligiannis.laugh_9.repositories.UserRepository;
import com.john_deligiannis.laugh_9.repositories.UserVoteRepository;
import com.john_deligiannis.laugh_9.security_config.JwtTokenUtil;
import com.john_deligiannis.laugh_9.storage.StorageFileNotFoundException;
import com.john_deligiannis.laugh_9.storage.StorageService;


@Controller
@RequestMapping(path="api/post")
public class PostController {

	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserVoteRepository userVoteRepository;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	private final StorageService storageService;
	
	@Autowired
	public PostController(StorageService storageService) {
		this.storageService = storageService;
	}
	
	@PostMapping(path="/add")
	public @ResponseBody ResponseEntity<String> addPost(
			@RequestHeader("Authorization") String tokenHeader,
			@RequestParam(required=false) MultipartFile file,
			@RequestParam String title,
			@RequestParam String category
	) {
		String jwtToken = tokenHeader.substring(7);
		String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
		
		User user = userRepository.findByUsername(username);
		
		if((user != null) && !title.isEmpty() && !category.isEmpty()) {
			
			String mediaSource = "post_default.jpg";
			if(file != null && !file.isEmpty()) {
				String[] args = file.getOriginalFilename().split("\\.");
				String extension = args[1];
				
				int rnd = (int) (Math.random() * 100000 + 10000);
				mediaSource = username + "_post_" + rnd + "." + extension;
				storageService.store(file, mediaSource);	
			}
			
			Post post = new Post();
			post.setMediaSource(mediaSource);
			post.setUser(user);
			post.setTitle(title);
			post.setCategory(category);
			post.setDownvotes(0L);
			post.setUpvotes(0L);
			post.setPopularity(Popularity.NEW.toString());
			post.setDate(new Date().getTime());
			postRepository.save(post);
			
			return ResponseEntity.ok("Post uploaded");
		}
		return ResponseEntity.badRequest().body("Unable to upload post");
	}
	
	@PostMapping(path="/delete")
	public @ResponseBody ResponseEntity<String> deletePost(
			@RequestHeader("Authorization") String tokenHeader,
			@RequestBody PostIdRequest postIdRequest
	) {
		String jwtToken = tokenHeader.substring(7);
		String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
		
		User user = userRepository.findByUsername(username);
		
		Post post = postRepository.findByUserAndPostId(user, postIdRequest.getPostId());
		
		if(post != null && user != null) {
			postRepository.delete(post);
			return ResponseEntity.ok("Post deleted");
		}
		
		return ResponseEntity.badRequest().body("Unable to delete post");
	}
	
	@PostMapping(path="/get/popular")
	public @ResponseBody ResponseEntity<List<Post>> getPopularPosts(
			@RequestBody PostCategoryRequest postCategoryRequest
	) {
		boolean categoryFound = false;
		for(Category c: Category.values()) {
			if(c.toString().equals(postCategoryRequest.getCategory())) {
				categoryFound = true;
			}
		}
		
		if(categoryFound) {
			List<Post> posts = postRepository.findByPopularityAndCategory(Popularity.POPULAR.toString(), postCategoryRequest.getCategory());
			if(posts != null) {
				return ResponseEntity.ok(posts);
			}
		}
		
		return ResponseEntity.badRequest().body(null);
	}
	
	@PostMapping(path="/get/new")
	public @ResponseBody ResponseEntity<List<Post>> getNewPosts(
			@RequestBody PostCategoryRequest postCategoryRequest
	) {
		boolean categoryFound = false;
		for(Category c: Category.values()) {
			if(c.toString().equals(postCategoryRequest.getCategory())) {
				categoryFound = true;
			}
		}
		
		if(categoryFound) {
			List<Post> posts = postRepository.findByPopularityAndCategory(Popularity.NEW.toString(), postCategoryRequest.getCategory());
			if(posts != null) {
				return ResponseEntity.ok(posts);
			}
		}
		
		return ResponseEntity.badRequest().body(null);
	}
	
	@PostMapping("/get")
	public @ResponseBody ResponseEntity<Post> getPost(
			@RequestBody PostIdRequest postIdRequest
	) {

		Post post = postRepository.findByPostId(postIdRequest.getPostId());
		
		if(post != null) {
			return ResponseEntity.ok(post);
		}
		return ResponseEntity.badRequest().body(null);
	}
	
	@PostMapping("/get/user")
	public @ResponseBody ResponseEntity<List<Post>> getPost(
			@RequestBody UsernameRequest usernameRequest
	) {

		User user = userRepository.findByUsername(usernameRequest.getUsername());
		List<Post> posts = postRepository.findByUser(user);
		
		if(posts != null) {
			return ResponseEntity.ok(posts);
		}
		return ResponseEntity.badRequest().body(null);
	}
	
	@PostMapping("/get/categories")
	public @ResponseBody ResponseEntity<Category[]> getPost() {
	
		return ResponseEntity.ok(Category.values());
	}
	
	@PostMapping("/upvote")
	public @ResponseBody ResponseEntity<String> upvotePost(
			@RequestHeader("Authorization") String tokenHeader,
			@RequestBody PostIdRequest postIdRequest
	) {
		String jwtToken = tokenHeader.substring(7);
		String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
		
		User user = userRepository.findByUsername(username);
		Post post = postRepository.findByPostId(postIdRequest.getPostId());
		
		if(post != null && user != null) {
			UserVote userVote = userVoteRepository.findByUserAndPost(user, post);
			
			if(userVote == null) {
				userVote = new UserVote();
				userVote.setPost(post);
				userVote.setUser(user);
				userVote.setVote(Vote.UPVOTE.toString());
				userVoteRepository.save(userVote);
				
				post.setUpvotes(post.getUpvotes() + 1);
				postRepository.save(post);
				
				return ResponseEntity.ok("Post upvoted");
			} else {
				if(userVote.getVote().equals(Vote.DOWNVOTE.toString())) {
					
					userVote.setVote(Vote.UPVOTE.toString());
					userVoteRepository.save(userVote);
					
					post.setUpvotes(post.getUpvotes() + 1);
					post.setDownvotes(post.getDownvotes() - 1);
					postRepository.save(post);
					
					return ResponseEntity.ok("Post upvoted");
				}
				return ResponseEntity.badRequest().body("Unable to upvote post");
			}
		}
		return ResponseEntity.badRequest().body("Unable to upvote post");
	}
	
	@PostMapping("/downvote")
	public @ResponseBody ResponseEntity<String> downvotePost(
			@RequestHeader("Authorization") String tokenHeader,
			@RequestBody PostIdRequest postIdRequest
	) {
		String jwtToken = tokenHeader.substring(7);
		String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
		
		User user = userRepository.findByUsername(username);
		Post post = postRepository.findByPostId(postIdRequest.getPostId());
		
		if(post != null && user != null) {
			UserVote userVote = userVoteRepository.findByUserAndPost(user, post);
			
			if(userVote == null) {
				userVote = new UserVote();
				userVote.setPost(post);
				userVote.setUser(user);
				userVote.setVote(Vote.DOWNVOTE.toString());
				userVoteRepository.save(userVote);
				
				post.setDownvotes(post.getDownvotes() + 1);
				postRepository.save(post);
				
				return ResponseEntity.ok("Post downvoted");
			} else {
				if(userVote.getVote().equals(Vote.UPVOTE.toString())) {
					
					userVote.setVote(Vote.DOWNVOTE.toString());
					userVoteRepository.save(userVote);
					
					post.setUpvotes(post.getUpvotes() - 1);
					post.setDownvotes(post.getDownvotes() + 1);
					postRepository.save(post);
					
					return ResponseEntity.ok("Post downvoted");
				}
				return ResponseEntity.badRequest().body("Unable to downvote post");
			}
		}
		return ResponseEntity.badRequest().body("Unable to downvote post");
	}
	
	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}
	
}
