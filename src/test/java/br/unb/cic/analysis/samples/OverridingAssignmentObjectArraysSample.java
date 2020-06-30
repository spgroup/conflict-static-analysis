package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectArraysSample {

    public static void main(String[] args) {
        Employee employee0 =  new Employee("Name", 0);
        Employee employee1 =  new Employee("Name", 1);
        Employee employee2 =  new Employee("Name", 2);

        Employee[] arr = {
                employee0, employee1, employee2
        };

        Employee[] aux = {
                employee0, employee1, employee2
        };

        employee0.setName("Name1");
        arr[0] = employee0; //left
        arr[1] = new Employee("Name1", 1);
        aux =  arr; //left
        aux[1] =  new Employee("Name", 1); //right

        System.out.println(arr);
    }
}
