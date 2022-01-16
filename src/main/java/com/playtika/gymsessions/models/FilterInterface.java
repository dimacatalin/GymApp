package com.playtika.gymsessions.models;

import java.util.List;

public interface FilterInterface {
    public List<GymSession> getListFromFilterQuery(String query);
}
