package com.attendance.management.repository;

import com.attendance.management.domain.Employee;
import java.util.List;
import java.util.Optional;

public interface IEmployeeRepository {
    Optional<Employee> findByEmployeeId(String employeeId);
    Employee save(Employee employee);
    List<Employee> findAll();
    void clear();
}
