package com.adbhelper.adb.log;


public class StartFormatLog extends DefaultFormatLog {

    private static final String BEGIN_ERROR1 = "Error type ";
    private static final String BEGIN_ERROR = "Error: ";

    public StartFormatLog(LogAdb logAdb) {
		super(logAdb);
	}
    @Override
    public String changeLine(String line) {
	if (line.startsWith(BEGIN_ERROR))
	{
	    error(line.substring(BEGIN_ERROR.length()));
	    return null;
	} else
	if (line.startsWith(BEGIN_ERROR1))
	{
	    return null;
	}
	return super.changeLine(line);
    }

}
