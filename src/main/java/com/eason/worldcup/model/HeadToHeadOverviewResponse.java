package com.eason.worldcup.model;

import java.util.ArrayList;
import java.util.List;

public class HeadToHeadOverviewResponse {

    private List<HeadToHeadMatchResponse> homeRecentMatches = new ArrayList<>();

    private List<HeadToHeadMatchResponse> headToHeadMatches = new ArrayList<>();

    private List<HeadToHeadMatchResponse> awayRecentMatches = new ArrayList<>();

    public List<HeadToHeadMatchResponse> getHomeRecentMatches() {
        return homeRecentMatches;
    }

    public void setHomeRecentMatches(List<HeadToHeadMatchResponse> homeRecentMatches) {
        this.homeRecentMatches = homeRecentMatches == null ? new ArrayList<>() : homeRecentMatches;
    }

    public List<HeadToHeadMatchResponse> getHeadToHeadMatches() {
        return headToHeadMatches;
    }

    public void setHeadToHeadMatches(List<HeadToHeadMatchResponse> headToHeadMatches) {
        this.headToHeadMatches = headToHeadMatches == null ? new ArrayList<>() : headToHeadMatches;
    }

    public List<HeadToHeadMatchResponse> getAwayRecentMatches() {
        return awayRecentMatches;
    }

    public void setAwayRecentMatches(List<HeadToHeadMatchResponse> awayRecentMatches) {
        this.awayRecentMatches = awayRecentMatches == null ? new ArrayList<>() : awayRecentMatches;
    }

}
