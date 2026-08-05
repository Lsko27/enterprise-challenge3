package br.com.fiap.enterprise_challenge3.security;

import br.com.fiap.enterprise_challenge3.model.Cidadao;
import br.com.fiap.enterprise_challenge3.model.Servidor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(
            @Value("${jwt.secret}") String segredoBase64,
            @Value("${jwt.expiration-ms:3600000}")
            long expiracaoMs
    ) {
        byte[] bytesDaChave =
                Decoders.BASE64.decode(segredoBase64);

        this.chave =
                Keys.hmacShaKeyFor(bytesDaChave);

        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(Cidadao cidadao) {
        return gerarToken(
                cidadao.getId(),
                cidadao.getNome(),
                "CIDADAO"
        );
    }

    public String gerarToken(Servidor servidor) {
        return gerarToken(
                servidor.getId(),
                servidor.getNome(),
                "SERVIDOR"
        );
    }

    private String gerarToken(
            Long usuarioId,
            String nome,
            String perfil
    ) {
        Instant agora = Instant.now();
        Instant expiracao =
                agora.plusMillis(expiracaoMs);

        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("nome", nome)
                .claim("perfil", perfil)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave)
                .compact();
    }

    public Long extrairUsuarioId(String token) {
        String subject =
                extrairClaims(token).getSubject();

        return Long.valueOf(subject);
    }

    public String extrairPerfil(String token) {
        return extrairClaims(token)
                .get("perfil", String.class);
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiracaoEmSegundos() {
        return expiracaoMs / 1000;
    }
}