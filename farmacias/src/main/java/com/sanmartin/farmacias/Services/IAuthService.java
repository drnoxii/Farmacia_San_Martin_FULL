package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.LoginDTO;
import com.sanmartin.farmacias.Dto.RegistroDTO;
import com.sanmartin.farmacias.Dto.UsuarioDTO;
import org.springframework.stereotype.Service;

@Service
public interface IAuthService {

    UsuarioDTO registrar(RegistroDTO dto);

    UsuarioDTO login(LoginDTO dto);
}
