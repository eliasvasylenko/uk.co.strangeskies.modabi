package uk.co.strangeskies.modabi.benchmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PersonsType {
	private final List<PersonType> person;

	public PersonsType() {
		person = new ArrayList<>();
	}

	public PersonsType(Collection<? extends PersonType> person) {
		this.person = new ArrayList<>(person);
	}

	public List<PersonType> getPerson() {
		return person;
	}
}