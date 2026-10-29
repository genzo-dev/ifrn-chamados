package com.ifrn.chamados.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ifrn.chamados.common.enums.UserType;
import com.ifrn.chamados.common.utils.EntityFinder;
import com.ifrn.chamados.department.Department;
import com.ifrn.chamados.department.DepartmentRepository;
import com.ifrn.chamados.users.dto.UserCreateDTO;
import com.ifrn.chamados.users.dto.UserUpdateDTO;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UsersService {
    private final UsersRepository usersRepository;
    private  final DepartmentRepository departmentRepository;

    public Users create(UserCreateDTO dto) {
        if (usersRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "O e-mail informado já está sendo utilizado. Por favor, informe outro e-mail." );
        }

        if (usersRepository.existsByRegistrationNumber(dto.registrationNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "A matricula já está cadastrada no sistema." );
        }

        Department department = null;

        if (dto.userType() == UserType.TECNICO) {
            if (dto.departmentId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Usuários do tipo TÉCNICO devem estar obrigatoriamente vinculados a um setor.");
            }

            department = EntityFinder.findByIdOrThrow(departmentRepository, dto.departmentId(), "Departamento");
        }

        Users user = new Users();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setRegistrationNumber(dto.registrationNumber());
        user.setUserType(dto.userType());
        user.setDepartment(department);

        return usersRepository.save(user);
    }

    public Page<Users> findAllFiltered(String name, UserType userType, Long departmentId, Pageable pageable) {
        String searchName = (name != null && !name.isBlank()) ? name.trim() : null;
        return usersRepository.findWithFilters(searchName, userType, departmentId, pageable);
    }

    public Users findById(Long id) {
        return EntityFinder.findByIdOrThrow(usersRepository, id, "Usuário");
    }

    public Users updateById(Long id, UserUpdateDTO dto) {
        Users user = EntityFinder.findByIdOrThrow(usersRepository, id, "Usuário");

        if (usersRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "O e-mail informado já está sendo utilizado por outro usuário.");
        }

        Department department = null;

        if (dto.userType() == UserType.TECNICO) {
            if (dto.departmentId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Usuários do tipo TÉCNICO devem estar obrigatoriamente vinculados a um setor.");
            }

            department = EntityFinder.findByIdOrThrow(departmentRepository, dto.departmentId(), "Departamento");
        }

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setUserType(dto.userType());
        user.setDepartment(department);

        return usersRepository.save(user);
    }
}
