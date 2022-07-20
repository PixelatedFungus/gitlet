package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * The class that will be serialized into the STAGE file
 * where the blobs being tracked will be placed for commits.
 * The STATUS command can access this information in order to
 * retrieve relevant information for the user.
 * @author Wilson Chu
 */
public class Stage implements Serializable {

    /** This instance variable represents the BLOBS we want to update or add in
     *  our next commit. The key is the user defined name of the file and the
     *  value is the corresponding blob that is located in our blobs directory.
     */
    private HashMap<String, String> stagedFilesHashMap = new HashMap<>();

    /** This instance variable represents the BLOBS we want to remove from
     *  our commit BLOBS. The key is the user defined name of the file and the
     *  value is the corresponding blob that is located in our blobs directory.
     */
    private HashMap<String, String> removedFilesHashMap = new HashMap<>();


    /* The instance variables below are all used by toString and must be
    update accordingly.
     */

    /** A String ArrayList of branches that are currently available. */
    private ArrayList<String> branches = new ArrayList<>();

    /** A HashSet ArrayList of files in the CWD that are ready to
        be committed. */
    private ArrayList<String> stagedFiles = new ArrayList<>();

    /** A HashSet ArrayList of files in the CWD that are ready to be
        removed. */
    private ArrayList<String> removedFiles = new ArrayList<>();

    /**
     * A HashSet ArrayList of files in the CWD that are have been modified
     * and are being tracked by our most recent commit but have not been staged
     * for commit.
     */
    private ArrayList<String> modifiedNotStagedFiles = new ArrayList<>();

    /**
     * A HashSet arry of files that exist in the CWD but are not tracked by
     * our most recent commit.
     */
    private ArrayList<String> untrackedFiles = new ArrayList<>();

    /* Helpful functions that other classes can use to manipulate the stage. */



    /**
     * Represents the information in our instance variables in the proper
     * format as described by the spec. This function is inherently
     * called when we use the command STATUS.
     * @return The formatted String returned by our toString method.
     */
    @Override
    public String toString() {
        StringBuilder completeStage = new StringBuilder();
        completeStage.append(Repo.SEPERATOR + " Branches "
                + Repo.SEPERATOR + "\n");
        Collections.sort(branches);
        for (String branch : branches) {
            if (branch.equals(Repo.getActiveBranch().getName())) {
                completeStage.append("*");
            }
            completeStage.append(branch).append("\n");
        }
        completeStage.append("\n");
        completeStage.append(Repo.SEPERATOR + " Staged Files "
                + Repo.SEPERATOR + "\n");
        Collections.sort(stagedFiles);
        for (String stagedFile : stagedFiles) {
            completeStage.append(stagedFile).append("\n");
        }
        completeStage.append("\n");
        completeStage.append(Repo.SEPERATOR + " Removed Files "
                + Repo.SEPERATOR + "\n");
        Collections.sort(removedFiles);
        for (String removedFile : removedFiles) {
            completeStage.append(removedFile).append("\n");
        }
        completeStage.append("\n");
        completeStage.append(Repo.SEPERATOR + " Modifications Not "
                + "Staged For Commit "
                + Repo.SEPERATOR + "\n");
        Collections.sort(modifiedNotStagedFiles);
        for (String modifiedNotStagedFile : modifiedNotStagedFiles) {
            completeStage.append(modifiedNotStagedFile).append("\n");
        }
        completeStage.append("\n");
        completeStage.append(Repo.SEPERATOR + " Untracked Files "
                + Repo.SEPERATOR + "\n");
        Collections.sort(untrackedFiles);
        for (String untrackedFile : untrackedFiles) {
            completeStage.append(untrackedFile).append("\n");
        }
        return completeStage.toString();
    }

    /**
     * The getter function for the stagedFilesHashMap instance variable.
     * @return The stagedFilesHashMap.
     */
    public HashMap<String, String> getStagedFilesHashMap() {
        return stagedFilesHashMap;
    }

    /**
     * The getter function for the removedFilesHashMap instance variable.
     * @return The removedFilesHashMap.
     */
    public HashMap<String, String> getRemovedFilesHashMap() {
        return removedFilesHashMap;
    }

    /**
     * The getter function for the branches instance variable.
     * @return The ArrayList of branches in our .gitlet/branches folder.
     */
    public ArrayList<String> getBranches() {
        return branches;
    }

    /**
     * The getter function for the stagedFiles instance variable.
     * @return The ArrayList of files staged for our next commit.
     */
    public ArrayList<String> getStagedFiles() {
        return stagedFiles;
    }

    /**
     * The getter function for the removedFiles instance variable.
     * @return The ArrayList of files staged for removal in our next commit.
     */
    public ArrayList<String> getRemovedFiles() {
        return removedFiles;
    }

    /**
     * The getter function for the modifiedNotStagedFiles instance variable.
     * @return The ArrayList of files modified but not staged.
     */
    public ArrayList<String> getModifiedNotStagedFiles() {
        return modifiedNotStagedFiles;
    }

    /**
     * The getter function for the untrackedFiles instance variable.
     * @return The ArrayList of files untracked by our current commit.
     */
    public ArrayList<String> getUntrackedFiles() {
        return untrackedFiles;
    }

}
