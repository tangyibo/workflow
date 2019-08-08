package com.weishao.SpringFlowable.config;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.flowable.common.engine.impl.runtime.Clock;

public class MyDefaultClock implements Clock {

	@SuppressWarnings("unused")
	private Date currentTime;
	
	@SuppressWarnings("unused")
	private Calendar currentCalendar;

	public MyDefaultClock() {
		this.reset();
	}

	@Override
	public Date getCurrentTime() {
		return new Date();
	}

	@Override
	public Calendar getCurrentCalendar() {
		return Calendar.getInstance();
	}

	@Override
	public Calendar getCurrentCalendar(TimeZone timeZone) {
		return Calendar.getInstance();
	}

	@Override
	public TimeZone getCurrentTimeZone() {
		return TimeZone.getDefault();
	}

	@Override
	public void setCurrentTime(Date currentTime) {
		this.currentTime = currentTime;
	}

	@Override
	public void setCurrentCalendar(Calendar currentTime) {
		this.currentCalendar = currentTime;
	}

	@Override
	public void reset() {
		this.currentTime = new Date();
		this.currentCalendar = Calendar.getInstance();
	}

}
