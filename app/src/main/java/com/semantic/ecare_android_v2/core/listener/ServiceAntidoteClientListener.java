package com.semantic.ecare_android_v2.core.listener;

import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.SensorState;




public interface ServiceAntidoteClientListener {
	void newSensorMeasure(CompoundMeasure measure);
	void newSensorState(SensorState state);
}
