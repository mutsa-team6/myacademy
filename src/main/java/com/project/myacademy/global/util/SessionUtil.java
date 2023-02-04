package com.project.myacademy.global.util;


import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtil {


    public static void setSessionNameAndRole(HttpServletRequest request, ReadEmployeeResponse employee) {
        HttpSession session = request.getSession(true);
        String foundName = employee.getName();
        session.setAttribute("name", foundName);
        if (employee.getEmployeeRole().equals(EmployeeRole.ROLE_USER)) {
            session.setAttribute("role", "강사");
        } else if (employee.getEmployeeRole().equals(EmployeeRole.ROLE_STAFF)) {
            session.setAttribute("role", "직원");
        } else {
            session.setAttribute("role", "원장");

        }

    }
}
