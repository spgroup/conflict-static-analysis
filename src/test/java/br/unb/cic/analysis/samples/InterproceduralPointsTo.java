package br.unb.cic.analysis.samples;


public class InterproceduralPointsTo {
	public void foo() {
		Employee emp1 = new Employee("name", 20);   //source

		emp1.salary = random();

		Employee emp2 = emp1;

		blah(emp2);
		System.out.println(" salary" + emp1.salary);
	}

	public int random() {
		return 10;
	}

	public void blah(Employee emp) {
		int y = emp.salary + 1;                          // sink

		y = y + 1;

		System.out.println(y);
	}

}