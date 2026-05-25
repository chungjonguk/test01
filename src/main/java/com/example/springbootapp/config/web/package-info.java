/**
 * MVC 공개 URL 암호화 ({@code /e/{token}.do}).
 * <ul>
 *   <li>{@link com.example.springbootapp.config.web.PublicPathCryptoService} — 암·복호화</li>
 *   <li>{@link com.example.springbootapp.config.web.EncryptedPathDecodeFilter} — 요청 경로 복호화</li>
 *   <li>{@link com.example.springbootapp.config.web.DoPathRedirectFilter} — 평문 URL → 암호화 URL 리다이렉트</li>
 * </ul>
 */
package com.example.springbootapp.config.web;
