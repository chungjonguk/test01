package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 화면 경로: {@code /admin/table-sequences}
 * <p>DB 테이블 시퀀스(sys_table_sequence) 목록 조회 화면.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminTableSequenceController {

	@GetMapping({"/table-sequences", "/table-sequences.html"})
	public String tableSequences() {
		return "admin/table-sequences";
	}
}
