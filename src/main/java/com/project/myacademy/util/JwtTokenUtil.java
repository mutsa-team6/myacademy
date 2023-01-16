package com.project.myacademy.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtTokenUtil {

    public static Claims openToken(String token, String key) {
        // 어떤 토큰을 열건지, 그 토큰을 열 키는 무엇인지
        return Jwts
                .parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean isExpired(String token, String key) {
        // 내용이 만료면 ture, 만료되지 않았다면 false
        return openToken(token, key)
                .getExpiration()
                .before(new Date());
    }

    // true라면 권한을 발급하면 안된다.
    public static String getAccount(String token, String key) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .get("account", String.class);
    }

    public static String createToken(String account, String key, long expireTime) {
        Claims claims = Jwts
                .claims(); //key-value형태

        claims
                .put("account", account); //claims에 담을 정보를 여기에서 추가해주면 된다.


        return Jwts
                .builder()
                .setClaims(claims) //map같은 형태로 정보를 넣어주면 된다.
                .setIssuedAt(new Date(System.currentTimeMillis())) //발행된 시간
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))//유효시간
                .signWith(SignatureAlgorithm.HS256, key) //HS256알고리즘으로 key를 암호화 해줄것이다.
                .compact(); //토큰에 필요한 모든 정보
    }
}
