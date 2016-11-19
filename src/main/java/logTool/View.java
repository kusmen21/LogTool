package logTool;

import java.util.List;

public class View
{
    public void printLine(String line)
    {
        System.out.println(line);
    }

    public void printError(String text)
    {
        printLine("### ERROR: " + text + " ###");
    }

    public void printSuccess()
    {
        printLine("### Your parameter saved successfully! ###");
    }

    public void printLogEntries(List<LogEntry> list)
    {
        printLine("\n###     DATE     ###     USERNAME     ###     MESSAGE     ###\n");
        for (LogEntry logEntry : list)
        {
            printLine(logEntry.toString());
        }
    }
}
