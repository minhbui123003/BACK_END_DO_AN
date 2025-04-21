package com.TTT.Tniciu_API.Service;

import com.TTT.Tniciu_API.Model.Role;
import com.TTT.Tniciu_API.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role roleDetails) {
        Optional<Role> optionalRole = roleRepository.findById(id);
        if (optionalRole.isPresent()) {
            Role existingRole = optionalRole.get();
            existingRole.setName(roleDetails.getName());
            return roleRepository.save(existingRole);
        } else {
            // Throw an exception or handle accordingly
            return null;
        }
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
