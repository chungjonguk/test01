/**
 * HTTP 요청 처리 계층.
 * <ul>
 *   <li>{@code *ApiController} — JSON REST (in: query/body/path, out: ResponseEntity)</li>
 *   <li>{@code page.*} — Thymeleaf 화면 (out: view 이름)</li>
 *   <li>{@code AuthController} — 폼 로그인·로그아웃 (out: redirect)</li>
 * </ul>
 */
package com.example.springbootapp.controller;
