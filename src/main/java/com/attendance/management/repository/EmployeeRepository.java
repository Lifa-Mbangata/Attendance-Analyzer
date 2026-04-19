package com.attendance.management.repository;

import com.attendance.management.domain.Employee;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class EmployeeRepository implements IEmployeeRepository {

    private final ConcurrentMap<String, Employee> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Employee> findByEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(employeeId));
    }

    @Override
    public Employee save(Employee employee) {
        if (employee == null || employee.getEmployeeId() == null || employee.getEmployeeId().isBlank()) {
            throw new IllegalArgumentException("Employee and employeeId are required");
        }
        store.put(employee.getEmployeeId(), employee);
        return employee;
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void clear() {
        store.clear();
    }
}
