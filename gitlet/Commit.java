package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles committing files from the staging are.
 * @author Wilson Chu
 */
public class Commit implements Serializable {

    /** The message that the user passes in or the default message. */
    private final String commitMessage;

    /**
     * A HashMap containing a key that is the file name the user chose
     * and the values are the SHA1 file titles.
     */
    private HashMap<String, String> blobs = new HashMap<>();

    /** The exact time when THIS commit object is initialized. */
    private final Date now;

    /** The SHA1 string of the previous commit. */
    private String prevCommit;

    /** Secondary parent used only when committing a merge. */
    private String secondaryCommit;

    /** This constructor should only be called when we are intializing
     * a repository. This constructor is unique as the message
     * being used is unique - "initial commit". The blobs object
     * will be null because there are no files being tracked
     * in the initial commit.
     */
    public Commit() {
        commitMessage = "initial commit";
        now = new Date(0);
    }

    /**
     * A constructor for commit that is used for all other cases when
     * creating a new commit.
     * @param message The message the user inputs.
     */
    public Commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Stage stagingArea = Repo.getStagingArea();
        if (stagingArea.getStagedFilesHashMap().size() == 0
            && stagingArea.getRemovedFilesHashMap().size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        commitMessage = message;
        now = new Date();
        prevCommit = Repo.getActiveBranch().getHeadCommit();
        Commit oldCommit = Utils.readObject(Utils.join(Repo.COMMITS,
                Repo.getActiveBranch().getHeadCommit()), Commit.class);
        blobs = new HashMap<>(oldCommit.blobs);
        commit();
    }

    /**
     * A helper function for the constructors. Takes files from the
     * staging area and commits them.
     */
    private void commit() {
        if (Repo.getStagingArea().getStagedFilesHashMap().size() > 0) {
            blobs.putAll(Repo.getStagingArea().getStagedFilesHashMap());
        }
        if (Repo.getStagingArea().getRemovedFilesHashMap().size() > 0) {
            for (Map.Entry<String, String> blob
                    : Repo.getStagingArea()
                    .getRemovedFilesHashMap().entrySet()) {
                blobs.remove(blob.getKey(), blob.getValue());
            }
        }
        Repo.clearStagingArea();
    }

    /**
     * A getter method to get the date that the commit was initialized.
     * @return A Date object.
     */
    public Date getNow() {
        return now;
    }

    /**
     * A getter method to get the user's input for the message parameter.
     * @return A String object.
     */
    public String getCommitMessage() {
        return commitMessage;
    }

    /**
     * A getter method to get the blobs tracked by this commit.
     * @return A HashMap object of our blobs.
     */
    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    /**
     * A getter method to get the SHA1 of our previous commit.
     * @return The SHA1 of our previous commit.
     */
    public String getPrevCommit() {
        return prevCommit;
    }

    /**
     * A setter method to set the SHA1 of our previous commit.
     * @param incomingCommit The SHA1 of our previous commit.
     */
    public void setPrevCommit(String incomingCommit) {
        prevCommit = incomingCommit;
    }

    /**
     * A getter method to get the SHA1 of our secondary commit.
     * @return The SHA1 of our secondary commit.
     */
    public String getSecondaryCommit() {
        return secondaryCommit;
    }

    /**
     * A setter method for our secondary commit.
     * @param incomingSecondary The SHA1 of our secondary commit.
     */
    public void setSecondaryCommit(String incomingSecondary) {
        secondaryCommit = incomingSecondary;
    }
}
