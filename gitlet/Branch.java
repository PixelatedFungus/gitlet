package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * This class allows us to create branches in gitlet.
 * @author Wilson Chu
 */
public class Branch implements Serializable {

    /** This variable points to the HEAD of THIS branch. This is the
     *  SHA1 of the commit file.
     */
    private String headCommit;

    /** The name of the branch being created. */
    private final String _name;

    /**
     * This constructor points the headCommit instance variable
     * to the COMMITFILE parameter.
     * @param name The name of the branch being created. Must be unique.
     */
    public Branch(String name) {
        _name = name;
        createLogFile();
    }

    /**
     * Method for initializing a log file for a new branch.
     * This method should only be called inside constructors as once
     * a log file is created for a branch, that will be the only
     * log file used from henceforth. This method also updates the
     * LOGFILE String which is the name of the file that contains the
     * logging information for this branch's commits.
     */
    private void createLogFile() {
        File newLog = Utils.join(Repo.BRANCHES, _name + "Log.txt");
        try {
            if (!newLog.createNewFile()) {
                System.out.println("A log file corresponding to branch "
                        + _name + " already exists. Cannot create "
                        + "a new log file.");
            }
        } catch (IOException e) {
            System.out.println("There is an issue in createLogFile where "
                        + "a new log file is not being created.");
        }
    }

    /**
     * This method takes a commit labeled COMMIT and adds it into the log txt
     * file associated with this branch as well as the global log file.
     * @param commit The commit object we are adding to our log file
     */
    public void log(Commit commit) {
        File logFile = Utils.join(Repo.BRANCHES, _name + "Log.txt");
        String logFileContents = Utils.readContentsAsString(logFile);
        SimpleDateFormat properDateFormat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");
        String globalLogContents = Utils.readContentsAsString(Repo.GLOBALLOG);
        String newInfo = Repo.SEPERATOR + "\n"
                + "commit " + headCommit + "\n" + "Date: ";
        newInfo += properDateFormat.format(commit.getNow()) + "\n";
        newInfo += commit.getCommitMessage();
        newInfo += "\n\n";
        String newGlobalInfo = newInfo;
        newInfo += logFileContents;
        newGlobalInfo += globalLogContents;
        Utils.writeContents(logFile, newInfo);
        Utils.writeContents(Repo.GLOBALLOG, newGlobalInfo);
    }

    /**
     * This method is very similar to the log method, except it is specifically
     * tailored for the merge command. It passes in a predetermined commit
     * message and makes sure to write the correct content to our log file.
     * @param commit The new commit that we are creating.
     * @param commit1SHA1 The SHA1 code of the HEAD commit of our
     *                    current branch.
     * @param commit2SHA1 The SHA1 code of the OTHER commit of our
     *                    incoming branch.
     */
    public void mergeLog(Commit commit, String commit1SHA1,
                         String commit2SHA1) {
        File logFile = Utils.join(Repo.BRANCHES, _name + "Log.txt");
        String logFileContents = Utils.readContentsAsString(logFile);
        SimpleDateFormat properDateFormat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");
        String globalLogContents = Utils.readContentsAsString(Repo.GLOBALLOG);
        String newInfo = Repo.SEPERATOR + "\n"
                + "commit " + headCommit + "\n";
        newInfo += "Merge: " + commit1SHA1.substring(0, 7) + " "
                + commit2SHA1.substring(0, 7) + "\n" + "Date: ";
        newInfo += properDateFormat.format(commit.getNow()) + "\n";
        newInfo += commit.getCommitMessage();
        newInfo += "\n\n";
        String newGlobalInfo = newInfo;
        newInfo += logFileContents;
        newGlobalInfo += globalLogContents;
        Utils.writeContents(logFile, newInfo);
        Utils.writeContents(Repo.GLOBALLOG, newGlobalInfo);
    }

    /**
     * Getter method for the HEADCOMMIT instance variable.
     * @return Type String that is the SHA1 code for the headCommit.
     */
    public String getHeadCommit() {
        return headCommit;
    }

    /**
     * Setter for the HEADCOMMIT instance variable.
     * @param newCommit Type String that is the SHA1 code for the new
     *                  headCommit.
     */
    public void setHeadCommit(String newCommit) {
        headCommit = newCommit;
    }

    /**
     * Getter method for the name of our branch.
     * @return Returns the name of the initialized branch object.
     */
    public String getName() {
        return _name;
    }

    /**
     * Overrides the default toString method.
     * @return The _name instance variable which is the name of THIS branch.
     */
    @Override
    public String toString() {
        return _name;
    }

}
