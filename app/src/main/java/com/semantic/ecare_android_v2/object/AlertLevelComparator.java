package com.semantic.ecare_android_v2.object;

import java.util.Comparator;

public class AlertLevelComparator implements Comparator<Alert>{

	@Override
	public int compare(Alert lhs, Alert rhs) {
		if(lhs.getLevel()>rhs.getLevel()){
			return -1;
		}else if(lhs.getLevel()<rhs.getLevel()){
			return 1;
		}else{//equals
			return 0;
		}
	}

}
