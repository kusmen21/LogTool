package logTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

public class Controller
{
    private Model model;
    private View view;
    private BufferedReader reader;

    private Controller()
    {
        view = new View();
        model = new Model(view);
        reader = new BufferedReader(new InputStreamReader(System.in));
        processLogAnalysis();
    }

    public static void main(String[] args)
    {
        new Controller();
    }

    private void processLogAnalysis()
    {
        getFilterParameters();
        getGroupingParameter();
        getCountOfThreads();
        getOutputFileName();
        closeResources();

        model.updateListWithFilteredLines();
        model.doGrouping();
        model.printAggregateStatistics();
        model.writeInFile();
    }

    /**
     * this method gets filter parameters from user and saves to the model
     * user can chose several parameters
     */
    private void getFilterParameters()
    {
        int filterOption = 0;
        boolean isOneParameterEntered = false;

            view.printLine("### Welcome to the LogTool! ###");
            view.printLine("Enter 'exit' anywhere to close the program");

            while (true) {
                if(isOneParameterEntered)
                    view.printLine("\nYou can enter additional parameters or leave filter settings by entering '4'");
                else view.printLine("\nPlease enter filter parameters");

                view.printLine("'1' to set username filter");
                view.printLine("'2' to set time period filter");
                view.printLine("'3' to set pattern for custom message");
                if(isOneParameterEntered) view.printLine("'4' to leave filter settings");
                try
                {
                    filterOption = Integer.parseInt(getLine());
                    if (!(filterOption >= 1 && filterOption <= 3)) throw new Exception();
                } catch (Exception e)
                {
                    if(isOneParameterEntered && filterOption == 4) break;
                    view.printError("Please, enter correct option number");
                    continue;
                }

                if (filterOption == 1) {
                    view.printLine("Enter username to filter:");
                    model.setUsernameFilter(getLine());
                    isOneParameterEntered = true;
                    view.printSuccess();
                } else if (filterOption == 2) {
                    view.printLine("Enter time period to filter in format 'yyyy-mm-dd yyyy-mm-dd'");
                    view.printLine("For example: 2016-09-05 2016-10-30");
                    String line = getLine();
                    if (line.matches("^\\d{4}-\\d{2}-\\d{2} \\d{4}-\\d{2}-\\d{2}$")) {
                        String[] twoDates = line.split(" ");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            model.setFromFilter(simpleDateFormat.parse(twoDates[0]));
                            model.setToFilter(simpleDateFormat.parse(twoDates[1]));
                            isOneParameterEntered = true;
                            view.printSuccess();
                        } catch (Exception e) {
                            view.printError("Please, enter correct time period");
                        }
                    } else {
                        view.printError("Please, enter correct time period");
                    }
                } else if (filterOption == 3) {
                    view.printLine("Enter pattern for custom message:");
                    model.setMessagePatternFilter(getLine());
                    isOneParameterEntered = true;
                    view.printSuccess();
                }
            }
    }

    /**
     * this method gets count of threads and saves to the model
     * (default=1)
     */
    private void getCountOfThreads()
    {
        while(true) {
            view.printLine("\nPlease enter count of threads used to process files (default = 1)");
            view.printLine("You can skip this option if you enter '1'");
            int countOfThreads;
            try {
                countOfThreads = Integer.parseInt(getLine());
                if (countOfThreads < 1) throw new Exception();
                model.setThreadCount(countOfThreads);
                view.printSuccess();
                break;
            } catch (Exception e) {
                view.printError("Please, enter correct count of threads");
            }
        }
    }

    /**
     * this method gets path to output file and filename and saves to the model
     * (default = src/logs/LogToolResult/LogToolOutput.log)
     */
    private void getOutputFileName()
    {
        int outputFileOption;
        while(true) {
            view.printLine("\nPlease enter output filename (default: src/logs/LogToolResult/LogToolOutput.log)");
            view.printLine("'1' to set path to output file and filename");
            view.printLine("'2' to set only filename");
            view.printLine("'3' to skip this option (default path to file)");

            try
            {
                outputFileOption = Integer.parseInt(reader.readLine());
                if (!( outputFileOption >= 1 &&  outputFileOption <= 3)) throw new Exception();
            } catch (Exception e)
            {
                view.printError("Please, enter correct option number");
                continue;
            }

            switch (outputFileOption)
            {
                case 1:
                {
                    view.printLine("Enter full path to output file directory (e.g. D:\\Projects\\):");
                    String line = getLine();
                    if (!line.endsWith("/") || !line.endsWith("\\")) line = line + "\\";
                    model.setPathToOutputFile(line);
                }
                case 2:
                {
                    view.printLine("Enter output filename (e.g. myLog.log):");
                    model.setOutputFileName(getLine());
                    break;
                }
                case 3: break;
            }
            view.printSuccess();
            break;
        }
    }

    /**
     * this method gets grouping type and saves to the model
     */
    private void getGroupingParameter()
    {
        int groupingOption;
        while(true)
        {
            view.printLine("\nPlease choose grouping parameter:");
            view.printLine("'1' to group by username");
            view.printLine("'2' to group by hour");
            view.printLine("'3' to group by day");
            view.printLine("'4' to group by month");
            view.printLine("'5' to group by year");
            try
            {
                groupingOption = Integer.parseInt(reader.readLine());
                if (!(groupingOption >= 1 && groupingOption <= 5)) throw new Exception();
            } catch (Exception e)
            {
                view.printError("Please, enter correct option number");
                continue;
            }
            switch (groupingOption)
            {
                case 1: model.setGroupingType(GroupingType.USERNAME); break;
                case 2: model.setGroupingType(GroupingType.HOUR); break;
                case 3: model.setGroupingType(GroupingType.DAY); break;
                case 4: model.setGroupingType(GroupingType.MONTH); break;
                case 5: model.setGroupingType(GroupingType.YEAR); break;
            }
            view.printSuccess();
            break;
        }
    }

    /**
     * if user wrote 'exit' - program closing
     * @return read line from console
     */
    private String getLine()
    {
        try
        {
            String line = reader.readLine();
            if (line.equalsIgnoreCase("exit"))
            {
                reader.close();
                System.exit(0);
            }
            else return line;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void closeResources()
    {
        try {
            reader.close();
        } catch (IOException ignored){}
    }
}
