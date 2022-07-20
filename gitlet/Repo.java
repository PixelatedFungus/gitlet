package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayDeque;

/**
 * This class handles the general commands used in our gitlet implementation.
 * @author Wilson Chu
 */
public class Repo {

    /** The current working directory with a slash at the end. */
    public static final String CWD = System.getProperty("user.dir") + "/";

    /** The .gitlet folder where all of our tracking will take place
     *  with slash. */
    public static final File GITLET = new File(CWD + ".gitlet/");

    /** The blobs folder where the blobs representing our files will exist
        with slash. */
    public static final File BLOBS = new File(CWD + ".gitlet/blobs/");

    /** The commits folder where our commit history
     * will be placed with slash. */
    public static final File COMMITS = new File(CWD
            + ".gitlet/commits/");

    /** The branches folder where all our branches are stored with slash. */
    public static final File BRANCHES = new File(CWD + ".gitlet/branches/");

    /** The file used for storing all of our staged changes,
     * ready to be committed. */
    public static final File STAGE = new File(CWD + ".gitlet/stage");

    /** The file used for storing our active branch serialized code. */
    public static final File ACTIVEBRANCH = new File(CWD + ".gitlet/active");

    /** The file used for storing our global log txt file. */
    public static final File GLOBALLOG = new File(CWD
            + ".gitlet/globalLog.txt");

    /** The default seperator for when we give the user an output. */
    public static final String SEPERATOR = "===";

    /** The branch that we are currently on. */
    private static Branch activeBranch;

    /** The object pointing to our staging area. This should be initialized
     *  each time we initialize this class.
     */
    private static Stage stagingArea;

    /**
     * Runs at the beginning of every command except for the INIT command.
     * Makes sure that a folder called .gitlet exists in the CWD.
     * If the .gitlet folder doesn't exist, the code prints out
     * "Not in an initialized Gitlet repository." and exits with
     * status 0.
     */
    public static void initializedCheck() {
        File gitletFile = new File(".gitlet");
        if (!gitletFile.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        String activeBranchName = Utils.readObject(ACTIVEBRANCH, String.class);
        activeBranch = Utils.readObject(Utils.join(BRANCHES, activeBranchName),
                Branch.class);
        stagingArea = Utils.readObject(STAGE, Stage.class);
    }

    /**
     * This method is called when the INIT command is ran. This method
     * takes in no parameters. It initializes the basic files
     * required for gitlet to work properly. If .gitlet was already
     * initialized, the method prints the message "A Gitlet version-control
     * system already exists in the current directory." Then the system
     * exits with status 0. If there are any issues with creating these
     * files, the mkdir() command will throw a SecurityException if
     * there are any issues. This SecurityException is handled with a
     * try/catch statement and will print out a message if the error
     * occurs.
     */
    public static void initialize() {
        try {
            if (GITLET.mkdir()) {
                BLOBS.mkdir();
                COMMITS.mkdir();
                BRANCHES.mkdir();
                STAGE.createNewFile();
                ACTIVEBRANCH.createNewFile();
                GLOBALLOG.createNewFile();
            } else {
                System.out.println("A Gitlet version-control "
                        + "system already exists in the current directory.");
                System.exit(0);
            }
        } catch (SecurityException e) {
            System.out.println("There was an issue in Repo.initialize() "
                    + "where the .gitlet file and its subdirectories could "
                    + "be made.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("There was an issue in Repo.initialize() "
                    + "where the STAGE file could not be made.");
        }
        stagingArea = new Stage();
        activeBranch = new Branch("master");
        Commit initialCommit = new Commit();
        stagingArea.getBranches().add("master");
        byte[] serializedInitialCommit = Utils.serialize(initialCommit);
        String initialCommitSHA1 = Utils.sha1((Object)
                serializedInitialCommit);
        Utils.writeObject(Utils.join(COMMITS, initialCommitSHA1),
                initialCommit);
        activeBranch.setHeadCommit(initialCommitSHA1);
        activeBranch.log(initialCommit);
        Utils.writeObject(Utils.join(BRANCHES, activeBranch.getName()),
                activeBranch);
        String currentBranchName = activeBranch.getName();
        Utils.writeObject(ACTIVEBRANCH, currentBranchName);
        Utils.writeObject(STAGE, stagingArea);
    }

    /**
     * This function implements the ADD command in gitlet. It looks at the
     * previous commit and the current staging area to determine which files
     * to add.
     * @param fileName The name of the file we want to add to our staging area.
     */
    public static void add(String fileName) {
        /*
         cases
         check if the file is already in the staging area
             check if the file is identical to the one tracked
             by the current commit, if so, remove the file from
             the staging area
         ------------------------------------------------------
             if the file is not identical to the one tracked
             by the current commit, create a new blob and
             overwrite the file in the staging area
         ------------------------------------------------------
         (case where the file is not in the staging area)
         check if the file is identical to the one being tracked
         by the current commit, if so, we do nothing, otherwise
         we create a new blob and add it to the staging area
         ------------------------------------------------------
         remove the file from the removal area
         rewrite the staging area back to its file
        */
        File file = new File(Utils.join(CWD, fileName).getPath());
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String headCommitSHA1 = activeBranch.getHeadCommit();
        Commit headCommit = Utils.readObject(Utils.join(COMMITS,
                headCommitSHA1), Commit.class);
        Stage stage = Utils.readObject(STAGE, Stage.class);
        stagingArea = stage;
        String fileContents = Utils.readContentsAsString(file);
        String cwdFileSHA1 = Utils.sha1(fileContents) + Utils.sha1(fileName);
        File newBlobFile = Utils.join(BLOBS, cwdFileSHA1);
        if (headCommit.getBlobs().size() > 0
                && cwdFileSHA1.equals(headCommit.getBlobs().get(fileName))) {
            stage.getStagedFilesHashMap().remove(fileName);
            stage.getStagedFiles().remove(fileName);
        } else {
            if (!newBlobFile.exists()) {
                Utils.writeObject(newBlobFile, fileContents);
            }
            stage.getStagedFilesHashMap().put(fileName, cwdFileSHA1);
            if (!stage.getBranches().contains(fileName)) {
                stage.getStagedFiles().add(fileName);
            }
        }
        stage.getRemovedFilesHashMap().remove(fileName);
        stage.getRemovedFiles().remove(fileName);
        Utils.writeObject(STAGE, stage);
    }

    /** This function implements the COMMIT command in gitlet. It creates a new
     *  commit with the given message parameter. Then this commit is serialized
     *  then our Utils.sha1 command is ran on the serialized bytes. Finally,
     *  this information is written to the appropriate file which is located
     *  in the commits folder. The file name is the SHA1 of our commit object.
     *  The active branch's headCommit is set to be this new commit object.
     *  The commit constructor automatically clears the staging area with
     *  its helper method.
     * @param message The message the user wants to associate with the commit.
     */
    public static void commit(String message) {
        Commit newCommit = new Commit(message);
        byte[] serializedNewCommit = Utils.serialize(newCommit);
        String newCommitSHA1 = Utils.sha1((Object) serializedNewCommit);
        Utils.writeObject(Utils.join(COMMITS, newCommitSHA1), newCommit);
        activeBranch.setHeadCommit(newCommitSHA1);
        activeBranch.log(newCommit);
        Utils.writeObject(Utils.join(BRANCHES, getActiveBranch().getName()),
                activeBranch);
    }

    /**
     * This function implements the LOG command in gitlet. It uses the
     * existing log file associated with the current active branch and reads
     * the file as a String, displaying it for the user.
     */
    public static void log() {
        File logFile = Utils.join(Repo.BRANCHES,
                activeBranch.getName() + "Log.txt");
        String log = Utils.readContentsAsString(logFile);
        System.out.println(log);
    }

    /**
     * This method implements the CHECKOUT command in gitlet. It is the
     * first of the three checkout methods. It takes a single input - the
     * name of the file we are trying to revert from the head commit. This
     * method will search for a file called FILENAME in the CWD. If such a
     * file exists, the method will overwrite the file with the contents of
     * the corresponding file in the head commit.
     * @param fileName The name of the file we are trying to revert.
     */
    public static void checkoutRevert(String fileName) {
        File pickedFile = new File(Utils.join(CWD, fileName).getPath());
        Commit headCommit = Utils.readObject(Utils.join(COMMITS,
                activeBranch.getHeadCommit()), Commit.class);
        if (!headCommit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String sha1fileName = headCommit.getBlobs().get(fileName);
        String fileNameContents = Utils.readObject(
                Utils.join(BLOBS, sha1fileName), String.class);
        Utils.writeContents(pickedFile, fileNameContents);
    }

    /**
     * This method implements the CHECKOUT command in gitlet. It is the
     * second of the three checkout methods. It uses the commit passed in
     * to find the file that we want to use to overwrite our file in our
     * CWD.
     * @param commitID The ID of the commit the user wants to find.
     * @param fileName The name of the file we want to overwrite in our CWD.
     */
    public static void checkoutFromCommit(String commitID, String fileName) {
        List<String> commitFiles = Utils.plainFilenamesIn(COMMITS);
        for (String commitFileName : commitFiles) {
            if (commitFileName.startsWith(commitID)) {
                commitID = commitFileName;
            }
        }
        File pickedFile = new File(Utils.join(CWD, fileName).getPath());
        File commitFile = new File(Utils.join(COMMITS, commitID).getPath());
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit pickedCommit = Utils.readObject(commitFile, Commit.class);
        if (!pickedCommit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String sha1fileName = pickedCommit.getBlobs().get(fileName);
        String fileNameContents = Utils.readObject(
                Utils.join(BLOBS, sha1fileName), String.class);
        Utils.writeContents(pickedFile, fileNameContents);
    }

    /**
     * This method implements the CHECKOUT command in gitlet. It is the
     * last of the three checkout methods. It uses the branch name passed
     * in by the user to bring the chosen branch into the CWD.
     * @param branchName A String object that is the name of the branch.
     */
    public static void checkoutBranch(String branchName) {
        File incomingBranchFile = Utils.join(BRANCHES, branchName);
        if (!incomingBranchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(activeBranch.getName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Branch incomingBranch = Utils.readObject(incomingBranchFile,
                Branch.class);
        wouldOverwrite(incomingBranch.getHeadCommit());
        setActiveBranch(incomingBranch);
        reset(incomingBranch.getHeadCommit());
        Utils.writeObject(ACTIVEBRANCH, incomingBranch.getName());
    }

    /**
     * This method implements the BRANCH command in gitlet. It creates a new
     * branch, then adds the newly created branch into our staging area and
     * writes the staging area into our stage file. It also points our new
     * branch's head commit to our most recent commit.
     * @param branchName This name is the name of the branch the user wants
     *                   to make.
     */
    public static void branch(String branchName) {
        if (stagingArea.getBranches().contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch newBranch = new Branch(branchName);
        stagingArea.getBranches().add(branchName);
        newBranch.setHeadCommit(activeBranch.getHeadCommit());
        File currentLogFile = Utils.join(BRANCHES,
                getActiveBranch().getName() + "Log.txt");
        File newLogFile = Utils.join(BRANCHES, branchName + "Log.txt");
        Utils.writeContents(newLogFile,
                Utils.readContentsAsString(currentLogFile));
        Utils.writeObject(Utils.join(BRANCHES, branchName), newBranch);
        Utils.writeObject(STAGE, stagingArea);
    }

    /**
     * This method implements the GLOBAL-LOG command in gitlet. It reads the
     * contents stored in the globalLog.txt file in the .gitlet folder and
     * prints it out.
     */
    public static void globalLog() {
        String globalFileContents = Utils.readContentsAsString(GLOBALLOG);
        System.out.println(globalFileContents);
    }

    /**
     * This method implements the RM command in gitlet. It takes
     * the file that needs to be removed. The method always tries
     * to remove the file from the staging area. If the file exists
     * in the most recent commit, the RM method will also delete the
     * file from the CWD as well as stage it for removal.
     * @param fileName The name of the file the user wants to delete.
     */
    public static void rm(String fileName) {
        File file = new File(Utils.join(CWD, fileName).getPath());
        Commit headCommit = Utils.readObject(Utils.join(COMMITS,
                activeBranch.getHeadCommit()), Commit.class);
        if (!stagingArea.getStagedFilesHashMap().containsKey(fileName)
            && !headCommit.getBlobs().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        stagingArea.getStagedFilesHashMap().remove(fileName);
        stagingArea.getStagedFiles().remove(fileName);
        if (headCommit.getBlobs().containsKey(fileName)) {
            file.delete();
            stagingArea.getRemovedFilesHashMap().put(fileName,
                    headCommit.getBlobs().get(fileName));
            if (!stagingArea.getStagedFiles().contains(fileName)) {
                stagingArea.getRemovedFiles().add(fileName);
            }
        }
        Utils.writeObject(STAGE, stagingArea);
    }

    /**
     * This method implements the RM-BRANCH command in gitlet. The
     * branch file is deleted and the name of the branch is removed
     * from our STAGINGAREA.
     * @param branchName The name of the branch the user wants to remove.
     */
    public static void rmBranch(String branchName) {
        if (activeBranch.getName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File branchFile = Utils.join(BRANCHES, branchName);
        if (!branchFile.delete()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        File branchFileLog = Utils.join(BRANCHES, branchName + "Log.txt");
        branchFileLog.delete();
        stagingArea.getBranches().remove(branchName);
        Utils.writeObject(STAGE, stagingArea);
    }

    /**
     * This method implements the RESET commaind in gitlet. This
     * method uses the wouldOverwrite and checkoutFromCommit
     * methods defined earlier in this class. This method reverts
     * the files in our CWD to the files tracked in the incoming
     * commit. The staging area is cleared after we do so and the
     * head of our activeBranch is set to the commit the user inputs.
     * @param incomingCommitID The ID of the commit the user wants
     *                         to set our activeBranch to.
     */
    public static void reset(String incomingCommitID) {
        File incomingCommitFile = Utils.join(COMMITS, incomingCommitID);
        if (!incomingCommitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        wouldOverwrite(incomingCommitID);
        List<String> cwdFiles =
                Utils.plainFilenamesIn(CWD);
        Commit incomingCommit = Utils.readObject(incomingCommitFile,
                Commit.class);
        for (String fileName : cwdFiles) {
            File filetoDelete = Utils.join(CWD, fileName);
            filetoDelete.delete();
        }
        for (Map.Entry<String, String> file
                : incomingCommit.getBlobs().entrySet()) {
            checkoutFromCommit(incomingCommitID, file.getKey());
        }
        activeBranch.setHeadCommit(incomingCommitID);
        String limitedHistory = getHistory(incomingCommitID);
        File activeBranchLog = Utils.join(BRANCHES, activeBranch + "Log.txt");
        Utils.writeContents(activeBranchLog, limitedHistory);
        Utils.writeObject(Utils.join(BRANCHES,
                activeBranch.getName()), activeBranch);
        clearStagingArea();
    }

    /**
     * This method gets the history of a given commit. This is used in
     * merge.
     * @param incomingCommit The commit whose history we are trying to find.
     * @return The log of the commit history.
     */
    private static String getHistory(String incomingCommit) {
        String existingHistory = "";
        Commit headCommit = Utils.readObject(Utils.join(COMMITS,
                        incomingCommit), Commit.class);
        String headCommitString = activeBranch.getHeadCommit();
        while (headCommit != null) {
            existingHistory += "===\n" + "commit " + headCommitString
                    + "\n";
            if (headCommit.getSecondaryCommit() != null) {
                existingHistory += "Merge: "
                        + headCommit.getPrevCommit().substring(0, 7) + " "
                        + headCommit.getSecondaryCommit().substring(
                                0, 7) + "\n";
            }
            SimpleDateFormat properDateFormat = new SimpleDateFormat(
                    "EEE MMM d HH:mm:ss yyyy Z");
            existingHistory += "Date: " + properDateFormat.format(
                    headCommit.getNow()) + "\n";
            existingHistory += headCommit.getCommitMessage();
            existingHistory += "\n\n";
            if (headCommit.getPrevCommit() != null) {
                headCommitString = headCommit.getPrevCommit();
                headCommit = Utils.readObject(Utils.join(COMMITS,
                        headCommit.getPrevCommit()), Commit.class);
            } else {
                headCommit = null;
            }
        }
        return existingHistory;
    }

    /**
     * A helper method that clears the staging area and writes it back to the
     * STAGE file.
     */
    public static void clearStagingArea() {
        Stage stage = getStagingArea();
        stage.getStagedFilesHashMap().clear();
        stage.getRemovedFilesHashMap().clear();
        stage.getStagedFiles().clear();
        stage.getRemovedFiles().clear();
        stage.getModifiedNotStagedFiles().clear();
        stage.getUntrackedFiles().clear();
        Utils.writeObject(Repo.STAGE, stage);
    }

    /**
     * A helper method that checks whether or not an incoming commit will
     * overwrite/delete existing files. This is meant to be called by
     * reset and checkout [branchName].
     * @param incomingCommitID The ID of the incoming commit.
     */
    private static void wouldOverwrite(String incomingCommitID) {
        Commit currentCommit = Utils.readObject(Utils.join(COMMITS,
                activeBranch.getHeadCommit()), Commit.class);
        Commit incomingCommit = Utils.readObject(Utils.join(COMMITS,
                incomingCommitID), Commit.class);
        List<String> cwdFiles =
                Utils.plainFilenamesIn(CWD);
        for (String fileName : cwdFiles) {
            if (incomingCommit.getBlobs().containsKey(fileName)
                    && !Utils.readObject(Utils.join(BLOBS,
                    incomingCommit.getBlobs().get(fileName)),
                    String.class).equals(Utils.readContentsAsString(
                            Utils.join(CWD, fileName)))
                    && !currentCommit.getBlobs().containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /**
     * This method implements the FIND commaind in gitlet. It iterates
     * through all the commits in our COMMITS folder and prints out all
     * the commit SHA1's associated with the user's message.
     * @param message The user's message.
     */
    public static void find(String message) {
        List<String> commitFiles =
                Utils.plainFilenamesIn(COMMITS);
        ArrayList<String> output = new ArrayList<>();
        for (String commitSHA1 : commitFiles) {
            Commit tempCommit = Utils.readObject(Utils.join(COMMITS,
                    commitSHA1), Commit.class);
            if (tempCommit.getCommitMessage().equals(message)) {
                output.add(commitSHA1);
            }
        }
        if (output.size() == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        for (String sha1 : output) {
            System.out.println(sha1);
        }
    }

    /**
     * The main merge function that is called from our main class. Uses many
     * helper functions to merge properly.
     * @param branch1String The String representing our current branch.
     * @param branch2String The String representing the incoming branch.
     */
    public static void merge(String branch1String, String branch2String) {
        if (!stagingArea.getBranches().contains(branch2String)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch1String.equals(branch2String)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String commonCommitSHA1 = getSplit(branch1String, branch2String);
        if (commonCommitSHA1 == null) {
            System.out.println("Something is wrong in the getSplit function.");
            System.exit(0);
        }
        Commit commonCommit = Utils.readObject(Utils.join(COMMITS,
                commonCommitSHA1), Commit.class);
        Branch branch1 = Utils.readObject(Utils.join(BRANCHES, branch1String),
                Branch.class);
        Branch branch2 = Utils.readObject(Utils.join(BRANCHES, branch2String),
                Branch.class);
        String branch1SHA1 = branch1.getHeadCommit();
        String branch2SHA1 = branch2.getHeadCommit();
        Commit branch1Head = Utils.readObject(Utils.join(COMMITS,
                branch1SHA1), Commit.class);
        Commit branch2Head = Utils.readObject(Utils.join(COMMITS,
                branch2SHA1), Commit.class);
        if (stagingArea.getStagedFiles().size() != 0
                || stagingArea.getRemovedFiles().size() != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        wouldOverwrite(Utils.readObject(Utils.join(BRANCHES, branch2String),
                Branch.class).getHeadCommit());
        if (commonCommitSHA1.equals(branch1.getHeadCommit())) {
            System.out.println("Current branch fast-forwarded.");
            checkoutBranch(branch2String);
            System.exit(0);
        }
        if (commonCommitSHA1.equals(branch2.getHeadCommit())) {
            System.out.println("Given branch is an ancestor of "
                    + "the current branch.");
            System.exit(0);
        }
        boolean mergeConflict = mergeInitialCases(branch1Head,
                branch2Head, commonCommit, branch2SHA1);
        Utils.writeObject(STAGE, stagingArea);
        commitMerge(branch1String, branch2String, branch1SHA1, branch2SHA1);
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * This method runs the initial cases in merge to shorten the
     * actual merge method.
     * @param branch1Head The Commit located at the head of branch1.
     * @param branch2Head The Commit located at the head of branch2.
     * @param commonCommit The Commit shared by branch1 and branch2.
     * @param branch2SHA1 The SHA1 of the branch2's head commit.
     * @return True if there was a merge error, false otherwise.
     */
    private static boolean mergeInitialCases(Commit branch1Head,
                                             Commit branch2Head,
                                             Commit commonCommit,
                                             String branch2SHA1) {
        HashSet<String> allBlobs = new HashSet<>(
                commonCommit.getBlobs().keySet());
        allBlobs.addAll(branch1Head.getBlobs().keySet());
        allBlobs.addAll(branch2Head.getBlobs().keySet());
        boolean mergeConflict = false;
        for (String fileName : allBlobs) {
            if (branch1Head.getBlobs().containsKey(fileName)
                    && branch2Head.getBlobs().containsKey(fileName)
                    && commonCommit.getBlobs().containsKey(fileName)) {
                caseA(fileName, branch1Head, branch2Head,
                        commonCommit, branch2SHA1);
                mergeConflict = caseD(fileName, branch1Head,
                        branch2Head, commonCommit)
                        || mergeConflict;
            } else if (!branch1Head.getBlobs().containsKey(fileName)
                    && branch2Head.getBlobs().containsKey(fileName)
                    && !commonCommit.getBlobs().containsKey(fileName)) {
                caseF(fileName, branch2Head);
            } else if (branch1Head.getBlobs().containsKey(fileName)
                    && !branch2Head.getBlobs().containsKey(fileName)
                    && commonCommit.getBlobs().containsKey(fileName)) {
                mergeConflict = caseD(fileName, branch1Head,
                        branch2Head, commonCommit)
                        || mergeConflict;
                caseG(fileName, branch1Head, commonCommit);
            } else if (!branch1Head.getBlobs().containsKey(fileName)
                    && branch2Head.getBlobs().containsKey(fileName)
                    && commonCommit.getBlobs().containsKey(fileName)) {
                mergeConflict = caseD(fileName, branch1Head,
                        branch2Head, commonCommit)
                        || mergeConflict;
            } else if (branch1Head.getBlobs().containsKey(fileName)
                    && branch2Head.getBlobs().containsKey(fileName)
                    && !commonCommit.getBlobs().containsKey(fileName)) {
                mergeConflict = caseD(fileName, branch1Head,
                        branch2Head, commonCommit) || mergeConflict;
            }
        }
        return mergeConflict;
    }

    /**
     * A commit method used by merge that includes a tailored commit message.
     * @param branch1String The string representing the name of our current
     *                      branch.
     * @param branch2String The string representing the name of our incoming
     *                      branch.
     * @param commit1SHA1 The string representing the SHA1 of the commit
     *                    at the head of our current branch.
     * @param commit2Sha1 The string representing the SHA1 of the commit
     *                    at the head of our incoming branch.
     */
    private static void commitMerge(String branch1String,
                                    String branch2String,
                                    String commit1SHA1,
                                    String commit2Sha1) {
        Commit newCommit = new Commit("Merged " + branch2String + " into "
                + branch1String + ".");
        newCommit.setSecondaryCommit(commit2Sha1);
        byte[] serializedNewCommit = Utils.serialize(newCommit);
        String newCommitSHA1 = Utils.sha1((Object) serializedNewCommit);
        Utils.writeObject(Utils.join(COMMITS, newCommitSHA1), newCommit);
        activeBranch.setHeadCommit(newCommitSHA1);
        activeBranch.mergeLog(newCommit, commit1SHA1, commit2Sha1);
        Utils.writeObject(Utils.join(BRANCHES, getActiveBranch().getName()),
                activeBranch);
    }

    /**
     * This method handles case A where a file exists in HEAD, other, and split
     * and the file was modified in other since the split point but not
     * modified in HEAD. In this case, the file is checked out from the other's
     * commit and the changes are staged.
     * @param fileName The name of the file we are working with.
     * @param head The head commit, our current commit.
     * @param other The incoming commit.
     * @param split The common commit shared between HEAD and other.
     * @param otherString The String representation of the other commit.
     */
    private static void caseA(String fileName, Commit head, Commit other,
                       Commit split, String otherString) {
        HashMap<String, String> headBlobs = head.getBlobs();
        HashMap<String, String> otherBlobs = other.getBlobs();
        HashMap<String, String> splitBlobs = split.getBlobs();
        if (!otherBlobs.get(fileName).equals(splitBlobs.get(fileName))
                && headBlobs.get(fileName).equals(splitBlobs.get(fileName))) {
            checkoutFromCommit(otherString, fileName);
            stagingArea.getStagedFiles().add(fileName);
            stagingArea.getStagedFilesHashMap().put(fileName,
                    otherBlobs.get(fileName));
        }
    }

    /**
     * This method handles case D where there is a merge error. This occurs
     * when files tracked in both the HEAD and other commit have differing
     * content. We overwrite the current version of the file in the CWD
     * with a representation containing both sets of file contents. Then
     * we stage this file.
     * @param fileName The name of the file we are working with.
     * @param head The head commit, our current commit.
     * @param other The incoming commit.
     * @param split The common ancestor of HEAD and other.
     * @return Returns true if there was indeed a merge error, otherwise false.
     */
    private static boolean caseD(String fileName, Commit head, Commit other,
                                 Commit split) {
        HashMap<String, String> headBlobs = head.getBlobs();
        HashMap<String, String> otherBlobs = other.getBlobs();
        HashMap<String, String> splitBlobs = split.getBlobs();
        boolean write = false;
        String newContent = "";
        if (!headBlobs.containsKey(fileName)
                && !otherBlobs.get(fileName).equals(
                        splitBlobs.get(fileName))) {
            String contentFromOtherBlob = Utils.readObject(Utils.join(BLOBS,
                    otherBlobs.get(fileName)), String.class);
            newContent = "<<<<<<< HEAD\n" + "=======\n"
                    + contentFromOtherBlob + ">>>>>>>\n";
            write = true;
        } else if (!otherBlobs.containsKey(fileName)
                && !headBlobs.get(fileName).equals(splitBlobs.get(fileName))) {
            String contentFromHeadBlob = Utils.readObject(Utils.join(
                    BLOBS, headBlobs.get(fileName)), String.class);
            newContent = "<<<<<<< HEAD\n" + contentFromHeadBlob
                    + "=======\n" + ">>>>>>>\n";
            write = true;
        } else if (headBlobs.containsKey(fileName)
                && otherBlobs.containsKey(fileName)
                && !headBlobs.get(fileName).equals(otherBlobs.get(fileName))
                && !headBlobs.get(fileName).equals(splitBlobs.get(fileName))
                && !otherBlobs.get(fileName).equals(
                        splitBlobs.get(fileName))) {
            String contentFromHeadBlob = Utils.readObject(Utils.join(BLOBS,
                    headBlobs.get(fileName)), String.class);
            String contentFromOtherBlob = Utils.readObject(Utils.join(BLOBS,
                    otherBlobs.get(fileName)), String.class);
            newContent = "<<<<<<< HEAD\n" + contentFromHeadBlob
                            + "=======\n" + contentFromOtherBlob + ">>>>>>>\n";
            write = true;
        }
        if (write) {
            String newBlobName = Utils.sha1(newContent) + Utils.sha1(fileName);
            Utils.writeObject(Utils.join(BLOBS, newBlobName), newContent);
            Utils.writeContents(Utils.join(CWD, fileName), newContent);
            stagingArea.getStagedFiles().add(fileName);
            stagingArea.getStagedFilesHashMap().put(fileName, newBlobName);
        }
        return write;
    }

    /**
     * This method handles case F which occurs when a file only exists in the
     * other branch. In this case, we will checkout the file from the other
     * branch's commit and write it into our CWD and stage it for addition.
     * @param fileName The file we are working with.
     * @param other The incoming commit.
     */
    private static void caseF(String fileName, Commit other) {
        HashMap<String, String> otherBlobs = other.getBlobs();
        File newFile = Utils.join(CWD, fileName);
        String fileContents = Utils.readObject(Utils.join(BLOBS,
                otherBlobs.get(fileName)), String.class);
        Utils.writeContents(newFile, fileContents);
        stagingArea.getStagedFiles().add(fileName);
        stagingArea.getStagedFilesHashMap().put(fileName,
                otherBlobs.get(fileName));
    }

    /**
     * This method handles case G which occurs when a file exists in HEAD and
     * split but not in other. In this case, we remove the file from the CWD
     * and stage it for removal.
     * @param fileName The file we are working with.
     * @param head The head commit, our current commit.
     * @param split The common ancestor of HEAD and other.
     */
    private static void caseG(String fileName, Commit head, Commit split) {
        HashMap<String, String> headBlobs = head.getBlobs();
        HashMap<String, String> splitBlobs = split.getBlobs();
        if (headBlobs.get(fileName).equals(splitBlobs.get(fileName))) {
            File tempFile = Utils.join(CWD, fileName);
            tempFile.delete();
            stagingArea.getRemovedFiles().add(fileName);
            stagingArea.getRemovedFilesHashMap().put(fileName,
                    headBlobs.get(fileName));
        }
    }

    /**
     * Our function for getting the split point of two branches using BFS.
     * @param branch1String The name of the current branch.
     * @param branch2String The name of the incoming branch.
     * @return The name of the split commit.
     */
    private static String getSplit(String branch1String,
                                   String branch2String) {
        HashSet<String> branchUnion = getUnion(branch1String, branch2String);
        HashMap<String, Integer> commitDepth = new HashMap<>();
        String headCommitString = Utils.readObject(
                Utils.join(BRANCHES, branch1String),
                Branch.class).getHeadCommit();
        Commit branch1Commit;
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(headCommitString);
        commitDepth.put(headCommitString, 0);
        while (queue.size() != 0) {
            String workingCommitSHA1 = queue.remove();
            branch1Commit = Utils.readObject(Utils.join(COMMITS,
                    workingCommitSHA1), Commit.class);
            int depth = commitDepth.get(workingCommitSHA1) + 1;
            if (branch1Commit.getPrevCommit() != null
                    && !commitDepth.containsKey(
                            branch1Commit.getPrevCommit())) {
                if (branch1Commit.getSecondaryCommit() != null
                        && !commitDepth.containsKey(
                                branch1Commit.getSecondaryCommit())) {
                    commitDepth.put(branch1Commit.getSecondaryCommit(), depth);
                    queue.add(branch1Commit.getSecondaryCommit());
                }
                commitDepth.put(branch1Commit.getPrevCommit(), depth);
                queue.add(branch1Commit.getPrevCommit());
            }
        }
        int shortestDepth = Integer.MAX_VALUE;
        String closestCommit = null;
        for (String commitSHA1 : branchUnion) {
            if (commitDepth.get(commitSHA1) < shortestDepth) {
                closestCommit = commitSHA1;
                shortestDepth = commitDepth.get(commitSHA1);
            }
        }
        return closestCommit;
    }

    /**
     * A method that gets a HashSet containing the shared commits between
     * two branches.
     * @param branch1String The name of our current branch.
     * @param branch2String The name of our incoming branch.
     * @return The HashSet of common commits shared by the two branches.
     */
    private static HashSet<String> getUnion(String branch1String,
                                            String branch2String) {
        HashSet<String> branch1Commits = getBranchHistory(branch1String);
        HashSet<String> branch2Commits = getBranchHistory(branch2String);
        HashSet<String> unionElements = new HashSet<>();
        for (String branch1CommitSHA1 : branch1Commits) {
            if (branch2Commits.contains(branch1CommitSHA1)) {
                unionElements.add(branch1CommitSHA1);
            }
        }
        return unionElements;
    }

    /**
     * A method to get the history of a branch as a HashSet.
     * @param branchString The string of the branch we want to find the
     *                     history of.
     * @return Returns a HashSet of the commits of the branch.
     */
    private static HashSet<String> getBranchHistory(String branchString) {
        HashSet<String> branchCommits = new HashSet<>();
        String branchHeadCommit = Utils.readObject(Utils.join(BRANCHES,
                branchString), Branch.class).getHeadCommit();
        branchCommits.add(branchHeadCommit);
        Commit branchHistory = Utils.readObject(Utils.join(COMMITS,
                Utils.readObject(Utils.join(BRANCHES, branchString),
                        Branch.class).getHeadCommit()), Commit.class);
        ArrayDeque<String> branchQueue = new ArrayDeque<>();
        while (true) {
            if (branchHistory.getPrevCommit() != null
                    && !branchCommits.contains(
                    branchHistory.getPrevCommit())) {
                branchQueue.add(branchHistory.getPrevCommit());
                branchCommits.add(branchHistory.getPrevCommit());
            }
            if (branchHistory.getSecondaryCommit() != null
                    && !branchCommits.contains(
                    branchHistory.getSecondaryCommit())) {
                branchQueue.add(branchHistory.getSecondaryCommit());
                branchCommits.add(branchHistory.getSecondaryCommit());
            }
            if (branchQueue.size() != 0) {
                branchHistory = Utils.readObject(Utils.join(
                        COMMITS, branchQueue.removeFirst()), Commit.class);
            } else {
                break;
            }
        }
        return branchCommits;
    }
    /**
     * A getter method for the currentBranch instance variable.
     * @return The current branch the user is using.
     */
    public static Branch getActiveBranch() {
        return activeBranch;
    }

    /**
     * A setter method for the currentBranch instance variable.
     * @param branch The branch that we want to set the current branch to.
     */
    public static void setActiveBranch(Branch branch) {
        activeBranch = branch;
    }

    /**
     * A setter method for the currentBranch instance variable.
     * @param branchName The name of the branch that we want to set the
     *                   current branch to.
     */
    public static void setActiveBranch(String branchName) {
        File newCurrentBranch = new File(BRANCHES + branchName);
        if (!newCurrentBranch.exists()) {
            throw new GitletException("Cannot set current branch "
                + "to be the new branch in Repo.setCurrentBranch.");
        }
        setActiveBranch(Utils.readObject(newCurrentBranch, Branch.class));
    }

    /**
     * A getter method for the stagingArea instance variable.
     * @return The staging area.
     */
    public static Stage getStagingArea() {
        return stagingArea;
    }
}
