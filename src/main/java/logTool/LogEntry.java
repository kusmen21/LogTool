package logTool;

import java.util.Calendar;

/**
 * LogEntry - one line in the log file
 */
public class LogEntry
{
    private String originalText;
    private Calendar date;
    private String username;
    private String message;

    public LogEntry(String originalText, Calendar date, String username, String message) {
        this.originalText = originalText;
        this.date = date;
        this.username = username;
        this.message = message;
    }

    public String getOriginalText() {
        return originalText;
    }

    public Calendar getDate() {
        return date;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public int getHour()
    {
        return date.get(Calendar.HOUR_OF_DAY);
    }

    public int getMonth()
    {
        return date.get(Calendar.MONTH);
    }

    public int getDay()
    {
        return date.get(Calendar.DAY_OF_MONTH);
    }

    public int getYear()
    {
        return date.get(Calendar.YEAR);
    }

    @Override
    public String toString() {
        return date.getTime() + "\t" + username + "\t" + message;
    }
}
