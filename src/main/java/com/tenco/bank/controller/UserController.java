package com.tenco.bank.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.tenco.bank.dto.KakaoProfile;
import com.tenco.bank.dto.NaverProfile;
import com.tenco.bank.dto.OAuthToken;
import com.tenco.bank.dto.SignInFormDto;
import com.tenco.bank.dto.SignUpFormDto;
import com.tenco.bank.handler.exception.CustomRestfulException;
import com.tenco.bank.repository.entity.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired // DI
	private UserService userService;

	@Autowired
	private HttpSession httpSession;

	@Autowired
	private JavaMailSender javaMailSender;

	/**
	 * 회원가입 페이지 요청
	 * 
	 * @return signUp.jsp 파일 리턴
	 */
	@GetMapping("/sign-up")
	public String signUpPage() {
		// prefix: /WEB-INF/view/
		// suffix: .jsp
		return "user/signUp";
	}

	/**
	 * 회원가입 요청
	 * 
	 * @param dto
	 * @return 로그인 페이지(/sign-in)
	 */
	@PostMapping("/sign-up")
	public String signProc(SignUpFormDto dto) {

		System.out.println("dto : " + dto.toString());
		System.out.println(dto.getCustomFile().getOriginalFilename());
		// 인증검사 x
		// 유효성 검사
		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new CustomRestfulException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}

		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new CustomRestfulException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		if (dto.getFullname() == null || dto.getFullname().isEmpty()) {
			throw new CustomRestfulException(Define.ENTER_YOUR_FULLNAME, HttpStatus.BAD_REQUEST);
		}

		// 파일업로드 처리
		MultipartFile file = dto.getCustomFile();
		if (file.isEmpty() == false) {
			// 사용자가 이미지를 넣었다면 기능 구현
			// 파일 사이즈 체크
			// 20MB
			if (file.getSize() > Define.MAX_FILE_SIZE) {
				throw new CustomRestfulException("파일 크기는 20MB 이상 클 수 없습니다.", HttpStatus.BAD_REQUEST);
			}

			// 서버 컴퓨터에 파일을 넣을 디렉토리가 있는지 검사
			String saveDirectory = Define.UPLOAD_FILE_DERECTORY;
			// 폴더가 없다면 오류 발생(파일생성시)
			File dir = new File(saveDirectory);
			if (dir.exists() == false) {
				dir.mkdir(); // 폴더가 없으면 폴더 생성
			}

			// 파일 이름 (중복처리 예방)
			UUID uuid = UUID.randomUUID();
			String fileName = uuid + "_" + file.getOriginalFilename();
			System.out.println("fileName : " + fileName);

			String uploadPath = Define.UPLOAD_FILE_DERECTORY + File.separator + fileName;
			File destination = new File(uploadPath);

			try {
				file.transferTo(destination);
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}

			// 객체 상태 변경(insert 처리)
			dto.setOriginFileName(file.getOriginalFilename()); // 사용자가 입력한 파일명
			dto.setUploadFileName(fileName);
		}

		userService.createUser(dto);
		return "redirect:/user/sign-in";
	}

	/**
	 * 로그인 페이지 요청
	 * 
	 * @return
	 */
	@GetMapping("/sign-in")
	public String signInPage() {
		return "user/signIn";
	}

	/**
	 * 로그인 요청 처리
	 * 
	 * @param SignInFormDto
	 * @return account/list.jsp
	 */
	@PostMapping("/sign-in")
	public String signInProc(SignInFormDto dto) {
		// 1. 유효성 검사
		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new CustomRestfulException("username을 입력하세요", HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new CustomRestfulException("password을 입력하세요", HttpStatus.BAD_REQUEST);
		}

		// 서비스 호출
		User user = userService.readUser(dto);
		httpSession.setAttribute(Define.PRINCIPAL, user);

		// 로그인 완료 -> 페이지 결정 (account/list)
		return "redirect:/account/list";
	}

	// 로그아웃 기능
	@GetMapping("/logout")
	public String logout() {
		httpSession.invalidate();
		return "redirect:/user/sign-in";
	}

	// 이메일 전송 기능
	@GetMapping("/test/email")
	public void test1() {
		System.out.println("실행됨?111");
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo("ehdzl5464@gmail.com");
		message.setSubject("제목입니다");
		message.setText("내용입니다");
		message.setFrom("test@gmail.com");
		message.setReplyTo("test@gmail.com");
		javaMailSender.send(message);
		System.out.println("실행됨?");
	}

	// 카카오 로그인 기능
	// http://localhost:80/user/kakao-callback?code="xxxxx"
	@GetMapping("/kakao-callback")
	public String kakaoCallback(@RequestParam String code) {
		// POST 방식, Header 구성, body 구성
		RestTemplate rt1 = new RestTemplate();
		// 헤더 구성
		HttpHeaders headers1 = new HttpHeaders();
		headers1.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		// 바디 구성
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", "22b5d7d5af96960c53924dbcef20a6e3");
		params.add("redirect_uri", "http://localhost:80/user/kakao-callback");
		params.add("code", code);

		// 헤더 + 바디 결합
		HttpEntity<MultiValueMap<String, String>> reqMsg = new HttpEntity<>(params, headers1);

		// http 요청
		ResponseEntity<OAuthToken> response = rt1.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST,
				reqMsg, OAuthToken.class);

		// 인증 토큰을 가지고 사용자 정보 다시 요청
		RestTemplate rt2 = new RestTemplate();
		// 헤더 구성
		HttpHeaders headers2 = new HttpHeaders();
		headers2.add("Authorization", "Bearer " + response.getBody().getAccessToken());
		headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		// 바디 구성 x

		// 결합
		HttpEntity<MultiValueMap<String, String>> kakaoInfo = new HttpEntity<>(headers2);

		// 요청
		ResponseEntity<KakaoProfile> response2 = rt2.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST,
				kakaoInfo, KakaoProfile.class);

		System.out.println(response2.getBody());

		KakaoProfile kakaoProfile = response2.getBody();

		// 최초 사용자 판단 - 사용자 username 존재 여부 확인
		SignUpFormDto dto = SignUpFormDto.builder().username("OAuth_Kakao" + kakaoProfile.getProperties().getNickname())
				.fullname("Kakao").password("asd1234").build();

		User oldUser = userService.readUserByUserName(dto.getUsername());
		// null이 담겨있는 상황
		if (oldUser == null) {
			userService.createUser(dto);
			oldUser = new User();
			oldUser.setUsername(dto.getUsername());
			oldUser.setFullname(dto.getFullname());
		}
		oldUser.setPassword(null);

		// 로그인 처리
		httpSession.setAttribute(Define.PRINCIPAL, oldUser);

		return "redirect:/account/list";

	}

	// 네이버 로그인
	@GetMapping("/naver-callback")
	public String naverCallback(@RequestParam String code, @RequestParam String state) {

		RestTemplate restTemplate = new RestTemplate();

		String url = "https://nid.naver.com/oauth2.0/token?" + "grant_type=authorization_code"
				+ "&client_id=oLRZrhIDwIY3zzTgv5BB" + "&client_secret=5d0pwFpezV" + "&code=" + code + "&state=" + state;

		HttpHeaders httpHeaders = new HttpHeaders();

		// 헤더 + 바디
		HttpEntity<MultiValueMap<String, String>> reqMsg = new HttpEntity<>(httpHeaders);
		// 토큰 요청
		ResponseEntity<OAuthToken> response = restTemplate.exchange(url, HttpMethod.POST, reqMsg, OAuthToken.class);

		RestTemplate restTemplate2 = new RestTemplate();

		// 헤더
		HttpHeaders headers2 = new HttpHeaders();
		headers2.add("Authorization", "Bearer " + response.getBody().getAccessToken());

		// 헤더 + 바디
		HttpEntity<MultiValueMap<String, String>> reqMsg2 = new HttpEntity<>(headers2);

		// 사용자 정보 요청
		ResponseEntity<NaverProfile> response2 = restTemplate2.exchange("https://openapi.naver.com/v1/nid/me",
				HttpMethod.POST, reqMsg2, NaverProfile.class);

		NaverProfile naverProfile = response2.getBody();

		// 최초 사용자 판단 - 사용자 username 존재 여부 확인
		SignUpFormDto dto2 = SignUpFormDto.builder().username("OAuth_Naver" + naverProfile.getResponse().getName())
				.fullname("Naver").password("asd1234").build();

		User oldUser2 = userService.readUserByUserName(dto2.getUsername());
		// null이 담겨있는 상황
		if (oldUser2 == null) {
			userService.createUser(dto2);
			oldUser2 = new User();
			oldUser2.setUsername(dto2.getUsername());
			oldUser2.setFullname(dto2.getFullname());
		}
		oldUser2.setPassword(null);

		// 로그인 처리
		httpSession.setAttribute(Define.PRINCIPAL, oldUser2);

		return "redirect:/account/list";
	}

}
