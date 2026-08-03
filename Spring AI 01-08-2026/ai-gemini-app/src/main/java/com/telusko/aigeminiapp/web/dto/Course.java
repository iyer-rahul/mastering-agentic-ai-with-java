package com.telusko.aigeminiapp.web.dto;

public record Course(
        String id,
        String title,
        String level,
        int hours,
        int priceInr,
        int seatsLeft
) {

    public Course withSeats(int newSeatCount) {
        return new Course(id, title, level, hours, priceInr, newSeatCount);
    }
}
