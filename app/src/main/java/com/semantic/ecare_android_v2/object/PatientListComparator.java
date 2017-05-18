package com.semantic.ecare_android_v2.object;

import java.util.Comparator;

public class PatientListComparator implements Comparator<Patient>{

	@Override
	public int compare(Patient lhs, Patient rhs) {
		return lhs.getName().compareTo(rhs.getName());
	}

}
