// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.sample.common;

public class Employees {
 
    public static Employee getDeveloperEmployeeItem() {

        Employee developerEmployee = new Employee();
        developerEmployee.setId("Andersen-" + System.currentTimeMillis());
        developerEmployee.setFirstName("Aaron");
        developerEmployee.setLastName("Andersen");
        return developerEmployee;
    }

    public static Employee getDevOpsEmployeeItem() {

        Employee devOpsEmployee = new Employee();
        devOpsEmployee.setId("Wakefield-" + System.currentTimeMillis());
        devOpsEmployee.setFirstName("John");
        devOpsEmployee.setLastName("Wakefield");
        return devOpsEmployee;
    }

    public static Employee getOperationalEmployeeItem() {

        Employee operationalEmployee = new Employee();
        operationalEmployee.setId("Johnson-" + System.currentTimeMillis());
        operationalEmployee.setFirstName("Michael");
        operationalEmployee.setLastName("Johnson");
        return operationalEmployee;
    }
    
    public static Employee getCEOEmployeeItem() {

        Employee ceoEmployee = new Employee();
        ceoEmployee.setId("Smith-" + System.currentTimeMillis());
        ceoEmployee.setFirstName("Brad");
        ceoEmployee.setLastName("Smith");
        return ceoEmployee;
    }
}
