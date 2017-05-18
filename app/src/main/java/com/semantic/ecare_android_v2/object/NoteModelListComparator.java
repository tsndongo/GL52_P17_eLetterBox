package com.semantic.ecare_android_v2.object;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class NoteModelListComparator implements Comparator<NoteModel>{

	@Override
	public int compare(NoteModel lhs, NoteModel rhs) {
		
		Long lhsTime = getDateTimeFromString(lhs.getNoteDate());
		Long rhsTime = getDateTimeFromString(rhs.getNoteDate());
		
		return rhsTime.compareTo(lhsTime);
	}
	
	private Long getDateTimeFromString(String dateString){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}
}
