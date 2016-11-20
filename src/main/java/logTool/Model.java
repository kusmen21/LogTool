package logTool;

import sun.security.pkcs.ParsingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Model
{
    private View view;
    //  list with all filtered lines from logs
    private List<LogEntry> list = new ArrayList<>();
    //  path to logs folder
    private static final String filesPath = "src/logs/";
    //  Filter parameters
    private String usernameFilter;
    private Date fromFilter;
    private Date toFilter;
    private String messagePatternFilter;
    //  Grouping parameter
    private GroupingType groupingType;
    //  Other parameters
    private int threadCount;
    private String pathToOutputFile = "src/logs/LogToolResult/";
    private String outputFileName = "LogToolOutput.log";

    public Model(View view)
    {
        this.view = view;
    }

    /**
     * this method scans folder for .log files, creates multiple threads for reading log files and fills the list
     * with filtered log entries
     */
    public void updateListWithFilteredLines()
    {
        view.printLine("### Processing... ###");
        try
        {
            ArrayList<Future<List<LogEntry>>> futures = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

            //  get only .log files
            File[] files = new File(filesPath).listFiles((dir, name) -> name.matches("^.*\\.log$"));
            for (File file : files)
            {
                futures.add(executorService.submit(getTask(file)));
            }

            //  executor service will wait 60 seconds for completion all tasks
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);

            //get list with all LogEntries
            for (Future<List<LogEntry>> future : futures)
            {
                list.addAll(future.get());
            }

        } catch (InterruptedException | ExecutionException e)
        {
            view.printError("Failed to get information from the task");
            e.printStackTrace();
        }
        catch (RejectedExecutionException e) {
            view.printError("Can`t submit the task");
            e.printStackTrace();
        }
    }

    /**
     * this method groups log entries in the list
     */
    public void doGrouping()
    {
        Comparator<LogEntry> comparator = null;

        if (groupingType == GroupingType.USERNAME)
            comparator = (o1, o2) -> o1.getUsername().compareTo(o2.getUsername());
        else if (groupingType == GroupingType.HOUR)
                comparator = (o1, o2) -> o1.getHour() < o2.getHour() ? -1 : o1.getHour() == o2.getHour() ? 0 : 1;
        else if (groupingType == GroupingType.DAY)
            comparator = (o1, o2) -> o1.getDay() < o2.getDay() ? -1 : o1.getDay() == o2.getDay() ? 0 : 1;
        else if (groupingType == GroupingType.MONTH)
            comparator = (o1, o2) -> o1.getMonth() < o2.getMonth() ? -1 : o1.getMonth() == o2.getMonth() ? 0 : 1;
        else if (groupingType == GroupingType.YEAR)
            comparator = (o1, o2) -> o1.getYear() < o2.getYear() ? -1 : o1.getYear() == o2.getYear() ? 0 : 1;

        Collections.sort(list, comparator);
    }

    /**
     * this method creates Callable task that opens log file, gets entries and matches them to filter
     * @param file - the file to be read
     * @return Callable task with list with LogEntries
     */
    private Callable<List<LogEntry>> getTask(File file)
    {
        Callable<List<LogEntry>> callable = () ->
        {
            List<LogEntry> list = new ArrayList<>();
            try(BufferedReader fileReader = new BufferedReader(new FileReader(filesPath + file.getName())))
            {
                while (fileReader.ready())
                {
                    String line = fileReader.readLine();
                    String[] logParameters = line.split(";");

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Calendar logDate = Calendar.getInstance();
                    logDate.setTime(simpleDateFormat.parse(logParameters[0]));

                    String username = logParameters[1];
                    String message = logParameters[2];

                    LogEntry logEntry = new LogEntry(line, logDate, username, message);
                    if (isMatchesToFilter(logEntry)) list.add(logEntry);
                }
            }
            catch (ParsingException e){e.printStackTrace();}
            return list;
        };
        return callable;
    }

    /**
     * this method matches LogEntry to filter
     * @param logEntry - one line in the log file
     * @return true if LogEntry matches to user`s filter
     */
    private boolean isMatchesToFilter(LogEntry logEntry)
    {
        /*Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2};.+;.+");*/

        if (usernameFilter != null && !usernameFilter.isEmpty())
        {
            if (!logEntry.getUsername().equals(usernameFilter)) return false;
        }
        if (messagePatternFilter != null && !messagePatternFilter.isEmpty())
        {
            if (!logEntry.getMessage().equals(messagePatternFilter)) return false;
        }
        if (fromFilter != null && toFilter != null)
        {
            if (logEntry.getDate().getTimeInMillis() < fromFilter.getTime() ||
                    logEntry.getDate().getTimeInMillis() > toFilter.getTime()) return false;
        }
        return true;
    }

    /**
     * this method writes aggregate statistics with filtered log records into the file
     */
    public void writeInFile()
    {
        PrintWriter writer = null;
        try
        {
            //  create all folders to output file
            Files.createDirectories(Paths.get(pathToOutputFile));
            writer = new PrintWriter(pathToOutputFile + outputFileName);
            for (LogEntry logEntry : list)
            {
                writer.println(logEntry.getOriginalText());
            }
            view.printLine("### Aggregate statistics was saved to " + pathToOutputFile + outputFileName + " ###");
        }
        catch (FileNotFoundException e)
        {
            view.printError("File not found!");
            e.printStackTrace();
        } catch (IOException e)
        {
            view.printError("Failed to create folders");
            e.printStackTrace();
        } finally
        {
            writer.close();
        }
    }

    /**
     * this method prints aggregate statistics with filtered log records
     */
    public void printAggregateStatistics()
    {
        view.printLogEntries(list);
    }

    //  Setters
    public void setUsernameFilter(String usernameFilter) {
        this.usernameFilter = usernameFilter;
    }

    public void setMessagePatternFilter(String messagePatternFilter) {
        this.messagePatternFilter = messagePatternFilter;
    }

    public void setFromFilter(Date fromFilter) {
        this.fromFilter = fromFilter;
    }

    public void setToFilter(Date toFilter) {
        this.toFilter = toFilter;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public void setPathToOutputFile(String pathToOutputFile) {
        this.pathToOutputFile = pathToOutputFile;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setGroupingType(GroupingType groupingType) {
        this.groupingType = groupingType;
    }
}
