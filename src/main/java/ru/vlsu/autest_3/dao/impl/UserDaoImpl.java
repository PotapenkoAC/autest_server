package ru.vlsu.autest_3.dao.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import ru.vlsu.autest_3.dao.UserDao;
import ru.vlsu.autest_3.dao.mapper.UserMapper;
import ru.vlsu.autest_3.dao.mapper.UserRowMapper;
import ru.vlsu.autest_3.dao.model.UserDo;

import java.security.Key;
import java.util.*;

@Component
public class UserDaoImpl implements UserDao {


    private static final String GET_USER_BY_LOGIN_AND_PASSWORD_SQL = "SELECT * FROM \"user\" WHERE login = :username AND password = :password";

    private static final String GET_USER_BY_TOKEN_SQL = "SELECT * FROM \"user\" WHERE token = :token";

    private static final String SET_LOGIN_SQL = "UPDATE \"user\" SET token = :token WHERE id = :id";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public UserDaoImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String login(String username, String password) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", username)
                .addValue("password", password);
        Optional<UserDo> optUser = jdbcTemplate.query(GET_USER_BY_LOGIN_AND_PASSWORD_SQL, params, new UserRowMapper()).stream().findFirst();
        UserDo user = optUser.orElse(null);
        if (user != null) {
            user.setToken(generateToken(user));
            MapSqlParameterSource updateParams = new MapSqlParameterSource()
                    .addValue("token", user.getToken())
                    .addValue("id", user.getId());
            jdbcTemplate.update(SET_LOGIN_SQL, updateParams);
            return user.getToken();
        }
        return "";
    }

    @Override
    public UserDo getByToken(String token) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("token", token);
        return (jdbcTemplate.query(GET_USER_BY_TOKEN_SQL, params, new UserRowMapper()).stream().findFirst()).orElse(null);

    }


    @Override
    public Optional<User> findByToken(String token) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("token", token);
        return (jdbcTemplate.query(GET_USER_BY_TOKEN_SQL, params, new UserMapper()).stream().findFirst());
    }

    private String generateToken(UserDo user) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        Map<String, String> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("login", user.getLogin());
        claims.put("id", user.getId().toString());
        return Jwts.builder().setClaims(claims).setHeaderParam(JwsHeader.KEY_ID, Arrays.toString(key.getEncoded())).signWith(key).compact();
    }
}
