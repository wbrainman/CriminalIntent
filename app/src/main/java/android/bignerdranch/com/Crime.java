package android.bignerdranch.com;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSoloved;
    private String mSuspectNumber;
    private String mSuspect;

    public Crime() {
        this(UUID.randomUUID());
    }

    public  Crime(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSoloved() {
        return mSoloved;
    }

    public void setSoloved(boolean soloved) {
        mSoloved = soloved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getSuspectNumber() {
        return mSuspectNumber;
    }

    public void setSuspectNumber(String suspectNumber) {
        mSuspectNumber = suspectNumber;
    }
}
