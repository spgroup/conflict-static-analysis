package br.unb.cic.analysis.samples;


public class InterproceduralPointsTo {

	class Employee {
		private String name = "Name";
		private int salary = 20;

		public Employee(String n, int s) {
			this.name = n;
			this.salary = s;
		}
	}

	public void foo() {
		Employee emp1 = new Employee("name", 20);   // source

		emp1.salary = random();

		Employee emp2 = emp1;

		blah(emp2);                                      // sink
		System.out.println(" salary" + emp1.salary);
	}

	private int random() {
		return 10;
	}

	private void blah(Employee emp) {
		int y = emp.salary + 1;

		y = y + 1;

		System.out.println(y);
	}

}