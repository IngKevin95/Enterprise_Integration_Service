package com.empresa.integration.infrastructure.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Implementacion de UserDetailsService que carga usuarios desde la base de datos. */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private static final String QUERY =
        "SELECT username, password FROM app_users WHERE username = ? AND enabled = true";

    private final DataSource dataSource;

    /**
     * Construye el servicio con el datasource configurado.
     *
     * @param dataSource datasource de la aplicacion
     */
    public JwtUserDetailsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(QUERY)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return User.withUsername(rs.getString("username"))
                    .password(rs.getString("password"))
                    .roles("USER")
                    .build();
            }
        } catch (SQLException ex) {
            throw new UsernameNotFoundException("Error loading user: " + username, ex);
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
